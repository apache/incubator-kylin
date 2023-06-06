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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.calcite.sql.SqlKind;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kylin.guava30.shaded.common.collect.ImmutableSet;
import org.apache.kylin.guava30.shaded.common.collect.Lists;
import org.apache.kylin.guava30.shaded.common.collect.Maps;
import org.apache.kylin.guava30.shaded.common.collect.Sets;
import org.apache.kylin.metadata.model.NDataModel;

/**
 * do something check on scd2 condition
 */
public class SCD2CondChecker {

    public static final SCD2CondChecker INSTANCE = new SCD2CondChecker();
    private static final Set<Set<SqlKind>> SCD_2_JOIN_PAIRS = ImmutableSet.of(
            Sets.newHashSet(SqlKind.GREATER_THAN, SqlKind.LESS_THAN_OR_EQUAL),
            Sets.newHashSet(SqlKind.GREATER_THAN_OR_EQUAL, SqlKind.LESS_THAN_OR_EQUAL),
            Sets.newHashSet(SqlKind.GREATER_THAN_OR_EQUAL, SqlKind.LESS_THAN));

    /**
     * SCD2 must have  equi condition
     */
    public boolean checkSCD2EquiJoinCond(String[] fks, String[] pks) {
        return fks.length > 0 && fks.length == pks.length;
    }

    public boolean isScd2Model(NDataModel nDataModel) {
        if (nDataModel.getJoinTables().stream().noneMatch(joinTableDesc -> joinTableDesc.getJoin().isNonEquiJoin())) {
            return false;
        }

        return nDataModel.getJoinTables().stream() //
                .filter(joinTableDesc -> joinTableDesc.getJoin().isNonEquiJoin())
                .allMatch(joinTableDesc -> Scd2Simplifier.INSTANCE.isValidScd2JoinDesc(joinTableDesc.getJoin()));
    }

    public boolean checkFkPkPairUnique(SimplifiedJoinDesc joinDesc) {
        return checkFkPkPairUnique(Scd2Simplifier.INSTANCE.buildAndCheckEqualConditions(joinDesc),
                joinDesc.getSimplifiedNonEquiJoinConditions());
    }

    public boolean checkSCD2NonEquiJoinCondPair(final List<SimplifiedJoinCondition> simplified) {
        if (CollectionUtils.isEmpty(simplified)) {
            return false;
        }

        int size = simplified.size();

        if (size % 2 != 0) {
            return false;
        }

        Map<String, List<SqlKind>> mapping = Maps.newHashMap();
        for (SimplifiedJoinCondition cond : simplified) {
            if (!checkSCD2SqlOp(cond.getOp())) {
                return false;
            }

            mapping.putIfAbsent(cond.getForeignKey(), Lists.newArrayList());
            mapping.putIfAbsent(cond.getPrimaryKey(), Lists.newArrayList());
            mapping.get(cond.getForeignKey()).add(cond.getOp());
            mapping.get(cond.getPrimaryKey()).add(inverse(cond.getOp()));
        }
        mapping.entrySet().removeIf(entry -> entry.getValue().size() != 2);

        return mapping.values().stream().allMatch(this::isValidOperatorPair);
    }

    public static SqlKind inverse(SqlKind kind) {
        switch (kind) {
        case GREATER_THAN:
            return SqlKind.LESS_THAN;
        case LESS_THAN:
            return SqlKind.GREATER_THAN;
        case GREATER_THAN_OR_EQUAL:
            return SqlKind.LESS_THAN_OR_EQUAL;
        case LESS_THAN_OR_EQUAL:
            return SqlKind.GREATER_THAN_OR_EQUAL;
        default:
            return null;
        }
    }

    private boolean isValidOperatorPair(List<SqlKind> ops) {
        if (ops.size() != 2 || ops.get(0) == ops.get(1)) {
            return false;
        }
        return SCD_2_JOIN_PAIRS.contains(Sets.newHashSet(ops));
    }

    boolean checkFkPkPairUnique(List<SimplifiedJoinCondition> equiFkPks, List<SimplifiedJoinCondition> nonEquiFkPks) {
        List<SimplifiedJoinCondition> allFkPks = new ArrayList<>();
        allFkPks.addAll(equiFkPks);
        allFkPks.addAll(nonEquiFkPks);

        Set<String> pairSet = new HashSet<>();
        for (SimplifiedJoinCondition allFkPk : allFkPks) {
            String[] joinKeys = new String[] { allFkPk.getPrimaryKey(), allFkPk.getForeignKey() };
            Arrays.sort(joinKeys);
            String key = String.join(",", joinKeys);
            if (pairSet.contains(key)) {
                return false;
            }
            pairSet.add(key);
        }
        return true;
    }

    private boolean checkSCD2SqlOp(SqlKind op) {
        if (Objects.isNull(op)) {
            return false;
        }

        switch (op) {
        case GREATER_THAN_OR_EQUAL:
        case LESS_THAN_OR_EQUAL:
        case GREATER_THAN:
        case LESS_THAN:
            return true;
        default:
            return false;
        }
    }
}
