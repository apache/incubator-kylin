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
select sum(ITEM_COUNT) as ITEM_CNT
FROM TEST_KYLIN_FACT as TEST_KYLIN_FACT
INNER JOIN TEST_ORDER as TEST_ORDER
ON TEST_KYLIN_FACT.ORDER_ID = TEST_ORDER.ORDER_ID
INNER JOIN TEST_ACCOUNT as BUYER_ACCOUNT
ON TEST_ORDER.BUYER_ID = BUYER_ACCOUNT.ACCOUNT_ID
INNER JOIN TEST_ACCOUNT as SELLER_ACCOUNT
ON TEST_KYLIN_FACT.SELLER_ID = SELLER_ACCOUNT.ACCOUNT_ID
INNER JOIN EDW.TEST_CAL_DT as TEST_CAL_DT
ON TEST_KYLIN_FACT.CAL_DT = TEST_CAL_DT.CAL_DT
INNER JOIN TEST_CATEGORY_GROUPINGS as TEST_CATEGORY_GROUPINGS
ON TEST_KYLIN_FACT.LEAF_CATEG_ID = TEST_CATEGORY_GROUPINGS.LEAF_CATEG_ID AND TEST_KYLIN_FACT.LSTG_SITE_ID = TEST_CATEGORY_GROUPINGS.SITE_ID
INNER JOIN EDW.TEST_SITES as TEST_SITES
ON TEST_KYLIN_FACT.LSTG_SITE_ID = TEST_SITES.SITE_ID
INNER JOIN EDW.TEST_SELLER_TYPE_DIM as TEST_SELLER_TYPE_DIM
ON TEST_KYLIN_FACT.SLR_SEGMENT_CD = TEST_SELLER_TYPE_DIM.SELLER_TYPE_CD
INNER JOIN TEST_COUNTRY as BUYER_COUNTRY
ON BUYER_ACCOUNT.ACCOUNT_COUNTRY = BUYER_COUNTRY.COUNTRY
INNER JOIN TEST_COUNTRY as SELLER_COUNTRY
ON SELLER_ACCOUNT.ACCOUNT_COUNTRY = SELLER_COUNTRY.COUNTRY
