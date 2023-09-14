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

import static org.apache.kylin.rest.cache.CacheConstant.PREFIX;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.kylin.common.util.CompressionUtils;
import org.apache.kylin.rest.service.CommonQueryCacheSupporter;
import org.apache.kylin.rest.util.SerializeUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractKylinCache implements KylinCache {
    protected boolean isExceptionQuery(String type) {
        return type.equals(CommonQueryCacheSupporter.Type.EXCEPTION_QUERY_CACHE.rootCacheName);
    }

    protected String getTypeProjectPrefix(String type, String project) {
        return String.format(Locale.ROOT, "%s-%s", type, project);
    }

    protected byte[] convertKeyToByte(String type, Object key) {
        try {
            String prefixAndType = PREFIX + type;
            byte[] typeBytes = getBytesFromString(prefixAndType);
            byte[] keyBytes = SerializeUtil.serialize(key);
            byte[] trueKeyBytes = new byte[keyBytes.length + typeBytes.length];
            System.arraycopy(typeBytes, 0, trueKeyBytes, 0, typeBytes.length);
            System.arraycopy(keyBytes, 0, trueKeyBytes, typeBytes.length, keyBytes.length);
            return trueKeyBytes;
        } catch (Exception e) {
            log.error("serialize fail!", e);
            return new byte[0];
        }
    }

    protected byte[] convertValueToByte(Object value) {
        try {
            return CompressionUtils.compress(SerializeUtil.serialize(value));
        } catch (Exception e) {
            log.error("serialize failed!", e);
            return new byte[0];
        }
    }

    protected byte[] getBytesFromString(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    protected Object convertByteToObject(byte[] bytes) {
        try {
            return SerializeUtil.deserialize(CompressionUtils.decompress(bytes));
        } catch (Exception e) {
            log.error("deserialize fail!", e);
            return null;
        }
    }
}
