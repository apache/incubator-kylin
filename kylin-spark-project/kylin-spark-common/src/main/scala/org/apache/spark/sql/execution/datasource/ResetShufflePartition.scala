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
package org.apache.spark.sql.execution.datasource

import org.apache.kylin.common.{KylinConfig, QueryContextFacade}
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.utils.SparderUtils

trait ResetShufflePartition extends Logging {
  val PARTITION_SPLIT_BYTES: Long =
    KylinConfig.getInstanceFromEnv.getQueryPartitionSplitSizeMB * 1024 * 1024 // 64MB

  def setShufflePartitions(bytes: Long, sparkSession: SparkSession): Unit = {
    QueryContextFacade.current().addAndGetSourceScanBytes(bytes)
    val defaultParallelism = SparderUtils.getTotalCore(sparkSession.sparkContext.getConf)
    val kylinConfig = KylinConfig.getInstanceFromEnv
    val partitionsNum = if (kylinConfig.getSparkSqlShufflePartitions != -1) {
      kylinConfig.getSparkSqlShufflePartitions
    } else {
      Math.min(QueryContextFacade.current().getSourceScanBytes / PARTITION_SPLIT_BYTES + 1,
        defaultParallelism).toInt
    }
    // when hitting cube, this will override the value of 'spark.sql.shuffle.partitions'
    sparkSession.conf.set("spark.sql.shuffle.partitions", partitionsNum.toString)
    logInfo(s"Set partition to $partitionsNum, " +
      s"total bytes ${QueryContextFacade.current().getSourceScanBytes}")
  }
}
