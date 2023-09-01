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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.util.NamedThreadFactory;

import lombok.extern.slf4j.Slf4j;
import org.apache.kylin.guava30.shaded.common.primitives.Longs;

@Slf4j
public class TransactionDeadLockHandler implements DeadLockHandler, Runnable {

    public static final String THREAD_NAME_PREFIX = "Transaction-Thread";
    private final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
    private final AtomicBoolean scheduleStarted = new AtomicBoolean(false);
    private final long interval = KylinConfig.getInstanceFromEnv().getLockCheckIntervalSeconds();
    private final ScheduledExecutorService checkerPool = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("DeadLockChecker"));
    
    private int checkCnt = 0;
    private double checkAvgCost = 0;
    private long checkMaxCost = 0;

    private TransactionDeadLockHandler() {
    }

    private static final class InstanceHolder {
        static final TransactionDeadLockHandler INSTANCE = new TransactionDeadLockHandler();
    }

    public static TransactionDeadLockHandler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void start() {
        if (scheduleStarted.compareAndSet(false, true)) {
            log.info("Start scheduling deadlock detection every {} seconds", interval);
            checkerPool.scheduleWithFixedDelay(this, 1, interval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void run() {
        try {
            Set<ThreadInfo> threadInfoSet = getThreadsToBeKill();
            if (CollectionUtils.isEmpty(threadInfoSet)) {
                return;
            }

            for (ThreadInfo threadInfo : threadInfoSet) {
                log.warn("Deadlock thread detail:\n{}", threadInfo);
            }
            Set<Long> threadIdSet = threadInfoSet.stream().map(ThreadInfo::getThreadId).collect(Collectors.toSet());
            killThreadsById(threadIdSet);
        } catch (Throwable throwable) {
            log.info("Deadlock detection failed. Wait for next execution ...", throwable);
        }
    }

    @Override
    public Set<ThreadInfo> getThreadsToBeKill() {
        ThreadInfo[] threadInfos = findDeadLockThreads();
        if (ArrayUtils.isEmpty(threadInfos)) {
            return Collections.emptySet();
        }
        Set<ThreadInfo> threadInfoSet = Arrays.stream(threadInfos)//
                .filter(Objects::nonNull)//
                .filter(t -> StringUtils.startsWith(t.getThreadName(), getThreadNamePrefix()))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(threadInfoSet)) {
            return Collections.emptySet();
        }
        log.info("Found transaction deadlock thread, size {}, total transaction size: {}", threadInfoSet.size(),
                MemoryLockUtils.getTransactionSize());
        return threadInfoSet;
    }

    @Override
    public Set<Long> getThreadIdToBeKill() {
        return getThreadsToBeKill().stream().filter(Objects::nonNull).map(ThreadInfo::getThreadId)
                .collect(Collectors.toSet());
    }

    @Override
    public ThreadInfo[] findDeadLockThreads() {
        long start = System.currentTimeMillis();
        long[] threadIds = Longs.toArray(MemoryLockUtils.findDeadLockThreadIds());
        updateTimeStatistics(System.currentTimeMillis() - start);
        if (ArrayUtils.isEmpty(threadIds)) {
            return new ThreadInfo[0];
        }
        return mxBean.getThreadInfo(threadIds, true, true);
    }

    private void updateTimeStatistics(long timeCost) {
        checkAvgCost = (checkAvgCost * checkCnt + timeCost) / (checkCnt + 1);
        checkCnt = checkCnt + 1;
        checkMaxCost = Math.max(checkMaxCost, timeCost);
        if (checkCnt % 200 == 0) {
            log.debug(
                    "DeadLock checker has run {} times, the average cost time is {} ms, and the max cost time is {} ms.",
                    checkCnt, checkAvgCost, checkMaxCost);
        }
    }

    @Override
    public Set<Thread> getThreadsById(Set<Long> threadIdSet) {
        if (CollectionUtils.isEmpty(threadIdSet)) {
            return Collections.emptySet();
        }
        return Thread.getAllStackTraces().keySet().stream().filter(t -> threadIdSet.contains(t.getId()))
                .collect(Collectors.toSet());
    }

    @Override
    public void killThreads(Set<Thread> threads) {
        for (Thread thread : threads) {
            log.info("Interrupt thread: {}", thread.getName());
            thread.interrupt();
        }
    }

    @Override
    public void killThreadsById(Set<Long> threadIdSet) {
        if (CollectionUtils.isEmpty(threadIdSet)) {
            return;
        }
        killThreads(getThreadsById(threadIdSet));
    }

    @Override
    public String getThreadNamePrefix() {
        return THREAD_NAME_PREFIX;
    }
}
