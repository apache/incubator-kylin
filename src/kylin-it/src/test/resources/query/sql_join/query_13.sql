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
        SUM(KYLIN_ITEM_COUNT)
    FROM
    (
            SELECT TEST_KYLIN_FACT.SELLER_ID + TEST_KYLIN_FACT.LSTG_SITE_ID AS S, CAL_DT, ITEM_COUNT as KYLIN_ITEM_COUNT
            FROM TEST_KYLIN_FACT
            ) B
    LEFT JOIN
        (  SELECT
            TEST_KYLIN_FACT.SELLER_ID,
            SUM(TEST_KYLIN_FACT.ITEM_COUNT),
            CAL_DT
        FROM
            TEST_KYLIN_FACT
        GROUP BY
            TEST_KYLIN_FACT.SELLER_ID,
            CAL_DT
            ) A
          ON A.SELLER_ID = B.S AND A.CAL_DT = B.CAL_DT
