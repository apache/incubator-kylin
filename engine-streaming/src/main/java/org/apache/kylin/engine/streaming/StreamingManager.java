/*
 *
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *
 *  contributor license agreements. See the NOTICE file distributed with
 *
 *  this work for additional information regarding copyright ownership.
 *
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *
 *  (the "License"); you may not use this file except in compliance with
 *
 *  the License. You may obtain a copy of the License at
 *
 *
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *
 *  limitations under the License.
 *
 * /
 */

package org.apache.kylin.engine.streaming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.persistence.JsonSerializer;
import org.apache.kylin.common.persistence.ResourceStore;
import org.apache.kylin.common.persistence.Serializer;
import org.apache.kylin.common.restclient.Broadcaster;
import org.apache.kylin.common.restclient.CaseInsensitiveStringCache;
import org.apache.kylin.metadata.MetadataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class StreamingManager {

    private static final Logger logger = LoggerFactory.getLogger(StreamingManager.class);

    // static cached instances
    private static final ConcurrentHashMap<KylinConfig, StreamingManager> CACHE = new ConcurrentHashMap<KylinConfig, StreamingManager>();

    public static final Serializer<StreamingConfig> STREAMING_SERIALIZER = new JsonSerializer<StreamingConfig>(StreamingConfig.class);

    private KylinConfig config;

    // name ==> StreamingConfig
    private CaseInsensitiveStringCache<StreamingConfig> streamingMap;

    public static void clearCache() {
        CACHE.clear();
    }

    private StreamingManager(KylinConfig config) throws IOException {
        this.config = config;
        this.streamingMap = new CaseInsensitiveStringCache<StreamingConfig>(config, Broadcaster.TYPE.STREAMING);
        reloadAllStreaming();
    }

    private ResourceStore getStore() {
        return ResourceStore.getStore(this.config);
    }

    public static StreamingManager getInstance(KylinConfig config) {
        StreamingManager r = CACHE.get(config);
        if (r != null) {
            return r;
        }

        synchronized (StreamingManager.class) {
            r = CACHE.get(config);
            if (r != null) {
                return r;
            }
            try {
                r = new StreamingManager(config);
                CACHE.put(config, r);
                if (CACHE.size() > 1) {
                    logger.warn("More than one streamingManager singleton exist");
                }
                return r;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to init StreamingManager from " + config, e);
            }
        }
    }

    public StreamingConfig getStreamingConfig(String name) {
        return streamingMap.get(name);
    }

    public List<StreamingConfig> listAllStreaming() {
        return new ArrayList<>(streamingMap.values());
    }

    /**
     * Reload StreamingConfig from resource store It will be triggered by an desc
     * update event.
     *
     * @param name
     * @throws IOException
     */
    public StreamingConfig reloadStreamingConfigLocal(String name) throws IOException {

        // Save Source
        String path = StreamingConfig.concatResourcePath(name);

        // Reload the StreamingConfig
        StreamingConfig ndesc = loadStreamingConfigAt(path);

        // Here replace the old one
        streamingMap.putLocal(ndesc.getName(), ndesc);
        return ndesc;
    }

    // remove streamingConfig
    public void removeStreamingConfig(StreamingConfig streamingConfig) throws IOException {
        String path = streamingConfig.getResourcePath();
        getStore().deleteResource(path);
        streamingMap.remove(streamingConfig.getName());
    }

    public StreamingConfig getConfig(String name) {
        name = name.toUpperCase();
        return streamingMap.get(name);
    }

    public void removeStreamingLocal(String streamingName) {
        streamingMap.removeLocal(streamingName);
    }

    /**
     * Update CubeDesc with the input. Broadcast the event into cluster
     *
     * @param desc
     * @return
     * @throws IOException
     */
    public StreamingConfig updateStreamingConfig(StreamingConfig desc) throws IOException {
        // Validate CubeDesc
        if (desc.getUuid() == null || desc.getName() == null) {
            throw new IllegalArgumentException("SteamingConfig Illegal.");
        }
        String name = desc.getName();
        if (!streamingMap.containsKey(name)) {
            throw new IllegalArgumentException("StreamingConfig '" + name + "' does not exist.");
        }

        // Save Source
        String path = desc.getResourcePath();
        getStore().putResource(path, desc, STREAMING_SERIALIZER);

        // Reload the StreamingConfig
        StreamingConfig ndesc = loadStreamingConfigAt(path);
        // Here replace the old one
        streamingMap.put(ndesc.getName(), desc);

        return ndesc;
    }

    public StreamingConfig saveStreamingConfig(StreamingConfig streamingConfig) throws IOException {
        if (streamingConfig == null || StringUtils.isEmpty(streamingConfig.getName())) {
            throw new IllegalArgumentException();
        }

        if (streamingMap.containsKey(streamingConfig.getName()))
            throw new IllegalArgumentException("StreamingConfig '" + streamingConfig.getName() + "' already exists");

        String path  = StreamingConfig.concatResourcePath(streamingConfig.getName());
        getStore().putResource(path, streamingConfig, StreamingConfig.SERIALIZER);
        streamingMap.put(streamingConfig.getName(), streamingConfig);
        return streamingConfig;
    }

    private StreamingConfig loadStreamingConfigAt(String path) throws IOException {
        ResourceStore store = getStore();
        StreamingConfig streamingDesc = store.getResource(path, StreamingConfig.class, STREAMING_SERIALIZER);

        if (StringUtils.isBlank(streamingDesc.getName())) {
            throw new IllegalStateException("StreamingConfig name must not be blank");
        }
        return streamingDesc;
    }

    private void reloadAllStreaming() throws IOException {
        ResourceStore store = getStore();
        logger.info("Reloading Streaming Metadata from folder " + store.getReadableResourcePath(ResourceStore.STREAMING_RESOURCE_ROOT));

        streamingMap.clear();

        List<String> paths = store.collectResourceRecursively(ResourceStore.STREAMING_RESOURCE_ROOT, MetadataConstants.FILE_SURFIX);
        for (String path : paths) {
            StreamingConfig streamingConfig;
            try {
                streamingConfig = loadStreamingConfigAt(path);
            } catch (Exception e) {
                logger.error("Error loading streaming desc " + path, e);
                continue;
            }
            if (path.equals(streamingConfig.getResourcePath()) == false) {
                logger.error("Skip suspicious desc at " + path + ", " + streamingConfig + " should be at " + streamingConfig.getResourcePath());
                continue;
            }
            if (streamingMap.containsKey(streamingConfig.getName())) {
                logger.error("Dup StreamingConfig name '" + streamingConfig.getName() + "' on path " + path);
                continue;
            }

            streamingMap.putLocal(streamingConfig.getName(), streamingConfig);
        }

        logger.debug("Loaded " + streamingMap.size() + " StreamingConfig(s)");
    }
}
