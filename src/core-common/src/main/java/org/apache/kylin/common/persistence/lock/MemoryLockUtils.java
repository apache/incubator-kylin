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

import static org.apache.kylin.common.persistence.transaction.TransactionManagerInstance.INSTANCE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.persistence.RawResource;
import org.apache.kylin.common.persistence.ResourceStore;
import org.apache.kylin.common.persistence.RootPersistentEntity;
import org.apache.kylin.common.persistence.ThreadViewResourceStore;
import org.apache.kylin.common.persistence.metadata.JdbcMetadataStore;
import org.apache.kylin.common.persistence.transaction.UnitOfWork;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryLockUtils {

    private MemoryLockUtils() {
    }

    private static final MemoryLockGraph MEMORY_LOCK_GRAPH = new MemoryLockGraph();

    public static List<TransactionLock> getProjectLock(String project) {
        return Collections.singletonList(INSTANCE.getProjectLock(project));
    }

    public static List<TransactionLock> getModuleLocks(String project, ModuleLockEnum module) {
        return getModuleLocks(project, Collections.singletonList(module));
    }

    public static List<TransactionLock> getModuleLocks(String project, Collection<ModuleLockEnum> modules) {
        return modules.stream().filter(Objects::nonNull).map(module -> INSTANCE.getModuleLock(project, module))
                .distinct().collect(Collectors.toList());
    }

    public static List<TransactionLock> getPathLocks(String path) {
        return getPathLocks(path, false);
    }

    public static List<TransactionLock> getPathLocks(String path, boolean readOnly) {
        return getPathLocks(Collections.singletonList(path), readOnly);
    }

    public static List<TransactionLock> getPathLocks(Collection<String> paths) {
        return getPathLocks(paths, false);
    }

    public static List<TransactionLock> getPathLocks(Collection<String> paths, boolean readOnly) {
        return paths.stream().filter(StringUtils::isNotBlank).map(path -> INSTANCE.getPathLock(path, readOnly))
                .flatMap(List::stream).distinct().collect(Collectors.toList());
    }

    public static <T extends RootPersistentEntity> T doWithLock(T entity, String resPath, boolean readonly,
            ResourceStore store, Action<T> action) {
        if (isNoNeedToLock(readonly, store)) {
            return action.run();
        }
        if (entity == null) {
            MemoryLockUtils.lockAndRecord(resPath, null, readonly);
            return action.run();
        }

        List<String> lockPaths = readonly? Collections.singletonList(resPath) : entity.getLockPaths(resPath);
        MemoryLockUtils.lockAndRecord(resPath, lockPaths, readonly);

        T newEntity = action.run();

        if (!readonly && newEntity != null) {
            // update locks with new entity
            List<String> newLockPaths = newEntity.getLockPaths(resPath);
            List<String> delta = newLockPaths.stream().filter(path -> !lockPaths.contains(path))
                    .collect(Collectors.toList());
            if (!delta.isEmpty()) {
                MemoryLockUtils.lockAndRecord(resPath, delta, false);
            }
        }
        return newEntity;
    }

    public static boolean isNoNeedToLock(boolean readonly, ResourceStore store) {
        return !UnitOfWork.isAlreadyInTransaction()
                || !readonly && !(store instanceof ThreadViewResourceStore)
                || !(store.getMetadataStore() instanceof JdbcMetadataStore)
                        && !KylinConfig.getInstanceFromEnv().isUTEnv()
                || UnitOfWork.get().getParams().isUseProjectLock();
    }

    public static RawResource doWithLock(String resPath, boolean readonly, ResourceStore store,
            Action<RawResource> action) {
        if (!isNoNeedToLock(readonly, store)) {
            lockAndRecord(resPath, null, readonly);
        }
        return action.run();
    }

    public static void manuallyLockModule(String project, ModuleLockEnum module, ResourceStore store) {
        if (isNoNeedToLock(false, store)) {
            return;
        }
        List<TransactionLock> moduleLocks = getModuleLocks(project, module);
        assert moduleLocks.size() == 1;
        batchLock(moduleLocks.get(0).transactionUnit(), moduleLocks, false);
    }

    public static void lockAndRecord(String resPath, List<String> specifiedPath, boolean readonly){

        if (resPath.equals("/")) {
            log.warn("The resPath of Entity is wrong. Cannot lock this path.", new Throwable());
            return;
        }
        
        List<String> paths = specifiedPath == null ? Collections.singletonList(resPath) : specifiedPath;

        if (readonly) {
            Set<String> holdReadLocks = UnitOfWork.get().getReadLockPath();
            paths = paths.stream().filter(path -> !holdReadLocks.contains(path)).collect(Collectors.toList());
        }

        if (paths.isEmpty()) {
            return;
        }
        List<TransactionLock> locks = getPathLocks(paths, readonly);

        batchLock(resPath, locks, readonly);
    }

    private static void batchLock(String resPath, List<TransactionLock> locks, boolean readonly) {
        for (TransactionLock lock : locks) {
            if (lock instanceof IntentionReadWriteLock.InnerWriteLock) {
                TransactionLock readLock = lock.getOppositeLock();
                readLock.unlock();
            }
        }

        locks.forEach(lock -> {
            lockWithPreCheck(lock);
            UnitOfWork.recordLocks(resPath, lock, readonly);
        });
    }

    private static void lockWithPreCheck(TransactionLock lock) {
        if (lock instanceof IntentionReadWriteLock.InnerWriteLock) {
            TransactionLock readLock = lock.getOppositeLock();
            readLock.unlock();
        }
        long threadID = Thread.currentThread().getId();
        Set<Long> dependencyThreads = lock.getLockDependencyThreads(threadID);
        List<Long> cycle = MEMORY_LOCK_GRAPH.preCheck(threadID, dependencyThreads);

        if (cycle.isEmpty()) {
            MEMORY_LOCK_GRAPH.setThread(threadID, lock);
            lock.lock();
            MEMORY_LOCK_GRAPH.resetThread(threadID);
        } else {
            StringBuilder builder = new StringBuilder();
            Thread.getAllStackTraces().keySet().stream().filter(t -> cycle.contains(t.getId()))
                    .forEach(thread -> builder.append(Arrays.toString(thread.getStackTrace())));
            throw new DeadLockException(
                    "Lock preCheck failed! To avoid deadlock, restart this transaction." + builder.toString());
        }
    }

    public static Set<Long> findDeadLockThreadIds() {
        List<List<Long>> cycles = MEMORY_LOCK_GRAPH.checkForDeadLock();
        return MEMORY_LOCK_GRAPH.getKeyNodes(cycles);
    }

    public static void removeThreadFromGraph() {
        MEMORY_LOCK_GRAPH.deleteThreadNode(Thread.currentThread().getId());
    }

    public static int getTransactionSize() {
        return MEMORY_LOCK_GRAPH.getThreadNodeCount();
    }

    public interface Action<T> {
        T run();
    }
}
