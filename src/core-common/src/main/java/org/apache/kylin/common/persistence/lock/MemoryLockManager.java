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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.guava30.shaded.common.base.Preconditions;
import org.apache.kylin.guava30.shaded.common.collect.Maps;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryLockManager implements LockManager {
    private final Map<String, ProjectLock> projectLockMap = Maps.newConcurrentMap();
    private final Map<String, ModuleLock> moduleLockMap = Maps.newConcurrentMap();
    private final Map<String, PathLock> pathLockMap = Maps.newConcurrentMap();

    // workaround for https://bugs.openjdk.org/browse/JDK-8161372
    private <T> T computeIfAbsent(Map<String, T> map, String key, Function<String, ? extends T> mappingFunction) {
        T result = map.get(key);
        if (null == result) {
            result = map.computeIfAbsent(key, mappingFunction);
        }
        return result;
    }

    @Override
    public ProjectLock getProjectLock(@NotNull String projectName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(projectName), "projectName is blank");
        return computeIfAbsent(projectLockMap, projectName, k -> new ProjectLock(projectName));
    }

    @Override
    public ProjectLock removeProjectLock(@NotNull String projectName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(projectName), "projectName is blank");
        return projectLockMap.remove(projectName);
    }

    @Override
    public ModuleLock getModuleLock(@NotNull String projectName, @NotNull ModuleLockEnum moduleName) {
        Preconditions.checkNotNull(moduleName, "moduleName is null");
        return computeIfAbsent(moduleLockMap, projectName + ":" + moduleName,
                k -> new ModuleLock(moduleName, getProjectLock(projectName)));
    }

    @Override
    public List<PathLock> getPathLock(@NotNull String pathName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(pathName), "pathName is blank");
        // parse the Module in the Path, and return the DEFAULT Module if it cannot be parsed
        val resourcePath = ResourcePathParser.parseResourcePath(pathName);
        ModuleLockEnum moduleLockEnum = ModuleLockEnum.getModuleEnum(resourcePath.getModule());

        ModuleLock moduleLock = getModuleLock(resourcePath.getProject(), moduleLockEnum);
        List<String> newPaths = ResourcePathParser.transformPath(pathName, moduleLockEnum, resourcePath);
        return newPaths.stream().map(path -> computeIfAbsent(pathLockMap, path, k -> new PathLock(path, moduleLock)))
                .collect(Collectors.toList());
    }

    @Override
    public String getProjectByPath(@NotNull String pathName) {
        return ResourcePathParser.parseResourcePath(pathName).getProject();
    }

    @Override
    public Map<String, ProjectLock> listAllProjectLock() {
        return Maps.newHashMap(projectLockMap);
    }

    @Override
    public void checkProjectLockSize() {
        if (projectLockMap.size() > 5000L) {
            projectLockMap.forEach((k, v) -> log.warn("lock leaks lock: {}，num: {}", k, v.getTranHoldCount()));
        }
    }
}
