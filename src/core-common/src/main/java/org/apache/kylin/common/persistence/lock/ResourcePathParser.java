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

import static org.apache.kylin.common.persistence.lock.ModuleLockEnum.DEFAULT;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.guava30.shaded.common.base.Joiner;
import org.apache.kylin.guava30.shaded.common.base.Preconditions;
import org.apache.kylin.guava30.shaded.common.cache.Cache;
import org.apache.kylin.guava30.shaded.common.cache.CacheBuilder;
import org.apache.kylin.guava30.shaded.common.collect.Lists;
import org.apache.kylin.guava30.shaded.common.collect.Maps;
import org.glassfish.jersey.uri.UriTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ResourcePathParser {

    private static final int CACHE_SIZE = 10000;
    private static final int CACHE_EXPIRE_MINUTES = 30;
    private static final String RESOURCE_PATH_SUFFIX = ".json";
    private static final Cache<String, ResourcePath> RES_CACHE = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE)
            .expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES).build();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> TEMPLATE_URIS;
    private static final List<UriTemplate> TEMPLATES;
    public static final Joiner JOINER = Joiner.on('/').skipNulls();

    static {
        TEMPLATE_URIS = Lists.newArrayList(//
                "/{project}/{module}/{resource1}", // normal
                "/{project}/{module}/{resource1}/{resource2}", // dataflow_detail
                "/{project}/{module}", // _health
                "/{project}" // UUID
        );
        TEMPLATES = TEMPLATE_URIS.stream().map(UriTemplate::new).collect(Collectors.toList());
    }

    public static ResourcePath parseResourcePath(String originPath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(originPath), "path is blank");
        if (originPath.endsWith(RESOURCE_PATH_SUFFIX)) {
            originPath = originPath.substring(0, originPath.length() - RESOURCE_PATH_SUFFIX.length());
        }

        ResourcePath resourcePath = RES_CACHE.getIfPresent(originPath);
        if (Objects.nonNull(resourcePath)) {
            return resourcePath;
        }

        for (UriTemplate template : TEMPLATES) {
            Map<String, String> map = Maps.newHashMap();
            if (template.match(originPath, map)) {
                resourcePath = MAPPER.convertValue(map, ResourcePath.class);
                RES_CACHE.put(originPath, resourcePath);
                return resourcePath;
            }
        }
        throw new IllegalArgumentException("The path does not match: " + originPath);
    }

    public static List<String> transformPath(String originPath, ModuleLockEnum moduleLockEnum,
            ResourcePathParser.ResourcePath resourcePath) {
        if (moduleLockEnum.equals(DEFAULT) || Objects.isNull(resourcePath)) {
            // can not parse module
            return Collections.singletonList(originPath);
        }

        // transform path name
        return resourcePath.transformPath();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ResourcePath {
        private String project;
        private String module;
        private String resource1;
        private String resource2;

        public List<String> transformPath() {
            return getDefaultLockPath();
        }

        private List<String> getDefaultLockPath() {
            return Collections.singletonList(JOINER.join(project, module, resource1, resource2));
        }
    }
}
