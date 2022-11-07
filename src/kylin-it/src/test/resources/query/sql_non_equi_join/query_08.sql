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
SELECT count(TEST_ACC.ACCOUNT_SELLER_LEVEL), TEST_ACC.ACCOUNT_BUYER_LEVEL
FROM
(
  select trim(ACCOUNT_COUNTRY) as ACCOUNT_COUNTRY, ACCOUNT_BUYER_LEVEL, ACCOUNT_SELLER_LEVEL
  FROM TEST_ACCOUNT
) TEST_ACC
LEFT JOIN
(
  SELECT trim(COUNTRY) as COUNTRY
  FROM TEST_COUNTRY
) TEST_COUNTRY
ON TEST_ACC.ACCOUNT_COUNTRY = TEST_COUNTRY.COUNTRY
and TEST_ACC.ACCOUNT_BUYER_LEVEL > 100
GROUP BY TEST_ACC.ACCOUNT_BUYER_LEVEL
ORDER BY 1,2
LIMIT 10000
