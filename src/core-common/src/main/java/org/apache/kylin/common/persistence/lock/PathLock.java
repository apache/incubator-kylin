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

import lombok.Getter;

@Getter
public class PathLock extends AbstractMemoryLock implements TempLock {

    private final String pathName;
    private final ModuleLock moduleLock;

    public PathLock(String pathName, ModuleLock moduleLock) {
        super(moduleLock);
        this.pathName = pathName;
        this.moduleLock = moduleLock;
    }

    @Override
    public String transactionUnit() {
        return moduleLock.transactionUnit() + ":" + pathName;
    }

    @Override
    public ModuleLockEnum moduleEnum() {
        return moduleLock.getModuleName();
    }
}
