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
package org.apache.kylin.tool.routine;

import static org.apache.kylin.common.util.TestUtils.getTestConfig;

import org.apache.kylin.common.persistence.metadata.EpochStore;
import org.apache.kylin.junit.annotation.JdbcMetadataInfo;
import org.apache.kylin.junit.annotation.MetadataInfo;
import org.apache.kylin.metadata.epoch.EpochManager;
import org.apache.kylin.tool.MaintainModeTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MetadataInfo(onlyProps = true)
@JdbcMetadataInfo
class MaintainModeToolTest {

    @Test
    void testForceToExit() {
        try {
            MaintainModeTool maintainModeTool = new MaintainModeTool();
            maintainModeTool.execute(new String[] { "-off", "--force" });
        } catch (Exception e) {
            log.info("Something wrong when running testForceToExit\n", e);
            throw new RuntimeException(e);
        }
    }

    @Test
    void testEnterMaintainMode() {
        try {
            MaintainModeTool maintainModeTool = new MaintainModeTool();
            maintainModeTool.execute(new String[] { "-on", "-reason", "test" });
        } catch (Exception e) {
            log.info("Something wrong when running testEnterMaintainMode\n", e);
            throw new RuntimeException(e);
        }
    }

    @Test
    void testEnterMaintainModeEpochCheck() {
        EpochManager epochManager = EpochManager.getInstance();
        epochManager.tryUpdateEpoch(EpochManager.GLOBAL, true);

        val globalEpoch = epochManager.getGlobalEpoch();
        int id = 1234;
        globalEpoch.setEpochId(id);

        ReflectionTestUtils.invokeMethod(epochManager, "insertOrUpdateEpoch", globalEpoch);

        Assertions.assertEquals(id, epochManager.getGlobalEpoch().getEpochId());
        MaintainModeTool maintainModeTool = new MaintainModeTool();
        maintainModeTool.execute(new String[] { "-on", "-reason", "test" });

        Assertions.assertEquals(id + 1, epochManager.getGlobalEpoch().getEpochId());

        maintainModeTool.execute(new String[] { "-off", "--force" });
        Assertions.assertEquals(id + 2, epochManager.getGlobalEpoch().getEpochId());
    }

    @Test
    void testCleanProject() {
        MaintainModeTool maintainModeTool = new MaintainModeTool();
        maintainModeTool.execute(new String[] { "-on", "-reason", "test", "-p", "notExistP1" });

        Assertions.assertEquals(2, getEpochStore().list().size());
    }

    EpochStore getEpochStore() {
        try {
            return EpochStore.getEpochStore(getTestConfig());
        } catch (Exception e) {
            throw new RuntimeException("cannot init epoch store!");
        }
    }
}
