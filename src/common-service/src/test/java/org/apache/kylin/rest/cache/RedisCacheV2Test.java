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

package org.apache.kylin.rest.cache;

import static org.apache.kylin.common.util.EncryptUtil.ENC_PREFIX;
import static org.apache.kylin.common.util.EncryptUtil.ENC_SUBFIX;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.kylin.common.util.CompressionUtils;
import org.apache.kylin.common.util.Unsafe;
import org.apache.kylin.guava30.shaded.common.collect.Lists;
import org.apache.kylin.guava30.shaded.common.collect.Maps;
import org.apache.kylin.junit.annotation.MetadataInfo;
import org.apache.kylin.rest.util.SerializeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.sync.RedisCommands;

@MetadataInfo
@ExtendWith(MockitoExtension.class)
class RedisCacheV2Test {
    @Mock
    private RedisCommands<byte[], byte[]> syncCommands;

    @InjectMocks
    private RedisCacheV2 redisCacheV2;

    @Test
    void testPut() {
        String type = "type";
        String project = "project";
        Object key = new Object();
        Object value = new Object();

        redisCacheV2.put(type, project, key, value);

        verify(syncCommands, times(1)).set(any(), any(), any());
    }

    @Test
    void testUpdate() {
        String type = "type";
        String project = "project";
        Object key = new Object();
        Object value = new Object();

        redisCacheV2.update(type, project, key, value);

        verify(syncCommands, times(1)).set(any(), any(), any());
    }

    @Test
    void testGetNull() {
        String type = "type";
        String project = "project";
        Object key = new Object();

        when(syncCommands.get(any())).thenReturn(null);

        Object result = redisCacheV2.get(type, project, key);

        assertNull(result);
        verify(syncCommands, times(1)).get(any());
    }

    @Test
    void testGet() throws DataFormatException, IOException {
        String type = "type";
        String project = "project";
        Object key = new Object();
        byte[] valueBytes = { -84, -19, 0, 5, 116, 0, 5, 118, 97, 108, 117, 101 };
        when(syncCommands.get(any())).thenReturn(valueBytes);
        Object expectedResult = SerializeUtil.deserialize(CompressionUtils.decompress(valueBytes));

        Object result = redisCacheV2.get(type, project, key);

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void testRemove() {
        String type = "type";
        String project = "project";
        Object key = new Object();

        when(syncCommands.del(any())).thenReturn(1L);

        boolean result = redisCacheV2.remove(type, project, key);

        assertTrue(result);
        verify(syncCommands, times(1)).del(any());
    }

    @Test
    void testClearAll() {
        mockScanResult();

        redisCacheV2.clearAll();

        verify(syncCommands, atLeastOnce()).scan(any(ScanArgs.class));
        verify(syncCommands, atLeastOnce()).del(any());
    }

    @Test
    void testClearByType() {
        String type = "type";
        String project = "project";
        mockScanResult();

        redisCacheV2.clearByType(type, project);

        verify(syncCommands, atLeastOnce()).scan(any(ScanArgs.class));
        verify(syncCommands, atLeastOnce()).del(any());
    }

    @Test
    void testGetInstance() {
        Unsafe.overwriteSystemProp(Maps.newHashMap(), "kylin.cache.redis.sentinel-enabled", "TRUE");
        Unsafe.overwriteSystemProp(Maps.newHashMap(), "kylin.cache.redis.sentinel-master", "default-master");

        KylinCache kylinCache = RedisCacheV2.getInstance();
        assertNull(kylinCache);

        Unsafe.clearProperty("kylin.cache.redis.sentinel-enabled");
        Unsafe.clearProperty("kylin.cache.redis.sentinel-master");
    }

    @Test
    void testRecoverInstance() {
        KylinCache kylinCache = redisCacheV2.recoverInstance();
        Assertions.assertNotNull(kylinCache);
    }

    @Test
    void testEncryptedPassword() {
        Unsafe.overwriteSystemProp(Maps.newHashMap(), "kylin.cache.redis.password",
                ENC_PREFIX + "password" + ENC_SUBFIX);
        try {
            RedisCacheV2.getInstance();
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    void testRedisHostsError() {
        Unsafe.overwriteSystemProp(Maps.newHashMap(), "kylin.cache.redis.hosts", " ");

        KylinCache kylinCache = RedisCacheV2.getInstance();
        assertNull(kylinCache);

        Unsafe.clearProperty("kylin.cache.redis.hosts");
    }

    private void mockScanResult() {
        KeyScanCursor<byte[]> scanCursor = new KeyScanCursor<>();
        scanCursor.setFinished(true);
        byte[] delKeys = new byte[] { 1, 2, 3 };
        ReflectionTestUtils.setField(scanCursor, "keys", Lists.newArrayList(delKeys));
        when(syncCommands.scan(any(ScanArgs.class))).thenReturn(scanCursor);
    }
}
