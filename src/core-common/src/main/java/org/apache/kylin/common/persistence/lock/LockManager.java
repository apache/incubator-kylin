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

import javax.validation.constraints.NotNull;

public interface LockManager {

    ProjectLock getProjectLock(@NotNull String projectName);

    ProjectLock removeProjectLock(@NotNull String projectName);

    ModuleLock getModuleLock(@NotNull String projectName, @NotNull ModuleLockEnum moduleName);

    /** Since metadata is in json format, when we want to lock a path, we need lock multiple resources;
     For example, if the pathName belongs to a model, we will lock model name、cc name、cc express. **/
    List<PathLock> getPathLock(@NotNull String pathName);

    String getProjectByPath(@NotNull String pathName);

    Map<String, ProjectLock> listAllProjectLock();

    void checkProjectLockSize();
}
