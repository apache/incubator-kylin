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

package org.apache.kylin.common.persistence;

import lombok.Getter;

/**
 * overall, RawResource is immutable
 */
public class TombRawResource extends RawResource {

    @Getter
    private static TombRawResource INSTANCE = new TombRawResource();

    private MetadataType metaType;

    TombRawResource(String metaKey, MetadataType metaType) {
        super(metaKey, null, 0, 0);
        this.metaType = metaType;
    }

    @Override
    public MetadataType getMetaType() {
        return metaType;
    }

    private TombRawResource() {
        super(null, null, 0, 0);
    }
}
