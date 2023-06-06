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
package org.apache.kylin.rest.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.kylin.common.exception.KylinException;
import org.apache.kylin.common.exception.code.ErrorCodeServer;
import org.apache.kylin.common.util.JsonUtil;
import org.apache.kylin.guava30.shaded.common.base.Preconditions;
import org.apache.kylin.metadata.model.JoinDesc;
import org.apache.kylin.metadata.model.JoinTableDesc;
import org.apache.kylin.metadata.model.util.scd2.Scd2Simplifier;
import org.apache.kylin.metadata.model.util.scd2.SimplifiedJoinDesc;
import org.apache.kylin.metadata.model.util.scd2.SimplifiedJoinTableDesc;

public class SCD2SimplificationConvertUtil {
    private SCD2SimplificationConvertUtil() {
    }

    /**
     * fill simplified cond according to joinTables
     * return true if is scd2 cond
     * @param simplifiedJoinTableDescs
     * @param joinTables
     */
    private static void fillSimplifiedCond(@Nonnull List<SimplifiedJoinTableDesc> simplifiedJoinTableDescs,
            @Nonnull List<JoinTableDesc> joinTables) {
        Preconditions.checkNotNull(simplifiedJoinTableDescs);
        Preconditions.checkNotNull(joinTables);

        for (int i = 0; i < joinTables.size(); i++) {
            JoinTableDesc joinTableDesc = joinTables.get(i);
            JoinDesc joinDesc = joinTableDesc.getJoin();

            if (Objects.isNull(joinDesc.getNonEquiJoinCondition())) {
                continue;
            }

            try {
                SimplifiedJoinDesc convertedJoinDesc = Scd2Simplifier.INSTANCE.simplifyScd2Conditions(joinDesc);
                SimplifiedJoinDesc responseJoinDesc = simplifiedJoinTableDescs.get(i).getSimplifiedJoinDesc();
                responseJoinDesc
                        .setSimplifiedNonEquiJoinConditions(convertedJoinDesc.getSimplifiedNonEquiJoinConditions());
                responseJoinDesc.setForeignKey(convertedJoinDesc.getForeignKey());
                responseJoinDesc.setPrimaryKey(convertedJoinDesc.getPrimaryKey());
            } catch (KylinException e) {
                throw new KylinException(ErrorCodeServer.SCD2_MODEL_UNKNOWN_EXCEPTION,
                        "only support scd2 join condition");
            }
        }
    }

    /**
     * convert join tables to  simplified join tables,
     * and fill non-equi join cond
     * @param joinTables
     * @return
     */
    public static List<SimplifiedJoinTableDesc> simplifiedJoinTablesConvert(List<JoinTableDesc> joinTables) {
        if (Objects.isNull(joinTables)) {
            return Collections.emptyList();
        }

        List<SimplifiedJoinTableDesc> simplifiedJoinTableDescs;
        try {
            simplifiedJoinTableDescs = Arrays.asList(
                    JsonUtil.readValue(JsonUtil.writeValueAsString(joinTables), SimplifiedJoinTableDesc[].class));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        fillSimplifiedCond(simplifiedJoinTableDescs, joinTables);

        return simplifiedJoinTableDescs;

    }

    /**
     * convert simplifiedJoinTables to join tables
     * @param simplifiedJoinTableDescs
     * @return
     */
    public static List<JoinTableDesc> convertSimplified2JoinTables(
            List<SimplifiedJoinTableDesc> simplifiedJoinTableDescs) {
        if (Objects.isNull(simplifiedJoinTableDescs)) {
            return Collections.emptyList();
        }
        List<JoinTableDesc> joinTableDescs;
        try {
            joinTableDescs = Arrays.asList(
                    JsonUtil.readValue(JsonUtil.writeValueAsString(simplifiedJoinTableDescs), JoinTableDesc[].class));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return joinTableDescs;

    }

    public static List<JoinTableDesc> deepCopyJoinTables(List<JoinTableDesc> joinTables) {

        return convertSimplified2JoinTables(simplifiedJoinTablesConvert(joinTables));

    }
}
