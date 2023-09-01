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
package org.apache.kylin.common.persistence.lock;

import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.ResourceEnum.COMPUTE_COLUMN_EXPRESSION;
import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.ResourceEnum.COMPUTE_COLUMN_NAME;
import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.ResourceEnum.DATAFLOW;
import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.ResourceEnum.DATAFLOW_DETAILS;
import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.ResourceEnum.EXECUTE;
import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.ResourceEnum.INDEX_PLAN;
import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.ResourceEnum.MODEL_DESC;
import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.ResourceEnum.TABLE_EXD;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.guava30.shaded.common.collect.Lists;
import org.apache.kylin.guava30.shaded.common.collect.Maps;

public enum ModuleLockEnum {
    JOB(EXECUTE.value()), //
    MODEL(MODEL_DESC.value(), DATAFLOW.value(), DATAFLOW_DETAILS.value(), INDEX_PLAN.value()), //
    TABLE(ResourceEnum.TABLE.value(), TABLE_EXD.value()), //
    HISTORY_SOURCE_USAGE(ResourceEnum.HISTORY_SOURCE_USAGE.value()), //
    PROJECT(ResourceEnum.PROJECT.value()), //
    COMPUTE_COLUMN(COMPUTE_COLUMN_NAME.value(), COMPUTE_COLUMN_EXPRESSION.value()), //
    DEFAULT();

    private final List<String> moduleNames;

    public List<String> getModuleNames() {
        return moduleNames;
    }

    ModuleLockEnum(String... moduleNames) {
        this.moduleNames = Lists.newArrayList(moduleNames);
    }

    private static final Map<String, ModuleLockEnum> MODULE_LOCK_ENUMS = Maps.newHashMap();

    static {
        Arrays.stream(values()).forEach(moduleLockEnum -> moduleLockEnum.moduleNames
                .forEach(module -> MODULE_LOCK_ENUMS.put(module, moduleLockEnum)));
    }

    public static ModuleLockEnum getModuleEnum(String moduleName) {
        if (StringUtils.isBlank(moduleName)) {
            return DEFAULT;
        }
        return MODULE_LOCK_ENUMS.getOrDefault(moduleName, DEFAULT);
    }

    public enum ResourceEnum {
        EXECUTE,
        MODEL_DESC,
        DATAFLOW,
        DATAFLOW_DETAILS,
        INDEX_PLAN,
        TABLE,
        TABLE_EXD,
        HISTORY_SOURCE_USAGE,
        PROJECT,
        COMPUTE_COLUMN_NAME,
        COMPUTE_COLUMN_EXPRESSION;

        public String value() {
            return name().toLowerCase();
        }
    }
}
