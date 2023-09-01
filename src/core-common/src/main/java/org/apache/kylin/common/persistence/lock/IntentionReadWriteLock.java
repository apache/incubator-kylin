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

import static java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.kylin.common.KylinConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.kylin.guava30.shaded.common.base.Preconditions;

@Slf4j
public class IntentionReadWriteLock implements IntentionLock, ReadWriteLock, MemoryLock {

    private final InnerReentrantLock lockInternal = new InnerReentrantLock();
    private final TransactionLock readLock;
    private final TransactionLock writeLock;

    private final IntentionShareLock intentionShareLock;

    public IntentionReadWriteLock(IntentionShareLock intentionShareLock) {
        this.intentionShareLock = intentionShareLock;
        this.readLock = new InnerReadLock(lockInternal);
        this.writeLock = new InnerWriteLock(lockInternal);
    }

    @Override
    public TransactionLock readLock() {
        return readLock;
    }

    @Override
    public TransactionLock writeLock() {
        return writeLock;
    }

    @Override
    public Lock shareLock() {
        return readLock;
    }

    protected long getLockTimeoutSeconds() {
        return KylinConfig.getInstanceFromEnv().getLockTimeoutSeconds();
    }

    private static class InnerReentrantLock extends ReentrantReadWriteLock {
        private final Set<Long> readLockOwners = new ConcurrentSkipListSet<>();

        public InnerReentrantLock() {
            super(true);
        }
        
        private Set<Long> getReadLockOwners() {
            return this.readLockOwners;
        }

        private Set<Long> getReadLockDependencyThreads(long threadId) {
            Thread writeLockOwner = getOwner();
            Set<Long> dependencyThreads = new HashSet<>();
            if (writeLockOwner != null) {
                if (writeLockOwner.getId() != threadId) {
                    dependencyThreads.add(writeLockOwner.getId());
                }
            } else {
                if (!getQueuedWriterThreads().isEmpty() && !readLockOwners.contains(threadId)) {
                    dependencyThreads.addAll(readLockOwners);
                }
            }
            return dependencyThreads;
        }

        private Set<Long> getWriteLockDependencyThreads(long threadId) {
            Set<Long> dependencyThreads = new HashSet<>();
            Thread writeLockOwner = getOwner();
            if (writeLockOwner != null) {
                if (writeLockOwner.getId() != threadId) {
                    dependencyThreads.add(writeLockOwner.getId());
                }
            } else {
                Preconditions.checkState(!readLockOwners.contains(threadId),
                        "Must release readLock before acquire writeLock");
                dependencyThreads.addAll(readLockOwners);
            }
            return dependencyThreads;
        }
    }

    public class InnerReadLock extends ReadLock implements TransactionLock {

        /**
         * Constructor for use by subclasses
         *
         * @param lock the outer lock object
         * @throws NullPointerException if the lock is null
         */
        protected InnerReadLock(ReentrantReadWriteLock lock) {
            super(lock);
        }

        @Override
        public void lock() {
            if (!isHeldByCurrentThread() && !this.tryLock()) {
                throw new LockTimeoutException(this.transactionUnit());
            }
            lockInternal.getReadLockOwners().add(Thread.currentThread().getId());
        }

        @Override
        public boolean tryLock() {
            try {
                intentionShareLock.lockIntentionLock();
                return super.tryLock(IntentionReadWriteLock.this.getLockTimeoutSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                intentionShareLock.unlockIntentionLock();
                log.warn("Failed to get lock: {}. This thread is interrupted.", transactionUnit());
                throw new LockInterruptException(e);
            }
        }

        @Override
        public void unlock() {
            if (isHeldByCurrentThread()) {
                lockInternal.getReadLockOwners().remove(Thread.currentThread().getId());
                super.unlock();
                intentionShareLock.unlockIntentionLock();
            }
        }

        @Override
        public boolean isHeldByCurrentThread() {
            return lockInternal.getReadHoldCount() > 0;
        }

        @Override
        public Set<Long> getLockDependencyThreads(long threadId) {
            Set<Long> dependencyThreads = new HashSet<>();
            dependencyThreads.addAll(intentionShareLock.getReadLockDependencyThreads(threadId));
            dependencyThreads.addAll(lockInternal.getReadLockDependencyThreads(threadId));
            return dependencyThreads;
        }

        @Override
        public TransactionLock getOppositeLock() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String transactionUnit() {
            return IntentionReadWriteLock.this.transactionUnit();
        }

        @Override
        public Long createTime() {
            return IntentionReadWriteLock.this.createTime();
        }

        @Override
        public ModuleLockEnum moduleEnum() {
            return IntentionReadWriteLock.this.moduleEnum();
        }
    }

    public class InnerWriteLock extends ReentrantReadWriteLock.WriteLock implements TransactionLock {

        /**
         * Constructor for use by subclasses
         *
         * @param lock the outer lock object
         * @throws NullPointerException if the lock is null
         */
        protected InnerWriteLock(ReentrantReadWriteLock lock) {
            super(lock);
        }

        @Override
        public void lock() {
            if (!isHeldByCurrentThread() && !this.tryLock()) {
                throw new LockTimeoutException(this.transactionUnit());
            }
        }

        @Override
        public boolean tryLock() {
            try {
                intentionShareLock.lockIntentionLock();
                return super.tryLock(IntentionReadWriteLock.this.getLockTimeoutSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                intentionShareLock.unlockIntentionLock();
                log.warn("Failed to get lock: {}. This thread is interrupted.", transactionUnit());
                throw new LockInterruptException(e);
            }
        }

        @Override
        public void unlock() {
            super.unlock();
            intentionShareLock.unlockIntentionLock();
        }

        @Override
        public boolean isHeldByCurrentThread() {
            return lockInternal.getWriteHoldCount() > 0;
        }

        @Override
        public Set<Long> getLockDependencyThreads(long threadId) {
            Set<Long> dependencyThreads = new HashSet<>(intentionShareLock.getReadLockDependencyThreads(threadId));
            dependencyThreads.addAll(lockInternal.getWriteLockDependencyThreads(threadId));
            return dependencyThreads;
        }

        @Override
        public TransactionLock getOppositeLock() {
            return readLock;
        }

        @Override
        public String transactionUnit() {
            return IntentionReadWriteLock.this.transactionUnit();
        }

        @Override
        public Long createTime() {
            return IntentionReadWriteLock.this.createTime();
        }

        @Override
        public ModuleLockEnum moduleEnum() {
            return IntentionReadWriteLock.this.moduleEnum();
        }
    }
}
