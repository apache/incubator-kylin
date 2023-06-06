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
package org.apache.kylin.metadata.model.util.scd2;

import static org.apache.kylin.metadata.model.NonEquiJoinCondition.SimplifiedJoinCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.calcite.sql.SqlKind;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.common.exception.KylinException;
import org.apache.kylin.common.exception.code.ErrorCodeServer;
import org.apache.kylin.common.util.Pair;
import org.apache.kylin.guava30.shaded.common.base.Preconditions;
import org.apache.kylin.guava30.shaded.common.collect.Lists;
import org.apache.kylin.metadata.model.JoinDesc;
import org.apache.kylin.metadata.model.NonEquiJoinCondition;
import org.apache.kylin.metadata.model.TblColRef;

/**
 * simplify non-equi-join
 */
public class Scd2Simplifier {

    public static final Scd2Simplifier INSTANCE = new Scd2Simplifier();

    /**
     * convert cond from origin model.
     * it may contain =,>=,< and so on
     *
     * if the condition is not SCD2, return null
     * @param joinDesc
     * @return
     */
    public SimplifiedJoinDesc simplifyScd2Conditions(@Nullable JoinDesc joinDesc) {
        if (Objects.isNull(joinDesc)) {
            return null;
        }

        List<SimplifiedJoinCondition> equalJoinPairs = buildAndCheckEqualConditions(joinDesc);
        List<SimplifiedJoinCondition> nonEqualJoinPairs = buildAndCheckNeqConditions(joinDesc);

        // check all the join-condition paris are unique
        SCD2CondChecker.INSTANCE.checkFkPkPairUnique(equalJoinPairs, nonEqualJoinPairs);
        SimplifiedJoinDesc convertedJoinDesc = new SimplifiedJoinDesc();
        convertedJoinDesc.setSimplifiedNonEquiJoinConditions(nonEqualJoinPairs);
        convertedJoinDesc.simplifyEqualJoinPairs(equalJoinPairs);
        return convertedJoinDesc;
    }

    private List<SimplifiedJoinCondition> buildAndCheckNeqConditions(JoinDesc joinDesc) {
        NonEquiJoinCondition nonEquiJoinCondition = joinDesc.getNonEquiJoinCondition();
        //null, or is not `and` cond
        if (Objects.isNull(nonEquiJoinCondition) || nonEquiJoinCondition.getOp() != SqlKind.AND) {
            throw new KylinException(ErrorCodeServer.SCD2_MODEL_CAN_ONLY_CONNECT_BY_AND);
        }
        List<SimplifiedJoinCondition> nonEqualJoinPairs = Lists.newArrayList();
        for (NonEquiJoinCondition nonEquivCond : nonEquiJoinCondition.getOperands()) {
            SimplifiedJoinCondition scd2Cond = simplifySCD2ChildCond(nonEquivCond, joinDesc);
            if (Objects.isNull(scd2Cond)) {
                throw new KylinException(ErrorCodeServer.SCD2_MODEL_CONTAINS_ILLEGAL_EXPRESSIONS);
            }
            nonEqualJoinPairs.add(scd2Cond);
        }

        if (!SCD2CondChecker.INSTANCE.checkSCD2NonEquiJoinCondPair(nonEqualJoinPairs)) {
            throw new KylinException(ErrorCodeServer.SCD2_CONDITION_MUST_APPEAR_IN_PAIRS);
        }

        if (CollectionUtils.isEmpty(nonEqualJoinPairs)) {
            throw new KylinException(ErrorCodeServer.SCD2_MODEL_REQUIRES_AT_LEAST_ONE_NON_EQUAL_CONDITION);
        }
        return nonEqualJoinPairs;
    }

    List<SimplifiedJoinCondition> buildAndCheckEqualConditions(JoinDesc joinDesc) {
        String[] fks = joinDesc.getForeignKey();
        String[] pks = joinDesc.getPrimaryKey();
        List<SimplifiedJoinCondition> simplifiedFksPks = new ArrayList<>();
        for (int i = 0; i < fks.length; i++) {
            simplifiedFksPks.add(new SimplifiedJoinCondition(fks[i], pks[i], SqlKind.EQUALS));
        }
        if (CollectionUtils.isEmpty(simplifiedFksPks)) {
            throw new KylinException(ErrorCodeServer.SCD2_MODEL_REQUIRES_AT_LEAST_ONE_EQUAL_CONDITION);
        }

        return simplifiedFksPks;
    }

    public TblColRef[] extractNeqFks(@Nonnull JoinDesc joinDesc) {
        Preconditions.checkNotNull(joinDesc, "joinDesc is null");

        List<TblColRef> fkList = simplifyScd2Conditions(joinDesc).getSimplifiedNonEquiJoinConditions().stream()
                .map(SimplifiedJoinCondition::getFk).distinct().collect(Collectors.toList());

        TblColRef[] fks = new TblColRef[fkList.size()];
        fkList.toArray(fks);
        return fks;
    }

    boolean isValidScd2JoinDesc(@Nullable JoinDesc joinDesc) {
        try {
            return simplifyScd2Conditions(joinDesc) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Simplify child condition, for example:
     * {@code (a>=b) and (a<c) } will be transformed to {@code {"a","b",">="}, {"a","c","<"}}
     */
    public SimplifiedJoinCondition simplifySCD2ChildCond(NonEquiJoinCondition neqCondition, JoinDesc joinDesc) {
        if (neqCondition.getType() != NonEquiJoinCondition.Type.EXPRESSION || neqCondition.getOperands().length != 2) {
            return null;
        }

        List<Pair<String, TblColRef>> scd2Pairs = Arrays.stream(neqCondition.getOperands())
                .map(nonEquiJoinConditionChild -> {
                    if (nonEquiJoinConditionChild.getOp() == SqlKind.CAST
                            && nonEquiJoinConditionChild.getOperands().length == 1) {
                        return nonEquiJoinConditionChild.getOperands()[0];
                    } else {
                        return nonEquiJoinConditionChild;
                    }
                })
                .filter(nonEquiJoinConditionChild -> nonEquiJoinConditionChild.getOperands().length == 0
                        || nonEquiJoinConditionChild.getType() == NonEquiJoinCondition.Type.COLUMN)
                .map(nonEquiJoinCondition1 -> new Pair<>(nonEquiJoinCondition1.getValue(),
                        nonEquiJoinCondition1.getColRef()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(scd2Pairs) || scd2Pairs.size() != 2) {
            return null;
        }

        // pk-fk should be consistent with the join table, just for display
        Pair<String, TblColRef> left = scd2Pairs.get(0);
        Pair<String, TblColRef> right = scd2Pairs.get(1);
        String[] fks = joinDesc.getForeignKey();
        if (fks.length == 0) {
            throw new KylinException(ErrorCodeServer.SCD2_MODEL_REQUIRES_AT_LEAST_ONE_NON_EQUAL_CONDITION);
        }
        String fkTableAlias = StringUtils.split(fks[0], '.')[0];
        String fkTableAliasOfNeqCondition = StringUtils.split(left.getFirst(), '.')[0];
        return fkTableAliasOfNeqCondition.equalsIgnoreCase(fkTableAlias)
                ? new SimplifiedJoinCondition(left.getKey(), left.getValue(), right.getKey(), right.getValue(),
                        neqCondition.getOp())
                : new SimplifiedJoinCondition(right.getKey(), right.getValue(), left.getKey(), left.getValue(),
                        SCD2CondChecker.inverse(neqCondition.getOp()));
    }
}
