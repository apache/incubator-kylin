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

import java.lang.management.ThreadInfo;
import java.util.Set;

public interface DeadLockHandler {

    /**
     * Detect deadlock ThreadInfo
     */
    ThreadInfo[] findDeadLockThreads();

    /**
     * Get the ThreadInfo that needs to be killed
     */
    Set<ThreadInfo> getThreadsToBeKill();

    /**
     * Get the ThreadIds that needs to be killed
     */
    Set<Long> getThreadIdToBeKill();

    /**
     * Filter thread based on ThreadId
     */
    Set<Thread> getThreadsById(Set<Long> threadIdSet);

    /**
     * Interrupt threads
     */
    void killThreads(Set<Thread> threads);

    /**
     * Interrupt threads by threadIds
     */
    void killThreadsById(Set<Long> threadIdSet);

    /**
     * Thread name prefix for filtering
     */
    String getThreadNamePrefix();

}
