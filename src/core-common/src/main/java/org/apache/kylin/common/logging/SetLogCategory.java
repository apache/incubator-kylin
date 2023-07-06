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
package org.apache.kylin.common.logging;

import java.io.Closeable;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.util.CollectionUtils;

public class SetLogCategory implements Closeable {

    private static final String LOG_CATEGORY = "logCategory";
    private static final ThreadLocal<Deque<String>> categoryThreadLocal = ThreadLocal.withInitial(LinkedList::new);

    // when category exist, and use new category to logger message, will push oldSetLogCategory
    public SetLogCategory(String category) {
        String oldCategory = ThreadContext.get(LOG_CATEGORY);
        ThreadContext.put(LOG_CATEGORY, category);
        if (StringUtils.isNotBlank(oldCategory)) {
            categoryThreadLocal.get().offerFirst(oldCategory);
        }
    }

    @Override
    public void close() {
        ThreadContext.remove(LOG_CATEGORY);
        if (!CollectionUtils.isEmpty(categoryThreadLocal.get())) {
            String oldCategory = categoryThreadLocal.get().pollFirst();
            if (StringUtils.isNotBlank(oldCategory)) {
                ThreadContext.put(LOG_CATEGORY, oldCategory);
            }
        }
    }
}
