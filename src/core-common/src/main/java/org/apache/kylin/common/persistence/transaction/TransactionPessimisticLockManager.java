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
package org.apache.kylin.common.persistence.transaction;

import static org.apache.kylin.common.persistence.lock.LockManagerInstance.INSTANCE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.kylin.common.persistence.lock.AbstractMemoryLock;
import org.apache.kylin.common.persistence.lock.ModuleLockEnum;
import org.apache.kylin.common.persistence.lock.ProjectLock;
import org.apache.kylin.common.persistence.lock.TransactionLock;
import org.apache.kylin.common.persistence.lock.TransactionLockManager;
import org.apache.kylin.common.persistence.lock.TransactionTempLock;

public class TransactionPessimisticLockManager implements TransactionLockManager {

    @Override
    public TransactionLock getLock(String project, boolean readonly) {
        return INSTANCE.getProjectLock(project).getReadOrWriteLock(false);
    }

    @Override
    public TransactionLock getTempLock(String project, boolean readonly) {
        AbstractMemoryLock lock = INSTANCE.getProjectLock(project);
        return new TransactionTempLock(lock.writeLock());
    }

    @Override
    public void removeLock(String project) {
        AbstractMemoryLock lock = INSTANCE.getProjectLock(project);
        if (lock != null) {
            synchronized (UnitOfWork.class) {
                AtomicLong atomicLong = lock.getTranHoldCount();
                if (atomicLong.decrementAndGet() == 0) {
                    INSTANCE.removeProjectLock(project);
                }
            }
        }
        INSTANCE.checkProjectLockSize();
    }

    public Map<String, ProjectLock> getProjectLocksForRead() {
        return Collections.unmodifiableMap(INSTANCE.listAllProjectLock());
    }

    @Override
    public TransactionLock getProjectLock(String projectName) {
        return INSTANCE.getProjectLock(projectName).getReadOrWriteLock(false);
    }

    @Override
    public TransactionLock getModuleLock(String projectName, ModuleLockEnum moduleName) {
        return INSTANCE.getModuleLock(projectName, moduleName).getReadOrWriteLock(false);
    }

    @Override
    public List<TransactionLock> getPathLock(String pathName, boolean readOnly) {
        return INSTANCE.getPathLock(pathName).stream().map(pathLock -> pathLock.getReadOrWriteLock(readOnly))
                .collect(Collectors.toList());
    }
}
