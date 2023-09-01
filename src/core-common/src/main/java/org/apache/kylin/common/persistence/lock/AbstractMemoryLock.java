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

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractMemoryLock extends IntentionReadWriteLock {

    private final AtomicLong tranHoldCount = new AtomicLong();

    private final Long createTime;

    AbstractMemoryLock(IntentionShareLock intentionShareLock) {
        super(intentionShareLock);
        createTime = System.currentTimeMillis();
    }

    AbstractMemoryLock(IntentionReadWriteLock intentionReadWriteLock) {
        super(new IntentionShareLock(intentionReadWriteLock));
        createTime = System.currentTimeMillis();
    }

    @Override
    public Long createTime() {
        return createTime;
    }

    public AtomicLong getTranHoldCount() {
        return tranHoldCount;
    }

    @Override
    public TransactionLock readLock() {
        tranHoldCount.incrementAndGet();
        return super.readLock();
    }

    @Override
    public TransactionLock writeLock() {
        tranHoldCount.incrementAndGet();
        return super.writeLock();
    }

    public TransactionLock getReadOrWriteLock(boolean read) {
        return read ? readLock() : writeLock();
    }
}
