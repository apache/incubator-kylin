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

package org.apache.kylin.metadata.cachesync;

import static org.apache.kylin.common.util.TestUtils.getTestConfig;

import org.apache.kylin.junit.annotation.MetadataInfo;
import org.apache.kylin.metadata.user.ManagedUser;
import org.apache.kylin.metadata.user.NKylinUserManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MetadataInfo(onlyProps = true)
class CachedCrudAssistTest {

    @Test
    void testCopyForWrite() {
        NKylinUserManager manager = NKylinUserManager.getInstance(getTestConfig());

        ManagedUser newUser = new ManagedUser("a", "a", false);

        ManagedUser brokenUser = new ManagedUser("b", "b", false);
        brokenUser.setBroken(true);
        manager.createUser(brokenUser);

        ManagedUser normalUser = new ManagedUser("c", "c", false);
        manager.createUser(normalUser);

        ManagedUser delUser = new ManagedUser("d", "d", false);
        delUser.setMvcc(10);

        // null entity
        Assertions.assertNull(manager.copyForWrite(null));

        // new entity
        Assertions.assertEquals(newUser, manager.copyForWrite(newUser));

        // broken entity
        Assertions.assertEquals(brokenUser, manager.copyForWrite(brokenUser));

        // normalEntity
        Assertions.assertEquals(0, manager.copyForWrite(normalUser).getMvcc());

        // insert a deleted user
        Assertions.assertThrows(IllegalArgumentException.class, () -> manager.createUser(delUser));
    }
}
