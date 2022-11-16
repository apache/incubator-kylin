-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
SELECT
"TEST_KYLIN_FACT"."TRANS_ID" as "TEST_KYLIN_FACT_TRANS_ID",
TRUNCATE(PRICE, 1),
TRUNCATE(PRICE),
TRUNCATE(sum(PRICE)),
TRUNCATE(sum(PRICE),1),
TRUNCATE(sum(PRICE),10),
TRUNCATE(sum(PRICE),1+1),
TRUNCATE(sum(PRICE)-1.00001,0),
TRUNCATE(sum(PRICE)/3.3,10),
TRUNCATE(CAST(sum(PRICE) AS double),10),
TRUNCATE(cast(sum(PRICE) as float),0),
TRUNCATE(cast(TRANS_ID as smallint),0),
TRUNCATE(cast(TRANS_ID as tinyint),0),
TRUNCATE(cast(sum(PRICE) as float),2),
TRUNCATE(cast(sum(PRICE) as double),0),
TRUNCATE(cast(sum(PRICE) as double),2)
FROM
"DEFAULT"."TEST_KYLIN_FACT" as "TEST_KYLIN_FACT"
WHERE
1 = 1
and TRANS_ID < 100
group by "TEST_KYLIN_FACT"."TRANS_ID"
,PRICE
