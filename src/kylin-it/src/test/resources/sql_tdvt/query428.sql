--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

SELECT "t0"."TEMP_Test__2731970693__3_" AS "TEMP_Test__2731970693__3_"
FROM (
  SELECT "CALCS"."STR1" AS "STR1",
    SUM("CALCS"."NUM1") AS "TEMP_Test__2731970693__3_",
    SUM("CALCS"."NUM1") AS "X_measure__0"
  FROM "TDVT"."CALCS" "CALCS"
  GROUP BY "CALCS"."STR1"
) "t0"
GROUP BY "t0"."TEMP_Test__2731970693__3_"
