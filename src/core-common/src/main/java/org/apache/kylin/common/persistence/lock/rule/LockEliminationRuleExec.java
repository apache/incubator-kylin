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

import org.apache.kylin.common.persistence.lock.TransactionLock;

public class LockEliminationRuleExec {
    private final LockEliminationRule ruleChain;

    private LockEliminationRuleExec() {
        // projectRule -> moduleRule -> pathRule
        ProjectEliminationRule projectRule = new ProjectEliminationRule();
        ModuleEliminationRule moduleRule = new ModuleEliminationRule();
        PathEliminationRule pathRule = new PathEliminationRule();
        moduleRule.setNext(pathRule);
        projectRule.setNext(moduleRule);
        ruleChain = projectRule;
    }

    private static final class InstanceHolder {
        static final LockEliminationRuleExec INSTANCE = new LockEliminationRuleExec();
    }

    public static LockEliminationRuleExec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Execute the lock elimination rule chain.
     */
    public LockInfo execute(String project, List<TransactionLock> projectLocks, List<TransactionLock> moduleLocks,
            List<TransactionLock> pathLocks) {
        LockInfo lockInfo = new LockInfo(project, projectLocks, moduleLocks, pathLocks);
        if (projectLocks.isEmpty() && moduleLocks.isEmpty() && pathLocks.isEmpty()) {
            return lockInfo;
        }
        ruleChain.handle(lockInfo);
        return lockInfo;
    }
}
