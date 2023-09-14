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

package org.apache.kylin.rest.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.kylin.common.SystemPropertiesCache;
import org.apache.kylin.junit.annotation.MetadataInfo;
import org.apache.kylin.rest.cache.KylinCache;
import org.apache.kylin.rest.cache.RedisCache;
import org.apache.kylin.rest.cache.RedisCacheV2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@MetadataInfo
@ExtendWith(MockitoExtension.class)
class QueryCacheManagerTest {
    @Mock
    private KylinCache kylinCache;
    @InjectMocks
    private QueryCacheManager queryCacheManager;

    @Test
    void testInitIsRedisEnabled() {
        System.setProperty("kylin.cache.redis.enabled", "true");
        try {
            queryCacheManager.init();
        } catch (Exception e) {
            Assertions.fail();
        }
        System.clearProperty("kylin.cache.redis.enabled");
    }

    @Test
    void testInitIsNotRedisEnabled() {
        System.setProperty("kylin.cache.redis.enabled", "false");
        try {
            queryCacheManager.init();
        } catch (Exception e) {
            Assertions.fail();
        }
        System.clearProperty("kylin.cache.redis.enabled");
    }

    @Test
    void testInitIsRedisSentinelEnabled() {
        System.setProperty("kylin.cache.redis.enabled", "true");
        System.setProperty("kylin.cache.redis.sentinel-enabled", "true");
        System.setProperty("kylin.cache.redis.sentinel-master", "master");

        assertDoesNotThrow(() -> queryCacheManager.init());

        System.clearProperty("kylin.cache.redis.enabled");
        System.clearProperty("kylin.cache.redis.sentinel-enabled");
        System.clearProperty("kylin.cache.redis.sentinel-master");
    }

    @Test
    void testRecoverCacheWhenNoNeedToRecover() {
        System.setProperty("kylin.cache.redis.enabled", "false");

        boolean recoverResult = queryCacheManager.recoverCache();
        assertFalse(recoverResult);

        System.clearProperty("kylin.cache.redis.enabled");
    }

    @Test
    void testRecoverCacheWhenCluster() {
        System.setProperty("kylin.cache.redis.enabled", "true");
        kylinCache = Mockito.mock(RedisCache.class);
        ReflectionTestUtils.setField(queryCacheManager, "kylinCache", kylinCache);

        try {
            queryCacheManager.recoverCache();
        } catch (Exception e) {
            Assertions.fail();
        }

        System.clearProperty("kylin.cache.redis.enabled");
    }

    @Test
    void testRecoverCacheWhenSentinelRecoverTrue() {
        SystemPropertiesCache.setProperty("kylin.cache.redis.enabled", "true");
        SystemPropertiesCache.setProperty("kylin.cache.redis.sentinel-enabled", "true");
        kylinCache = Mockito.mock(RedisCacheV2.class);
        ReflectionTestUtils.setField(queryCacheManager, "kylinCache", kylinCache);
        Mockito.when(((RedisCacheV2) kylinCache).recoverInstance()).thenReturn(kylinCache);

        boolean recoverResult = queryCacheManager.recoverCache();
        assertTrue(recoverResult);

        SystemPropertiesCache.clearProperty("kylin.cache.redis.enabled");
        SystemPropertiesCache.clearProperty("kylin.cache.redis.sentinel-enabled");
    }

    @Test
    void testRecoverCacheWhenSentinelRecoverFailed() {
        System.setProperty("kylin.cache.redis.enabled", "true");
        System.setProperty("kylin.cache.redis.sentinel-enabled", "true");
        kylinCache = Mockito.mock(RedisCacheV2.class);
        ReflectionTestUtils.setField(queryCacheManager, "kylinCache", kylinCache);

        boolean recoverResult = queryCacheManager.recoverCache();
        assertFalse(recoverResult);

        System.clearProperty("kylin.cache.redis.enabled");
        System.clearProperty("kylin.cache.redis.sentinel-enabled");
    }

    @Test
    void testGetCacheException() {
        Mockito.when(kylinCache.get("type", "project", "key")).thenThrow(new RuntimeException());

        Object value = queryCacheManager.getCache("type", "project", "key");

        assertNull(value);
    }

    @Test
    void testPutCacheException() {
        Mockito.doThrow(new RuntimeException()).when(kylinCache).put("type", "project", "key", "value");

        assertDoesNotThrow(() -> queryCacheManager.putCache("type", "project", "key", "value"));
    }

    @Test
    void testRemoveCacheException() {
        Mockito.doThrow(new RuntimeException()).when(kylinCache).remove("type", "project", "key");

        boolean value = queryCacheManager.removeCache("type", "project", "key");

        assertFalse(value);
    }

    @Test
    void testClearAllCacheException() {
        Mockito.doThrow(new RuntimeException()).when(kylinCache).clearAll();

        assertDoesNotThrow(() -> queryCacheManager.clearAllCache());
    }

    @Test
    void testClearCacheByTypeException() {
        Mockito.doThrow(new RuntimeException()).when(kylinCache).clearByType("type", "project");

        assertDoesNotThrow(() -> queryCacheManager.clearCacheByType("type", "project"));
    }

    @Test
    void testClearSchemaCacheV2() {
        assertDoesNotThrow(() -> queryCacheManager.clearSchemaCacheV2("project", "userName"));
    }

    @Test
    void testClearSchemaCache() {
        assertDoesNotThrow(() -> queryCacheManager.clearSchemaCache("project", "userName"));
    }

    @Test
    void testOnClearSchemaCache() {
        assertDoesNotThrow(() -> queryCacheManager.onClearProjectCache("project"));
    }

}
