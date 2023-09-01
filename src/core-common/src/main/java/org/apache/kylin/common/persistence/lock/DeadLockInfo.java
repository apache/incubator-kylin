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

import java.lang.management.LockInfo;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class DeadLockInfo {

    @JsonProperty("thread_id")
    private long threadId;
    @JsonProperty("thread_name")
    private String threadName;
    @JsonProperty("owned_locks")
    private List<String> ownedLocks;
    @JsonProperty("waiting_lock")
    private String waitingLock;
    @JsonProperty("stacktrace")
    private String stacktrace;

    @JsonIgnore
    private static final int MAX_STACKTRACE_LENGTH = 16;

    public DeadLockInfo(ThreadInfo ti) {
        this.stacktrace = getStackTraceString(ti.getStackTrace());
        this.threadName = ti.getThreadName();
        this.threadId = ti.getThreadId();
        this.waitingLock = ti.getLockInfo().toString();
        this.ownedLocks = Arrays.stream(ti.getLockedSynchronizers()).map(LockInfo::toString)
                .collect(Collectors.toList());
    }

    private String getStackTraceString(StackTraceElement[] st) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (; i < st.length && i < MAX_STACKTRACE_LENGTH; i++) {
            StackTraceElement ste = st[i];
            sb.append("\tat ").append(ste.toString());
            sb.append('\n');
        }
        if (i < st.length) {
            sb.append("\t...");
            sb.append('\n');
        }
        return sb.toString();
    }
}
