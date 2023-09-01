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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.kylin.guava30.shaded.common.collect.ComparisonChain;
import org.apache.kylin.guava30.shaded.common.collect.Maps;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This graph only records nodes info, the edges are calculated at realtime.
 */
@Slf4j
public class MemoryLockGraph {

    private final Map<Long, ThreadNode> nodeMap = Maps.newConcurrentMap();

    public void setThread(long threadId, TransactionLock requiredLock) {
        ThreadNode node = nodeMap.computeIfAbsent(threadId, ThreadNode::new);
        node.setRequiredLock(requiredLock);
    }

    public void deleteThreadNode(long threadId) {
        nodeMap.remove(threadId);
    }

    public void resetThread(long threadId) {
        nodeMap.get(threadId).onLocked();
    }

    public List<Long> preCheck(long threadId, Set<Long> dependencies) {
        if (dependencies.isEmpty()) {
            return Collections.emptyList();
        }
        ThreadNode target = nodeMap.get(threadId);
        if (target == null) {
            return Collections.emptyList();
        }
        List<ThreadNode> trace = new ArrayList<>();
        for (Long id : dependencies) {
            ThreadNode node = nodeMap.get(id);
            if (node == null) {
                continue;
            }
            if (findCycleForTarget(target, node, trace)) {
                return trace.stream().map(ThreadNode::getThreadId).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private boolean findCycleForTarget(ThreadNode target, ThreadNode currentNode, List<ThreadNode> trace) {
        if (currentNode.getThreadId() == target.getThreadId()) {
            return true;
        }
        if (!trace.contains(currentNode)) {
            trace.add(currentNode);
            for (Long threadId : currentNode.getDependencies()) {
                ThreadNode node = nodeMap.get(threadId);
                if (node != null && findCycleForTarget(target, node, trace)) {
                    return true;
                }
            }
            trace.remove(trace.size() - 1);
        }
        return false;
    }

    public List<List<Long>> checkForDeadLock() {
        List<ThreadNode> trace = new ArrayList<>();
        List<List<Long>> cycles = new ArrayList<>();
        for (ThreadNode startNode : nodeMap.values()) {
            Set<Long> finishedNode = cycles.stream().flatMap(List::stream).collect(Collectors.toSet());
            findCycles(startNode, trace, cycles, finishedNode);
        }
        return cycles;
    }

    private void findCycles(ThreadNode currentNode, List<ThreadNode> trace, List<List<Long>> cycles,
            Set<Long> finishedNode) {
        if (finishedNode.contains(currentNode.getThreadId())) {
            return;
        }
        int index = trace.indexOf(currentNode);
        if (index != -1) {
            cycles.add(trace.subList(index, trace.size()).stream().map(ThreadNode::getThreadId)
                    .collect(Collectors.toList()));
        } else {
            trace.add(currentNode);
            currentNode.getDependencies().forEach(id -> {
                ThreadNode node = nodeMap.get(id);
                if (node != null) {
                    findCycles(node, trace, cycles, finishedNode);
                }
            });
            trace.remove(trace.size() - 1);
        }
    }

    public Set<Long> getKeyNodes(List<List<Long>> cycles) {
        Map<Long, Integer> cycleCntMap = Maps.newHashMap();
        cycles.stream().flatMap(List::stream).forEach(threadId -> {
            int cnt = cycleCntMap.getOrDefault(threadId, 0);
            cycleCntMap.put(threadId, cnt + 1);
        });
        Comparator<Long> comparator = ((Comparator<Long>) (thread1, thread2) -> ComparisonChain.start()
                .compare(cycleCntMap.get(thread1), cycleCntMap.get(thread2))
                .compare(nodeMap.get(thread1).getCreateTime(), nodeMap.get(thread2).getCreateTime()).result())
                        .reversed();
        Set<Long> keyNode = new HashSet<>();
        cycles.forEach(cycle -> {
            cycle.sort(comparator);
            keyNode.add(cycle.get(0));
        });
        return keyNode;
    }

    public boolean isThreadWaitForLock(long threadId) {
        return nodeMap.get(threadId).getRequiredLock() == null;
    }

    public int getThreadNodeCount() {
        return nodeMap.size();
    }

    @Data
    static class ThreadNode {
        long threadId;
        long createTime;
        TransactionLock requiredLock;

        private ThreadNode(long threadId) {
            this.threadId = threadId;
            this.createTime = System.currentTimeMillis();
        }

        private void onLocked() {
            this.requiredLock = null;
        }

        private Set<Long> getDependencies() {
            return requiredLock == null ? Collections.emptySet() : requiredLock.getLockDependencyThreads(threadId);
        }
    }
}
