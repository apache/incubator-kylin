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
SELECT FACT1.CAL_DT, TEST_ACCOUNT.ACCOUNT_ID, SUM(FACT1.ITEM_COUNT)
FROM
(
  SELECT SELLER_ID, ITEM_COUNT, CAL_DT
  FROM TEST_KYLIN_FACT
  WHERE SELLER_ID > 10000037
) FACT1
LEFT JOIN
(
SELECT *
FROM TEST_ACCOUNT
WHERE ACCOUNT_ID < 10000100
) TEST_ACCOUNT
ON FACT1.SELLER_ID = TEST_ACCOUNT.ACCOUNT_ID
GROUP BY FACT1.CAL_DT, TEST_ACCOUNT.ACCOUNT_ID
