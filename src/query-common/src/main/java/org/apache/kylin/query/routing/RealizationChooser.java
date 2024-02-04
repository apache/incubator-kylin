/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kylin.query.routing;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.calcite.rex.RexNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.QueryContext;
import org.apache.kylin.common.constant.LogConstant;
import org.apache.kylin.common.exception.KylinTimeoutException;
import org.apache.kylin.common.exception.ServerErrorCode;
import org.apache.kylin.common.logging.SetLogCategory;
import org.apache.kylin.common.msg.MsgPicker;
import org.apache.kylin.common.util.NamedThreadFactory;
import org.apache.kylin.common.util.SetThreadName;
import org.apache.kylin.guava30.shaded.common.annotations.VisibleForTesting;
import org.apache.kylin.guava30.shaded.common.base.Preconditions;
import org.apache.kylin.guava30.shaded.common.collect.HashMultimap;
import org.apache.kylin.guava30.shaded.common.collect.ImmutableMap;
import org.apache.kylin.guava30.shaded.common.collect.Lists;
import org.apache.kylin.guava30.shaded.common.collect.Maps;
import org.apache.kylin.guava30.shaded.common.collect.Multimap;
import org.apache.kylin.guava30.shaded.common.collect.Sets;
import org.apache.kylin.metadata.cube.cuboid.NLayoutCandidate;
import org.apache.kylin.metadata.cube.cuboid.NLookupCandidate;
import org.apache.kylin.metadata.cube.model.LayoutEntity;
import org.apache.kylin.metadata.cube.model.NDataSegment;
import org.apache.kylin.metadata.cube.model.NDataflowManager;
import org.apache.kylin.metadata.cube.model.NIndexPlanManager;
import org.apache.kylin.metadata.model.FunctionDesc;
import org.apache.kylin.metadata.model.FusionModelManager;
import org.apache.kylin.metadata.model.ISourceAware;
import org.apache.kylin.metadata.model.JoinTableDesc;
import org.apache.kylin.metadata.model.MeasureDesc;
import org.apache.kylin.metadata.model.NDataModel;
import org.apache.kylin.metadata.model.NDataModelManager;
import org.apache.kylin.metadata.model.NTableMetadataManager;
import org.apache.kylin.metadata.model.ParameterDesc;
import org.apache.kylin.metadata.model.TableDesc;
import org.apache.kylin.metadata.model.TableRef;
import org.apache.kylin.metadata.model.TblColRef;
import org.apache.kylin.metadata.model.graph.JoinsGraph;
import org.apache.kylin.metadata.project.NProjectLoader;
import org.apache.kylin.metadata.project.NProjectManager;
import org.apache.kylin.metadata.realization.CapabilityResult;
import org.apache.kylin.metadata.realization.HybridRealization;
import org.apache.kylin.metadata.realization.IRealization;
import org.apache.kylin.metadata.realization.NoRealizationFoundException;
import org.apache.kylin.metadata.realization.NoStreamingRealizationFoundException;
import org.apache.kylin.metadata.realization.RealizationRuntimeException;
import org.apache.kylin.metadata.realization.SQLDigest;
import org.apache.kylin.query.relnode.OlapContext;
import org.apache.kylin.query.relnode.OlapContextProp;
import org.apache.kylin.query.relnode.OlapFilterRel;
import org.apache.kylin.query.relnode.OlapTableScan;
import org.apache.kylin.query.util.RelAggPushDownUtil;
import org.apache.kylin.storage.StorageContext;
import org.apache.kylin.util.FilterConditionExpander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.ttl.TtlRunnable;

import lombok.val;

public class RealizationChooser {

    private static final Logger logger = LoggerFactory.getLogger(RealizationChooser.class);
    private static final ExecutorService selectCandidateService = new ThreadPoolExecutor(
            KylinConfig.getInstanceFromEnv().getQueryRealizationChooserThreadCoreNum(),
            KylinConfig.getInstanceFromEnv().getQueryRealizationChooserThreadMaxNum(), 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("RealChooser"), new ThreadPoolExecutor.CallerRunsPolicy());

    private RealizationChooser() {
    }

    // select models for given contexts, return realization candidates for each context
    public static void selectLayoutCandidate(List<OlapContext> contexts) {
        // try different model for different context
        for (OlapContext ctx : contexts) {
            if (ctx.isConstantQueryWithAggregations()) {
                continue;
            }
            attemptSelectCandidate(ctx);
            Preconditions.checkNotNull(ctx.getRealization());
        }
    }

