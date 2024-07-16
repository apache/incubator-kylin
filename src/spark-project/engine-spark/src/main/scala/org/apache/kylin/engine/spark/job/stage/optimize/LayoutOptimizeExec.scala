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
package org.apache.kylin.engine.spark.job.stage.optimize

import java.util.concurrent.TimeUnit

import org.apache.kylin.engine.spark.job.SegmentExec.ResultType
import org.apache.kylin.engine.spark.job.stage.StageExec
import org.apache.kylin.guava30.shaded.common.collect.Queues

import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait LayoutOptimizeExec extends StageExec {

  protected final lazy val pipe = Queues.newLinkedBlockingQueue[Future[ResultType]]()

  def canSkip: Boolean

  def drain[T](timeout: Long = 1, unit: TimeUnit = TimeUnit.SECONDS): Unit = {
    val awaitPermission = null.asInstanceOf[scala.concurrent.CanAwait]
    do {
      val entry = pipe.poll(timeout, unit)
      entry.result(Duration.Inf)(awaitPermission).asInstanceOf[T]
    } while (!pipe.isEmpty)
  }
}