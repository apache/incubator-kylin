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
import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.persistence.lock.MemoryLockUtils;
import org.apache.kylin.common.persistence.lock.ModuleLockEnum;
import org.apache.kylin.common.persistence.lock.TransactionLock;
import org.apache.kylin.guava30.shaded.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * If the number of path locks exceeds the maximum size
 * the path locks will be cleared and the corresponding module locks will be added.
 */
@Slf4j
public class PathEliminationRule extends LockEliminationRule {

    @Override
    protected void doHandle(LockInfo lockInfo) {
        if (lockInfo.getPathLocks().isEmpty()) {
            return;
        }

        int maxSize = KylinConfig.getInstanceFromEnv().getLockRemoveMaxSize();
        Map<ModuleLockEnum, List<TransactionLock>> modulePathMap = lockInfo.getPathLocks().stream()
                .collect(Collectors.groupingBy(TransactionLock::moduleEnum));

        List<ModuleLockEnum> moduleNames = Lists.newArrayList();
        modulePathMap.forEach((module, pathLocks) -> {
            if (pathLocks.size() > maxSize) {
                pathLocks.clear();
                moduleNames.add(module);
            }
        });

        if (moduleNames.isEmpty()) {
            return;
        }
        log.info("add new module locks: [{}]", StringUtils.join(moduleNames, ","));
        List<TransactionLock> newPathLocks = modulePathMap.values().stream().filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream).collect(Collectors.toList());
        List<TransactionLock> newModuleLocks = MemoryLockUtils.getModuleLocks(lockInfo.getProject(), moduleNames);

        lockInfo.setPathLocks(newPathLocks);
        lockInfo.getModuleLocks().addAll(newModuleLocks);
    }
}
