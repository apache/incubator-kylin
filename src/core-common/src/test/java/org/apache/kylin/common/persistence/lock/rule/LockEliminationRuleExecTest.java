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

import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.MODEL;
import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.TABLE;

import java.util.Collections;
import java.util.List;

import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.persistence.lock.MemoryLockUtils;
import org.apache.kylin.common.persistence.lock.TransactionLock;
import org.apache.kylin.common.util.NLocalFileMetadataTestCase;
import org.apache.kylin.guava30.shaded.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LockEliminationRuleExecTest extends NLocalFileMetadataTestCase {

    @BeforeEach
    public void setup() throws Exception {
        createTestMetadata();
    }

    @AfterEach
    public void teardown() {
        cleanupTestMetadata();
    }

    @Test
    void testProjectRule() {
        //              ProjectLock          ModuleLock        PathLock
        // Source:      default             TABLE             table1,table2
        // Expected:    default             NULL              NULL

        String project = "default";
        {
            List<TransactionLock> projectLock = MemoryLockUtils.getProjectLock(project);
            List<TransactionLock> moduleLocks = MemoryLockUtils.getModuleLocks(project, MODEL);
            List<TransactionLock> pathLocks = MemoryLockUtils.getPathLocks(
                    Lists.newArrayList("/default/table/db.table1.json", "/default/table/db.table2.json"));
            LockInfo lockInfo = LockEliminationRuleExec.getInstance().execute(project, projectLock, moduleLocks,
                    pathLocks);
            Assertions.assertFalse(lockInfo.getProjectLocks().isEmpty());
            Assertions.assertTrue(lockInfo.getModuleLocks().isEmpty());
            Assertions.assertTrue(lockInfo.getPathLocks().isEmpty());
            Assertions.assertEquals(project, lockInfo.getProjectLocks().get(0).transactionUnit());
        }
        {
            List<TransactionLock> projectLock = MemoryLockUtils.getProjectLock(project);
            List<TransactionLock> moduleLocks = MemoryLockUtils.getModuleLocks(project, TABLE);
            List<TransactionLock> pathLocks = MemoryLockUtils.getPathLocks(
                    Lists.newArrayList("/default/table/db.table1.json", "/default/table/db.table2.json"));
            LockInfo lockInfo = LockEliminationRuleExec.getInstance().execute(project, projectLock, moduleLocks,
                    pathLocks);
            Assertions.assertFalse(lockInfo.getProjectLocks().isEmpty());
            Assertions.assertTrue(lockInfo.getModuleLocks().isEmpty());
            Assertions.assertTrue(lockInfo.getPathLocks().isEmpty());
            Assertions.assertEquals(project, lockInfo.getProjectLocks().get(0).transactionUnit());
        }
        {
            List<TransactionLock> projectLock = MemoryLockUtils.getProjectLock(project);
            List<TransactionLock> moduleLocks = Collections.emptyList();
            List<TransactionLock> pathLocks = Collections.emptyList();
            LockInfo lockInfo = LockEliminationRuleExec.getInstance().execute(project, projectLock, moduleLocks,
                    pathLocks);
            Assertions.assertFalse(lockInfo.getProjectLocks().isEmpty());
            Assertions.assertTrue(lockInfo.getModuleLocks().isEmpty());
            Assertions.assertTrue(lockInfo.getPathLocks().isEmpty());
            Assertions.assertEquals(project, lockInfo.getProjectLocks().get(0).transactionUnit());
        }
    }

    @Test
    void testModuleRule() {
        //              ProjectLock      ModuleLock        PathLock
        // Source:      NULL             TABLE             table1,table2
        // Expected:    NULL             TABLE             NULL
        String project = "default";

        {
            List<TransactionLock> projectLock = Collections.emptyList();
            List<TransactionLock> moduleLocks = MemoryLockUtils.getModuleLocks(project, TABLE);
            List<TransactionLock> pathLocks = MemoryLockUtils.getPathLocks(
                    Lists.newArrayList("/default/table/db.table1.json", "/default/table/db.table2.json"));
            LockInfo lockInfo = LockEliminationRuleExec.getInstance().execute(project, projectLock, moduleLocks,
                    pathLocks);
            Assertions.assertTrue(lockInfo.getProjectLocks().isEmpty());
            Assertions.assertFalse(lockInfo.getModuleLocks().isEmpty());
            Assertions.assertTrue(lockInfo.getPathLocks().isEmpty());
            Assertions.assertEquals(TABLE, lockInfo.getModuleLocks().get(0).moduleEnum());
        }
        {
            List<TransactionLock> projectLock = Collections.emptyList();
            List<TransactionLock> moduleLocks = MemoryLockUtils.getModuleLocks(project, MODEL);
            List<TransactionLock> pathLocks = MemoryLockUtils.getPathLocks(
                    Lists.newArrayList("/default/table/db.table1.json", "/default/table/db.table2.json"));
            LockInfo lockInfo = LockEliminationRuleExec.getInstance().execute(project, projectLock, moduleLocks,
                    pathLocks);
            Assertions.assertTrue(lockInfo.getProjectLocks().isEmpty());
            Assertions.assertFalse(lockInfo.getModuleLocks().isEmpty());
            Assertions.assertFalse(lockInfo.getPathLocks().isEmpty());
            Assertions.assertEquals(MODEL, lockInfo.getModuleLocks().get(0).moduleEnum());
            Assertions.assertEquals(2, lockInfo.getPathLocks().size());
        }
    }

    @Test
    void testPathRule() {
        //              ProjectLock      ModuleLock        PathLock
        // Source:      NULL             MODEL             table1,table2
        // Expected:    NULL             MODEL,TABLE       NULL
        String project = "default";
        KylinConfig.getInstanceFromEnv().setProperty("kylin.env.lock-remove-max-size", "1");
        {
            List<TransactionLock> projectLock = Collections.emptyList();
            List<TransactionLock> moduleLocks = MemoryLockUtils.getModuleLocks(project, MODEL);
            List<TransactionLock> pathLocks = MemoryLockUtils.getPathLocks(
                    Lists.newArrayList("/default/table/db.table1.json", "/default/table/db.table2.json"));
            LockInfo lockInfo = LockEliminationRuleExec.getInstance().execute(project, projectLock, moduleLocks,
                    pathLocks);
            Assertions.assertTrue(lockInfo.getProjectLocks().isEmpty());
            Assertions.assertFalse(lockInfo.getModuleLocks().isEmpty());
            Assertions.assertTrue(lockInfo.getPathLocks().isEmpty());

            Assertions.assertEquals(2, lockInfo.getModuleLocks().size());
        }
        {
            List<TransactionLock> projectLock = Collections.emptyList();
            List<TransactionLock> moduleLocks = MemoryLockUtils.getModuleLocks(project, MODEL);
            List<TransactionLock> pathLocks = MemoryLockUtils
                    .getPathLocks(Lists.newArrayList("/default/table/db.table1.json"));
            LockInfo lockInfo = LockEliminationRuleExec.getInstance().execute(project, projectLock, moduleLocks,
                    pathLocks);
            Assertions.assertTrue(lockInfo.getProjectLocks().isEmpty());
            Assertions.assertFalse(lockInfo.getModuleLocks().isEmpty());
            Assertions.assertFalse(lockInfo.getPathLocks().isEmpty());
        }
    }

}