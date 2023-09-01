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
package org.apache.kylin.common.persistence.lock.rule;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.kylin.common.persistence.lock.ModuleLockEnum;
import org.apache.kylin.common.persistence.lock.TransactionLock;

import lombok.extern.slf4j.Slf4j;

/**
 * If the path locks are included by module locks, the path locks will be cleared.
 */
@Slf4j
public class ModuleEliminationRule extends LockEliminationRule {
    @Override
    protected void doHandle(LockInfo lockInfo) {
        if (lockInfo.getPathLocks().isEmpty() || lockInfo.getModuleLocks().isEmpty()) {
            return;
        }

        List<ModuleLockEnum> modules = lockInfo.getModuleLocks().stream().map(TransactionLock::moduleEnum)
                .collect(Collectors.toList());
        Map<ModuleLockEnum, List<TransactionLock>> modulePathMap = lockInfo.getPathLocks().stream()
                .collect(Collectors.groupingBy(TransactionLock::moduleEnum));
        modulePathMap.forEach((module, pathLocks) -> {
            if (modules.contains(module)) {
                pathLocks.clear();
                log.info("path lock has been included by module lock [{}]", module);
            }
        });
        List<TransactionLock> newPathLocks = modulePathMap.values().stream().filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream).collect(Collectors.toList());
        lockInfo.setPathLocks(newPathLocks);
    }
}
