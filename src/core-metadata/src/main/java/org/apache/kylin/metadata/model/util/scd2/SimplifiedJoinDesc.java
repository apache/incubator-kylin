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

import java.util.List;

import org.apache.kylin.metadata.model.JoinDesc;
import org.apache.kylin.metadata.model.NonEquiJoinCondition;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimplifiedJoinDesc extends JoinDesc.NonEquivJoinDesc {

    private static final long serialVersionUID = 3422512377209976139L;

    @JsonProperty("simplified_non_equi_join_conditions")
    private List<NonEquiJoinCondition.SimplifiedJoinCondition> simplifiedNonEquiJoinConditions;

    /**
     * non-equi join condition to fks,pks
     */
    public void simplifyEqualJoinPairs(List<NonEquiJoinCondition.SimplifiedJoinCondition> simplifiedNeqConditions) {

        int len = simplifiedNeqConditions.size();

        String[] fks = new String[len];
        String[] pks = new String[len];

        for (int i = 0; i < len; i++) {
            fks[i] = simplifiedNeqConditions.get(i).getForeignKey();
            pks[i] = simplifiedNeqConditions.get(i).getPrimaryKey();
        }

        this.setForeignKey(fks);
        this.setPrimaryKey(pks);
    }

}
