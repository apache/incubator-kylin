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

import static org.apache.kylin.common.exception.ServerErrorCode.REDIS_INIT_FAILED;
import static org.apache.kylin.rest.cache.CacheConstant.NX;
import static org.apache.kylin.rest.cache.CacheConstant.PREFIX;
import static org.apache.kylin.rest.cache.CacheConstant.XX;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.Singletons;
import org.apache.kylin.common.exception.KylinException;
import org.apache.kylin.common.util.EncryptUtil;

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.internal.HostAndPort;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisCacheV2 extends AbstractKylinCache implements KylinCache {
    private static final AtomicBoolean isRecovering = new AtomicBoolean(false);
    private String redisExpireTimeUnit;
    private long redisExpireTime;
    private long redisExpireTimeForException;
    private StatefulRedisMasterReplicaConnection<byte[], byte[]> redisConnection;
    private RedisCommands<byte[], byte[]> syncCommands;

    private RedisCacheV2() {
        KylinConfig kylinConfig = KylinConfig.getInstanceFromEnv();
        loadConfigurations(kylinConfig);
        createRedisClient(kylinConfig);
        log.info("Redis init success.");
    }

    public static KylinCache getInstance() {
        try {
            return Singletons.getInstance(RedisCacheV2.class);
        } catch (Exception e) {
            log.error("Redis init failed:{} ", ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    @Override
    public void put(String type, String project, Object key, Object value) {
        long expireTime = isExceptionQuery(type) ? redisExpireTimeForException : redisExpireTime;
        put(getTypeProjectPrefix(type, project), key, value, NX, redisExpireTimeUnit, expireTime);
    }

    public void put(String type, Object key, Object value, String ifExist, String expireTimeUnit, long expireTime) {
        byte[] realKey = convertKeyToByte(type, key);
        byte[] valueBytes = convertValueToByte(value);
        syncCommands.set(realKey, valueBytes, createSetArgs(ifExist, expireTimeUnit, expireTime));
    }

    @Override
    public void update(String type, String project, Object key, Object value) {
        long expireTime = isExceptionQuery(type) ? redisExpireTimeForException : redisExpireTime;
        put(getTypeProjectPrefix(type, project), key, value, XX, redisExpireTimeUnit, expireTime);
    }

    @Override
    public Object get(String type, String project, Object key) {
        byte[] realKey = convertKeyToByte(getTypeProjectPrefix(type, project), key);
        log.trace("redis get start");
        byte[] sqlResp = syncCommands.get(realKey);
        log.trace("redis get done, size = {}bytes", sqlResp == null ? 0 : sqlResp.length);

        if (sqlResp != null) {
            Object result = convertByteToObject(sqlResp);
            log.trace("redis result deserialized");
            return result;
        }
        return null;
    }

    @Override
    public boolean remove(String type, String project, Object key) {
        Long removedCount = syncCommands.del(convertKeyToByte(getTypeProjectPrefix(type, project), key));
        return removedCount != null && removedCount > 0;
    }

    @Override
    public void clearAll() {
        clearByType("", "");
    }

    @Override
    public void clearByType(String type, String project) {
        String prefixAndType = PREFIX;
        if (!type.isEmpty()) {
            prefixAndType += getTypeProjectPrefix(type, project);
        }
        KeyScanCursor<byte[]> scanCursor = syncCommands.scan(ScanArgs.Builder.matches(prefixAndType + "*"));

        if (scanCursor.isFinished()) {
            for (byte[] key : scanCursor.getKeys()) {
                syncCommands.del(key);
            }
        }
        while (!scanCursor.isFinished()) {
            for (byte[] key : scanCursor.getKeys()) {
                syncCommands.del(key);
            }
            scanCursor = syncCommands.scan(scanCursor);
        }
    }

    public KylinCache recoverInstance() {
        KylinCache kylinCache = this;
        if (isRecovering.compareAndSet(false, true)) {
            try {
                log.info("Destroy RedisCacheV2.");
                if (redisConnection != null) {
                    redisConnection.close();
                }
                Singletons.clearInstance(RedisCacheV2.class);
                log.info("Initiate RedisCacheV2.");
                kylinCache = Singletons.getInstance(RedisCacheV2.class);
            } finally {
                isRecovering.set(false);
            }
        }
        return kylinCache;
    }

    private void loadConfigurations(KylinConfig kylinConfig) {
        redisExpireTimeUnit = kylinConfig.getRedisExpireTimeUnit();
        redisExpireTime = kylinConfig.getRedisExpireTime();
        redisExpireTimeForException = kylinConfig.getRedisExpireTimeForException();
    }

    private List<HostAndPort> parseHosts(KylinConfig kylinConfig) {
        String redisHosts = kylinConfig.getRedisHosts().trim();
        String[] hostAndPorts = redisHosts.split(",");
        if (Arrays.stream(hostAndPorts).anyMatch(StringUtils::isBlank)) {
            throw new KylinException(REDIS_INIT_FAILED, "Redis client init failed because there are "
                    + "some errors in kylin.properties for 'kylin.cache.redis.hosts'");
        }

        return Arrays.stream(hostAndPorts).map(hostAndPort -> {
            String host = hostAndPort.split(":")[0].trim();
            int port = Integer.parseInt(hostAndPort.split(":")[1]);
            return HostAndPort.of(host, port);
        }).collect(Collectors.toList());
    }

    private char[] parsePassword(KylinConfig kylinConfig) {
        String redisPassword = kylinConfig.getRedisPassword();
        if (EncryptUtil.isEncrypted(redisPassword)) {
            redisPassword = EncryptUtil.getDecryptedValue(redisPassword);
        }
        return redisPassword == null ? null : redisPassword.toCharArray();
    }

    private void createRedisClient(KylinConfig kylinConfig) {
        List<HostAndPort> hostAndPorts = parseHosts(kylinConfig);
        char[] redisPassword = parsePassword(kylinConfig);

        log.info("The 'kylin.cache.redis.sentinel-enabled' is {}", kylinConfig.isRedisSentinelEnabled());
        if (kylinConfig.isRedisSentinelEnabled()) {
            log.info("kylin will use redis sentinel");
            RedisURI redisURI = buildRedisURI(kylinConfig, hostAndPorts, redisPassword);
            RedisClient redisClient = RedisClient.create();
            redisConnection = MasterReplica.connect(redisClient, ByteArrayCodec.INSTANCE, redisURI);
            redisConnection.setReadFrom(ReadFrom.REPLICA_PREFERRED);
            syncCommands = redisConnection.sync();
            log.info("Lettuce sentinel ping:{}", syncCommands.ping());
        }
    }

    private RedisURI buildRedisURI(KylinConfig kylinConfig, List<HostAndPort> hostAndPorts, char[] redisPassword) {
        RedisURI.Builder redisUriBuilder = RedisURI.builder()
                .withSentinelMasterId(kylinConfig.getRedisSentinelMasterId()).withPassword(redisPassword)
                .withTimeout(Duration.ofMillis(kylinConfig.getRedisConnectionTimeout()));
        for (HostAndPort hostAndPort : hostAndPorts) {
            redisUriBuilder.withSentinel(hostAndPort.getHostText(), hostAndPort.getPort());
        }
        RedisURI redisURI = redisUriBuilder.build();
        log.info("Redis uri:{}", redisURI);
        return redisURI;
    }

    private SetArgs createSetArgs(String ifExist, String expireTimeUnit, Long expireTime) {
        SetArgs setArgs = new SetArgs();
        setArgs = expireTimeUnit.equals("EX") ? setArgs.ex(expireTime) : setArgs.px(expireTime);
        setArgs = ifExist.equals(NX) ? setArgs.nx() : setArgs.xx();
        return setArgs;
    }

}
