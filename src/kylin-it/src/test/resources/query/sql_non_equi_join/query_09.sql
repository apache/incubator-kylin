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
-- non-equi join with equi joins, same first table
SELECT count(TEST_ACCOUNT.ACCOUNT_SELLER_LEVEL), TEST_ACCOUNT.ACCOUNT_BUYER_LEVEL
FROM
TEST_ACCOUNT
LEFT JOIN
(
  select TEST_COUNTRY.COUNTRY as COUNTRY, ACCOUNT_BUYER_LEVEL, ACCOUNT_SELLER_LEVEL
  FROM TEST_ACCOUNT
  LEFT JOIN TEST_COUNTRY
  ON TEST_ACCOUNT.ACCOUNT_COUNTRY = TEST_COUNTRY.COUNTRY
) TEST_ACC
ON
TEST_ACCOUNT.ACCOUNT_COUNTRY = TEST_ACC.COUNTRY
or TEST_ACCOUNT.ACCOUNT_BUYER_LEVEL > 100
GROUP BY TEST_ACCOUNT.ACCOUNT_BUYER_LEVEL
ORDER BY 1,2
LIMIT 10000
