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

package org.apache.kylin.engine.mr.steps;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.kylin.common.util.Bytes;

/**
 */
public class FactDistinctColumnPartitioner extends Partitioner<Text, Text> {
    private Configuration conf;

    @Override
    public int getPartition(Text key, Text value, int numReduceTasks) {

        long colIndex = Bytes.toLong(key.getBytes(), 0, Bytes.SIZEOF_LONG);
        if (colIndex < 0) {
            // the last reducer is for merging hll
            return numReduceTasks - 1;
        } else {
            return (int) (colIndex);
        }

    }

}
