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

package org.apache.kylin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.kylin.common.persistence.lock.ResourcePathParser;
import org.apache.kylin.metadata.model.ComputedColumnDesc;
import org.apache.kylin.metadata.model.NDataModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class NDataModelTest {

    @Test
    void testModelLockPath() {
        NDataModel model = new NDataModel();
        ComputedColumnDesc cc = new ComputedColumnDesc();
        cc.setColumnName("cc1");
        cc.setTableIdentity("ssb.lineOrder");
        cc.setInnerExpression("a / 1 + a / 2 + a / 4");
        ReflectionTestUtils.setField(model, "project", "default");
        ReflectionTestUtils.setField(model, "computedColumnDescs", Collections.singletonList(cc));
        List<ResourcePathParser.ResourcePath> resourcePaths = new ArrayList<>();
        for (String lockPath : model.getLockPaths()) {
            resourcePaths.add(ResourcePathParser.parseResourcePath(lockPath));
        }
        Assertions.assertEquals(5, resourcePaths.size());
        Assertions.assertEquals(cc.getTableIdentity() + "." + cc.getColumnName(), resourcePaths.get(3).getResource1());

        String escapedExpression = "a @ 1 + a @ 2 + a @ 4";
        Assertions.assertEquals(escapedExpression, resourcePaths.get(4).getResource1());
    }
}