    public static void multiThreadSelectLayoutCandidate(List<OlapContext> contexts) {
        List<Future<?>> futureList = Lists.newArrayList();
        try {
            // try different model for different context
            CountDownLatch latch = new CountDownLatch(contexts.size());
            for (OlapContext ctx : contexts) {
                TtlRunnable r = Objects.requireNonNull(TtlRunnable.get(() -> selectCandidate0(latch, ctx)));
                Future<?> future = selectCandidateService.submit(r);
                futureList.add(future);
            }
            latch.await();
            for (Future<?> future : futureList) {
                future.get();
            }
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NoRealizationFoundException) {
                throw (NoRealizationFoundException) e.getCause();
            } else if (e.getCause() instanceof NoStreamingRealizationFoundException) {
                throw (NoStreamingRealizationFoundException) e.getCause();
            } else {
                throw new RealizationRuntimeException("unexpected error when choose layout", e);
            }
        } catch (InterruptedException e) {
            for (Future<?> future : futureList) {
                future.cancel(true);
            }
            QueryContext.current().getQueryTagInfo().setTimeout(true);
            Thread.currentThread().interrupt();
            throw new KylinTimeoutException("The query exceeds the set time limit of "
                    + KylinConfig.getInstanceFromEnv().getQueryTimeoutSeconds()
                    + "s. Current step: Realization chooser. ");
        }
    }

    private static void selectCandidate0(CountDownLatch latch, OlapContext ctx) {
        KylinConfig kylinConfig = KylinConfig.getInstanceFromEnv();
        String queryId = QueryContext.current().getQueryId();
        try (KylinConfig.SetAndUnsetThreadLocalConfig ignored0 = KylinConfig.setAndUnsetThreadLocalConfig(kylinConfig);
                SetThreadName ignored1 = new SetThreadName(Thread.currentThread().getName() + " QueryId %s", queryId);
                SetLogCategory ignored2 = new SetLogCategory(LogConstant.QUERY_CATEGORY)) {

            String project = ctx.getOlapSchema().getProjectName();
            NTableMetadataManager.getInstance(kylinConfig, project);
            NDataModelManager.getInstance(kylinConfig, project);
            NDataflowManager.getInstance(kylinConfig, project);
            NIndexPlanManager.getInstance(kylinConfig, project);
            NProjectLoader.updateCache(project);

            if (!ctx.isConstantQueryWithAggregations()) {
                ctx.setRealizationCheck(new RealizationCheck());
                attemptSelectCandidate(ctx);
                Preconditions.checkNotNull(ctx.getRealization());
            }
        } catch (KylinTimeoutException e) {
            logger.error("realization chooser thread task interrupted due to query [{}] timeout", queryId);
        } finally {
            NProjectLoader.removeCache();
            latch.countDown();
        }
    }

    @VisibleForTesting
    public static void attemptSelectCandidate(OlapContext context) {
        // Step 1. get model through matching fact table with query
        Multimap<NDataModel, IRealization> modelMap = makeOrderedModelMap(context);
        checkNoRealizationFound(context, modelMap);

        logger.info("Context join graph: {}, {}", context.toHumanReadString(), context.getJoinsGraph());

        // Step 2.1 try to exactly match model
        List<Candidate> candidates = trySelectCandidates(context, modelMap, false, false);

        // Step 2.2 try to partial match model
        if (CollectionUtils.isEmpty(candidates)) {
            String project = context.getOlapSchema().getProjectName();
            KylinConfig projectConfig = NProjectManager.getProjectConfig(project);
            boolean partialMatch = projectConfig.isQueryMatchPartialInnerJoinModel();
            boolean nonEquiPartialMatch = projectConfig.partialMatchNonEquiJoins();
            if (partialMatch || nonEquiPartialMatch) {
                candidates = trySelectCandidates(context, modelMap, partialMatch, nonEquiPartialMatch);
                context.getStorageContext().setPartialMatchModel(CollectionUtils.isNotEmpty(candidates));
            }
        }

        if (candidates.isEmpty()) {
            checkNoRealizationWithStreaming(context);
            RelAggPushDownUtil.registerUnmatchedJoinDigest(context.getTopNode());
            String project = context.getOlapSchema().getProjectName();
            String msg = NProjectManager.getProjectConfig(project).isQueryDryRunEnabled()
                    ? helpfulMessageForUser(context)
                    : toErrorMsg(context);
            throw new NoRealizationFoundException("No realization found for " + msg);
        }

        // Step 3. find the lowest-cost candidate
        QueryRouter.sortCandidates(context.getOlapSchema().getProjectName(), candidates);
        logger.trace("Cost Sorted Realizations {}", candidates);
        Candidate candidate = candidates.get(0);
        restoreOlapContextProps(context, candidate.getRewrittenCtx());
        context.fixModel(candidate.getRealization().getModel(), candidate.getMatchedJoinsGraphAliasMap());
        adjustForCapabilityInfluence(candidate, context);

        context.setRealization(candidate.getRealization());
        if (candidate.getCapability().isVacant()) {
            QueryContext.current().getQueryTagInfo().setVacant(true);
            NLayoutCandidate layoutCandidate = (NLayoutCandidate) candidate.capability.getSelectedCandidate();
            context.getStorageContext().setCandidate(layoutCandidate);
            context.getStorageContext().setLayoutId(layoutCandidate.getLayoutEntity().getId());
            context.getStorageContext().setEmptyLayout(true);
            return;
        }

        if (candidate.capability.getSelectedCandidate() instanceof NLookupCandidate) {
            boolean useSnapshot = context.isFirstTableLookupTableInModel(context.getRealization().getModel());
            context.getStorageContext().setUseSnapshot(useSnapshot);
        } else {
            Set<TblColRef> dimensions = Sets.newHashSet();
            Set<FunctionDesc> metrics = Sets.newHashSet();
            buildDimensionsAndMetrics(context, dimensions, metrics);
            buildStorageContext(context, dimensions, metrics, candidate);
            buildSecondStorageEnabled(context.getSQLDigest());
            if (!QueryContext.current().isForModeling()) {
                fixContextForTableIndexAnswerNonRawQuery(context);
            }
        }
    }

    private static void checkNoRealizationFound(OlapContext context, Multimap<NDataModel, IRealization> modelMap) {
        if (!modelMap.isEmpty()) {
            return;
        }
        checkNoRealizationWithStreaming(context);
        RelAggPushDownUtil.registerUnmatchedJoinDigest(context.getTopNode());
        String msg = NProjectManager.getProjectConfig(context.getOlapSchema().getProjectName()).isQueryDryRunEnabled()
                ? helpfulMessageForUser(context)
                : toErrorMsg(context);
        throw new NoRealizationFoundException("No realization found for " + msg);
    }

    private static List<Candidate> trySelectCandidates(OlapContext context, Multimap<NDataModel, IRealization> modelMap,
            boolean partialMatch, boolean nonEquiPartialMatch) {
        List<Candidate> candidates = Lists.newArrayList();
        for (NDataModel model : modelMap.keySet()) {
            // preserve the props of OlapContext may be modified
            OlapContextProp preservedOlapProps = preservePropsBeforeRewrite(context);
            Map<String, String> matchedAliasMap = matchJoins(model, context, partialMatch, nonEquiPartialMatch);
            Set<IRealization> realizations = Sets.newHashSet(modelMap.get(model));
            List<Candidate> list = selectRealizations(model, context, matchedAliasMap, realizations);

            // discard the props of OlapContext modified by rewriteCcInnerCol
            restoreOlapContextProps(context, preservedOlapProps);

            // The matchJoin() method has the potential to optimize the JoinsGraph, 
            // therefore we perform a check on ready segments at this point.
            if (!hasReadySegments(model)) {
                logger.info("Exclude this model {} because there are no ready segments", model.getAlias());
                continue;
            }

            if (CollectionUtils.isNotEmpty(list)) {
                candidates.addAll(list);
                logger.info("context & model({}/{}/{}) match info: {}", context.getOlapSchema().getProjectName(),
                        model.getUuid(), model.getAlias(), true);
            }
        }
        return candidates;
    }

    private static void checkNoRealizationWithStreaming(OlapContext context) {
        String projectName = context.getOlapSchema().getProjectName();
        KylinConfig kylinConfig = KylinConfig.getInstanceFromEnv();

        NTableMetadataManager tableManager = NTableMetadataManager.getInstance(kylinConfig, projectName);
        for (OlapTableScan tableScan : context.getAllTableScans()) {
            TableDesc tableDesc = tableManager.getTableDesc(tableScan.getTableName());
            if (tableDesc.getSourceType() == ISourceAware.ID_STREAMING) {
                throw new NoStreamingRealizationFoundException(ServerErrorCode.STREAMING_MODEL_NOT_FOUND,
                        MsgPicker.getMsg().getNoStreamingModelFound());
            }
        }
    }

    private static List<Candidate> selectRealizations(NDataModel model, OlapContext context,
            Map<String, String> matchedGraphAliasMap, Set<IRealization> realizations) {
        // skip selection
        if (MapUtils.isEmpty(matchedGraphAliasMap)) {
            return Lists.newArrayList();
        }

        context.fixModel(model, matchedGraphAliasMap);
        preprocessSpecialAggregations(context);
        List<Candidate> candidates = Lists.newArrayListWithCapacity(realizations.size());
        for (IRealization real : realizations) {
            Candidate candidate = selectRealization(context, real, matchedGraphAliasMap);
            if (candidate != null) {
                candidates.add(candidate);
                logger.trace("Model {} QueryRouter matched", model);
            } else {
                logger.trace("Model {} failed in QueryRouter matching", model);
            }
        }
        context.setNeedToManyDerived(needToManyDerived(model));
        context.unfixModel();
        return candidates;
    }

    public static Candidate selectRealization(OlapContext olapContext, IRealization realization,
            Map<String, String> matchedJoinGraphAliasMap) {
        if (!realization.isOnline()) {
            logger.warn("Realization {} is not ready", realization);
            return null;
        }

        Candidate candidate = new Candidate(realization, olapContext, matchedJoinGraphAliasMap);
        logger.info("Find candidates by table {} and project={} : {}", olapContext.getFirstTableScan().getTableName(),
                olapContext.getOlapSchema().getProjectName(), candidate);

        for (OlapFilterRel filterRel : olapContext.getAllFilterRels()) {
            List<RexNode> filterConditions = new FilterConditionExpander(olapContext, filterRel)
                    .convert(filterRel.getCondition());
            olapContext.getExpandedFilterConditions().addAll(filterConditions);
        }

        QueryRouter.applyRules(candidate);

        if (!candidate.getCapability().isCapable()) {
            return null;
        }

        logger.info("The realizations remaining: {}, and the final chosen one for current olap context {} is {}",
                candidate.getRealization().getCanonicalName(), olapContext.getId(),
                candidate.getRealization().getCanonicalName());
        return candidate;
    }

    static OlapContextProp preservePropsBeforeRewrite(OlapContext oriOlapContext) {
        OlapContextProp preserved = new OlapContextProp(-1);
        preserved.setAllColumns(Sets.newHashSet(oriOlapContext.getAllColumns()));
        preserved.setSortColumns(Lists.newArrayList(oriOlapContext.getSortColumns()));
        preserved.setInnerGroupByColumns(Sets.newHashSet(oriOlapContext.getInnerGroupByColumns()));
        preserved.setGroupByColumns(Sets.newLinkedHashSet(oriOlapContext.getGroupByColumns()));
        preserved.setInnerFilterColumns(Sets.newHashSet(oriOlapContext.getInnerFilterColumns()));
        for (FunctionDesc agg : oriOlapContext.getAggregations()) {
            preserved.getReservedMap().put(agg,
                    FunctionDesc.newInstance(agg.getExpression(), agg.getParameters(), agg.getReturnType()));
        }

        return preserved;
    }

    static void restoreOlapContextProps(OlapContext oriOlapContext, OlapContextProp preservedOlapContext) {
        oriOlapContext.setAllColumns(preservedOlapContext.getAllColumns());
        oriOlapContext.setSortColumns(preservedOlapContext.getSortColumns());
        // By creating a new hashMap, the containsKey method can obtain appropriate results.
        // This is necessary because the aggregations have changed during the query matching process,
        // therefore changed hash of the same object would be put into different bucket of the LinkedHashMap.
        Map<FunctionDesc, FunctionDesc> map = Maps.newHashMap(preservedOlapContext.getReservedMap());
        oriOlapContext.getAggregations().forEach(agg -> {
            if (map.containsKey(agg)) {
                final FunctionDesc functionDesc = map.get(agg);
                agg.setExpression(functionDesc.getExpression());
                agg.setParameters(functionDesc.getParameters());
                agg.setReturnType(functionDesc.getReturnType());
            }
        });
        oriOlapContext.setGroupByColumns(preservedOlapContext.getGroupByColumns());
        oriOlapContext.setInnerGroupByColumns(preservedOlapContext.getInnerGroupByColumns());
        oriOlapContext.setInnerFilterColumns(preservedOlapContext.getInnerFilterColumns());
        oriOlapContext.resetSQLDigest();
    }

    private static boolean needToManyDerived(NDataModel model) {
        return model.getJoinTables().stream().anyMatch(JoinTableDesc::isDerivedToManyJoinRelation);
    }

    private static boolean hasReadySegments(NDataModel model) {
        if (QueryContext.current().isDryRun())
            return true;
        val dataflow = NDataflowManager.getInstance(KylinConfig.getInstanceFromEnv(), model.getProject())
                .getDataflow(model.getUuid());
        if (model.isFusionModel()) {
            FusionModelManager fusionModelManager = FusionModelManager.getInstance(KylinConfig.getInstanceFromEnv(),
                    model.getProject());
            String batchId = fusionModelManager.getFusionModel(model.getFusionId()).getBatchModel().getUuid();
            val batchDataflow = NDataflowManager.getInstance(KylinConfig.getInstanceFromEnv(), model.getProject())
                    .getDataflow(batchId);
            return dataflow.hasReadySegments() || batchDataflow.hasReadySegments();
        }
        return dataflow.hasReadySegments();
    }

    public static void fixContextForTableIndexAnswerNonRawQuery(OlapContext context) {
        if (context.getRealization().getConfig().isUseTableIndexAnswerNonRawQuery()
                && !context.getStorageContext().isEmptyLayout() && context.isAnsweredByTableIndex()) {
            if (!context.getAggregations().isEmpty()) {
                List<FunctionDesc> aggregations = context.getAggregations();
                HashSet<TblColRef> needDimensions = Sets.newHashSet();
                for (FunctionDesc aggregation : aggregations) {
                    List<ParameterDesc> parameters = aggregation.getParameters();
                    for (ParameterDesc aggParameter : parameters) {
                        needDimensions.addAll(aggParameter.getColRef().getSourceColumns());
                    }
                }
                context.getStorageContext().getDimensions().addAll(needDimensions);
                context.getAggregations().clear();
            }
            if (context.getSQLDigest().getAggregations() != null) {
                context.getSQLDigest().getAggregations().clear();
            }
            if (context.getStorageContext().getMetrics() != null) {
                context.getStorageContext().getMetrics().clear();
            }
        }
    }

    private static void adjustForCapabilityInfluence(Candidate chosen, OlapContext olapContext) {
        CapabilityResult capability = chosen.getCapability();

        for (CapabilityResult.CapabilityInfluence inf : capability.influences) {

            if (inf instanceof CapabilityResult.DimensionAsMeasure) {
                FunctionDesc functionDesc = ((CapabilityResult.DimensionAsMeasure) inf).getMeasureFunction();
                functionDesc.setDimensionAsMetric(true);
                addToContextGroupBy(functionDesc.getSourceColRefs(), olapContext);
                olapContext.resetSQLDigest();
                olapContext.getSQLDigest();
                logger.info("Adjust DimensionAsMeasure for {}", functionDesc);
            } else {

                MeasureDesc involvedMeasure = inf.getInvolvedMeasure();
                if (involvedMeasure == null)
                    continue;

                involvedMeasure.getFunction().getMeasureType().adjustSqlDigest(involvedMeasure,
                        olapContext.getSQLDigest());
            }
        }
    }

    private static void addToContextGroupBy(Collection<TblColRef> colRefs, OlapContext context) {
        for (TblColRef col : colRefs) {
            if (!col.isInnerColumn() && context.belongToContextTables(col))
                context.getGroupByColumns().add(col);
        }
    }

    private static void preprocessSpecialAggregations(OlapContext context) {
        if (CollectionUtils.isEmpty(context.getAggregations()))
            return;
        Iterator<FunctionDesc> it = context.getAggregations().iterator();
        while (it.hasNext()) {
            FunctionDesc func = it.next();
            if (FunctionDesc.FUNC_GROUPING.equalsIgnoreCase(func.getExpression())) {
                it.remove();
            } else if (FunctionDesc.FUNC_INTERSECT_COUNT.equalsIgnoreCase(func.getExpression())) {
                TblColRef col = func.getColRefs().get(1);
                context.getGroupByColumns().add(col);
            }
        }
    }

    private static void buildSecondStorageEnabled(SQLDigest sqlDigest) {
        KylinConfig kylinConfig = KylinConfig.getInstanceFromEnv();
        if (kylinConfig.getSecondStorageQueryPushdownLimit() <= 0)
            return;

        if (sqlDigest.isRawQuery && sqlDigest.getLimit() > kylinConfig.getSecondStorageQueryPushdownLimit()) {
            QueryContext.current().setRetrySecondStorage(false);
        }
    }

    private static void buildStorageContext(OlapContext olapContext, Set<TblColRef> dimensions,
            Set<FunctionDesc> metrics, Candidate candidate) {
        boolean isBatchQuery = !(olapContext.getRealization() instanceof HybridRealization)
                && !olapContext.getRealization().isStreaming();
        if (isBatchQuery) {
            buildBatchStorageContext(olapContext.getStorageContext(), dimensions, metrics, candidate);
        } else {
            buildHybridStorageContext(olapContext.getStorageContext(), dimensions, metrics, candidate);
        }
    }

    private static void buildBatchStorageContext(StorageContext storageContext, Set<TblColRef> dimensions,
            Set<FunctionDesc> metrics, Candidate candidate) {
        val layoutCandidate = (NLayoutCandidate) candidate.getCapability().getSelectedCandidate();
        val prunedSegments = candidate.getQueryableSeg().getBatchSegments();
        val prunedPartitions = candidate.getPrunedPartitions();
        if (layoutCandidate.isEmptyCandidate()) {
            storageContext.setLayoutId(-1L);
            storageContext.setEmptyLayout(true);
            logger.info("The context {}, chose empty layout", storageContext.getCtxId());
            return;
        }
        LayoutEntity layout = layoutCandidate.getLayoutEntity();
        storageContext.setCandidate(layoutCandidate);
        storageContext.setDimensions(dimensions);
        storageContext.setMetrics(metrics);
        storageContext.setLayoutId(layout.getId());
        storageContext.setPrunedSegments(prunedSegments);
        storageContext.setPrunedPartitions(prunedPartitions);
        logger.info("The context {}, chosen model: {}, its join: {}, layout: {}, dimensions: {}, measures: {}, " //
                + "segments: {}", storageContext.getCtxId(), layout.getModel().getAlias(),
                layout.getModel().getJoinsGraph(), layout.getId(), layout.getOrderedDimensions(),
                layout.getOrderedMeasures(), candidate.getQueryableSeg().getPrunedSegmentIds(true));
    }

    private static void buildHybridStorageContext(StorageContext context, Set<TblColRef> dimensions,
            Set<FunctionDesc> metrics, Candidate candidate) {
        context.setPrunedStreamingSegments(candidate.getQueryableSeg().getStreamingSegments());
        NLayoutCandidate streamingCandidate = (NLayoutCandidate) candidate.getCapability()
                .getSelectedStreamingCandidate();
        context.setStreamingCandidate(streamingCandidate);
        if (streamingCandidate == null || streamingCandidate.isEmptyCandidate()) {
            context.setStreamingLayoutId(-1L);
        } else {
            context.setStreamingLayoutId(streamingCandidate.getLayoutEntity().getId());
        }

        List<NDataSegment> prunedSegments = candidate.getQueryableSeg().getBatchSegments();
        NLayoutCandidate batchCandidate = (NLayoutCandidate) candidate.getCapability().getSelectedCandidate();
        if ((batchCandidate == null && streamingCandidate == NLayoutCandidate.EMPTY)
                || (streamingCandidate == null && batchCandidate == NLayoutCandidate.EMPTY)) {
            throw new NoStreamingRealizationFoundException(ServerErrorCode.STREAMING_MODEL_NOT_FOUND,
                    String.format(Locale.ROOT, MsgPicker.getMsg().getNoStreamingModelFound()));
        }

        if (batchCandidate == NLayoutCandidate.EMPTY && streamingCandidate == NLayoutCandidate.EMPTY) {
            context.setLayoutId(-1L);
            context.setStreamingLayoutId(-1L);
            context.setEmptyLayout(true);
            logger.info("The context {}, chose empty layout", context.getCtxId());
            return;
        }

        if (differentTypeofIndex(batchCandidate, streamingCandidate)) {
            context.setLayoutId(null);
            context.setStreamingLayoutId(null);
            context.setEmptyLayout(true);
            logger.error("The index types of streaming model and batch model are different, "
                    + "and this situation is not supported yet.");
            throw new NoStreamingRealizationFoundException(ServerErrorCode.STREAMING_MODEL_NOT_FOUND,
                    String.format(Locale.ROOT, MsgPicker.getMsg().getNoStreamingModelFound()));
        }

        NDataModel model = candidate.getRealization().getModel();

        context.setCandidate(batchCandidate);
        context.setDimensions(dimensions);
        context.setMetrics(metrics);
        context.setLayoutId(batchCandidate == null ? -1L : batchCandidate.getLayoutEntity().getId());
        context.setPrunedSegments(prunedSegments);
        context.setPrunedPartitions(candidate.getPrunedPartitions());
        if (batchCandidate != null && !batchCandidate.isEmptyCandidate()) {
            LayoutEntity layout = batchCandidate.getLayoutEntity();
            logger.info("The context {}, chosen model: {}, its join: {}, " //
                    + "batch layout: {}, batch layout dimensions: {}, " //
                    + "batch layout measures: {}, batch segments: {}", context.getCtxId(), model.getAlias(),
                    model.getJoinsGraph(), layout.getId(), layout.getOrderedDimensions(), layout.getOrderedMeasures(),
                    candidate.getQueryableSeg().getPrunedSegmentIds(true));
        }

        if (streamingCandidate != null && !streamingCandidate.isEmptyCandidate()) {
            LayoutEntity cuboidLayout = streamingCandidate.getLayoutEntity();
            logger.info("The context {}, chosen model: {}, its join: {}, " //
                    + "streaming layout: {}, streaming layout dimensions: {},  " //
                    + "streaming layout measures: {}, streaming segments: {}", context.getCtxId(), model.getAlias(),
                    model.getJoinsGraph(), cuboidLayout.getId(), cuboidLayout.getOrderedDimensions(),
                    cuboidLayout.getOrderedMeasures(), candidate.getQueryableSeg().getPrunedSegmentIds(false));
        }
    }

    private static boolean differentTypeofIndex(NLayoutCandidate batchLayout, NLayoutCandidate streamLayout) {
        if (batchLayout == null || batchLayout.isEmptyCandidate()) {
            return false;
        }
        if (streamLayout == null || streamLayout.isEmptyCandidate()) {
            return false;
        }
        return batchLayout.getLayoutEntity().getIndex().isTableIndex() != streamLayout.getLayoutEntity().getIndex()
                .isTableIndex();
    }

    private static void buildDimensionsAndMetrics(OlapContext context, Collection<TblColRef> dimensions,
            Collection<FunctionDesc> metrics) {
        SQLDigest sqlDigest = context.getSQLDigest();
        IRealization realization = context.getRealization();
        for (FunctionDesc func : sqlDigest.getAggregations()) {
            if (!func.isDimensionAsMetric() && !func.isGrouping()) {
                // use the FunctionDesc from cube desc as much as possible, that has more info such as HLLC precision

                if (FunctionDesc.FUNC_INTERSECT_COUNT.equalsIgnoreCase(func.getExpression())) {
                    realization.getMeasures().stream()
                            .filter(measureDesc -> measureDesc.getFunction().getReturnType().equals("bitmap") && func
                                    .getParameters().get(0).equals(measureDesc.getFunction().getParameters().get(0)))
                            .forEach(measureDesc -> metrics.add(measureDesc.getFunction()));
                    dimensions.add(func.getParameters().get(1).getColRef());
                } else if (FunctionDesc.FUNC_BITMAP_UUID.equalsIgnoreCase(func.getExpression())
                        || FunctionDesc.FUNC_BITMAP_BUILD.equalsIgnoreCase(func.getExpression())) {
                    realization.getMeasures().stream()
                            .filter(measureDesc -> measureDesc.getFunction().getReturnType().equals("bitmap") && func
                                    .getParameters().get(0).equals(measureDesc.getFunction().getParameters().get(0)))
                            .forEach(measureDesc -> metrics.add(measureDesc.getFunction()));
                } else {
                    FunctionDesc aggrFuncFromDataflowDesc = realization.findAggrFunc(func);
                    metrics.add(aggrFuncFromDataflowDesc);
                }
            } else if (func.isDimensionAsMetric()) {
                FunctionDesc funcUsedDimenAsMetric = findAggrFuncFromRealization(func, realization);
                dimensions.addAll(funcUsedDimenAsMetric.getColRefs());

                Set<TblColRef> groupByCols = Sets.newLinkedHashSet(sqlDigest.getGroupByColumns());
                groupByCols.addAll(funcUsedDimenAsMetric.getColRefs());
                sqlDigest.setGroupByColumns(Lists.newArrayList(groupByCols));
            }
        }

        if (sqlDigest.isRawQuery) {
            dimensions.addAll(sqlDigest.getAllColumns());
        } else {
            dimensions.addAll(sqlDigest.getGroupByColumns());
            dimensions.addAll(sqlDigest.getFilterColumns());
        }
    }

    private static FunctionDesc findAggrFuncFromRealization(FunctionDesc aggrFunc, IRealization realization) {
        for (MeasureDesc measure : realization.getMeasures()) {
            if (measure.getFunction().equals(aggrFunc))
                return measure.getFunction();
        }
        return aggrFunc;
    }

    private static String toErrorMsg(OlapContext ctx) {
        StringBuilder buf = new StringBuilder("OlapContext");
        RealizationCheck checkResult = ctx.getRealizationCheck();
        for (List<RealizationCheck.IncapableReason> reasons : checkResult.getModelIncapableReasons().values()) {
            for (RealizationCheck.IncapableReason reason : reasons) {
                buf.append(", ").append(reason);
            }
        }
        buf.append(", ").append(ctx.getFirstTableScan());
        ctx.getJoins().forEach(join -> buf.append(", ").append(join));
        return buf.toString();
    }

    private static String helpfulMessageForUser(OlapContext ctx) {
        StringBuilder buf = new StringBuilder(ctx.toHumanReadString());
        buf.append(System.getProperty("line.separator"));
        if (ctx.getJoinsGraph() != null) {
            buf.append("  Join graph : ").append(ctx.getJoinsGraph().toString())
                    .append(System.getProperty("line.separator"));
        }
        buf.append("  Incapable message : ");
        for (List<RealizationCheck.IncapableReason> reasons : ctx.getRealizationCheck().getModelIncapableReasons()
                .values()) {
            for (RealizationCheck.IncapableReason reason : reasons) {
                buf.append(reason).append(", ");
            }
        }
        return buf.toString();
    }

    public static Map<String, String> matchJoins(NDataModel model, OlapContext ctx, boolean partialMatchInnerJoin,
            boolean partialMatchNonEquiJoin) {
        Map<String, String> matchedAliasMap = Maps.newHashMap();
        TableRef firstTable = ctx.getFirstTableScan().getTableRef();
        boolean matched;

        if (ctx.isFirstTableLookupTableInModel(model)) {
            // one lookup table, the matchedAliasMap was set to the mapping of tableAlias => modelAlias
            String modelAlias = model.findFirstTable(firstTable.getTableIdentity()).getAlias();
            matchedAliasMap = ImmutableMap.of(firstTable.getAlias(), modelAlias);
            matched = true;
            logger.info("Context fact table {} matched lookup table in model {}",
                    ctx.getFirstTableScan().getTableName(), model);
        } else if (ctx.getJoins().size() != ctx.getAllTableScans().size() - 1) {
            // has hanging tables
            ctx.getRealizationCheck().addModelIncapableReason(model,
                    RealizationCheck.IncapableReason.MODEL_BAD_JOIN_SEQUENCE);
            return new HashMap<>();
        } else {
            // normal join graph match
            if (ctx.getJoinsGraph() == null) {
                ctx.setJoinsGraph(new JoinsGraph(firstTable, ctx.getJoins()));
            }
            matched = ctx.getJoinsGraph().match(model.getJoinsGraph(), matchedAliasMap, partialMatchInnerJoin,
                    partialMatchNonEquiJoin);
            if (!matched) {
                KylinConfig kylinConfig = KylinConfig.getInstanceFromEnv();
                if (kylinConfig.isJoinMatchOptimizationEnabled()) {
                    logger.info(
                            "Query match join with join match optimization mode, trying to match with newly rewrite join graph.");
                    ctx.matchJoinWithFilterTransformation();
                    ctx.matchJoinWithEnhancementTransformation();
                    matched = ctx.getJoinsGraph().match(model.getJoinsGraph(), matchedAliasMap, partialMatchInnerJoin,
                            partialMatchNonEquiJoin);
                    logger.info("Match result for match join with join match optimization mode is: {}", matched);
                }
                logger.debug(
                        "Context [{}] join graph missed model {}, model join graph {}, unmatched model join graph {}, unmatched olap join graph {}",
                        ctx.getId(), model.getAlias(), model.getJoinsGraph(),
                        ctx.getJoinsGraph().unmatched(model.getJoinsGraph()),
                        model.getJoinsGraph().unmatched(ctx.getJoinsGraph()));
            }
        }

        if (!matched) {
            ctx.getRealizationCheck().addModelIncapableReason(model,
                    RealizationCheck.IncapableReason.MODEL_UNMATCHED_JOIN);
            return new HashMap<>();
        }
        ctx.getRealizationCheck().addCapableModel(model, matchedAliasMap);
        return matchedAliasMap;
    }

    private static Multimap<NDataModel, IRealization> makeOrderedModelMap(OlapContext context) {
        if (context.getModelAlias() != null) {
            logger.info("The query is bounded to a certain model({}/{}).", context.getOlapSchema().getProjectName(),
                    context.getModelAlias());
        }

        KylinConfig kylinConfig = context.getOlapSchema().getConfig();
        String project = context.getOlapSchema().getProjectName();
        String factTable = context.getFirstTableScan().getOlapTable().getTableName();
        Set<IRealization> realizations = NProjectManager.getInstance(kylinConfig).getRealizationsByTable(project,
                factTable);

        final Multimap<NDataModel, IRealization> mapModelToRealizations = HashMultimap.create();
        boolean streamingEnabled = kylinConfig.streamingEnabled();
        for (IRealization real : realizations) {
            boolean skip = false;
            if (!real.isOnline()) {
                skip = true;
                logger.warn("Offline model({}/{}) with fact table {} cannot be queried.", project, real, factTable);
            } else if (isModelViewBounded(context, real)) {
                skip = true;
            } else if (omitFusionModel(streamingEnabled, real)) {
                skip = true;
                logger.info("Fusion model({}/{}) is skipped.", project, real.getUuid());
            }
            if (skip) {
                continue;
            }
            mapModelToRealizations.put(real.getModel(), real);
        }

        if (mapModelToRealizations.isEmpty()) {
            logger.error("No realization found for project {} with fact table {}", project, factTable);
        }

        useOnlyModelsInPriorities(project, mapModelToRealizations);
        return mapModelToRealizations;
    }

    private static void useOnlyModelsInPriorities(String project,
            Multimap<NDataModel, IRealization> modelToRealizations) {
        KylinConfig projectConfig = NProjectManager.getProjectConfig(project);
        String[] modelPriorities = QueryContext.current().getModelPriorities();
        if (!projectConfig.useOnlyModelsInPriorities() || modelPriorities.length == 0) {
            return;
        }

        modelToRealizations.entries().removeIf(
                entry -> !Arrays.asList(modelPriorities).contains(entry.getKey().getAlias().toUpperCase(Locale.ROOT)));
        List<String> usedModels = modelToRealizations.keys().stream().map(NDataModel::getAlias)
                .collect(Collectors.toList());
        logger.info("Use only models in priorities: {}", usedModels);
    }

    /**
     * context is bound to a certain model (by model view)
     */
    private static boolean isModelViewBounded(OlapContext context, IRealization realization) {
        return context.getModelAlias() != null
                && !StringUtils.equalsIgnoreCase(realization.getModel().getAlias(), context.getModelAlias());
    }

    private static boolean omitFusionModel(boolean turnOnStreaming, IRealization real) {
        return !turnOnStreaming && real.getModel().isFusionModel();
    }
}
