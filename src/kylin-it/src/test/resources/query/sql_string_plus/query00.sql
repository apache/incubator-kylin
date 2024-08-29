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

select 1+2.3, '1'+2.1e-1, '1.3'+'-2.0211', 'a' + 1, 'a' + cast('1.33' as varchar), cast('a' as string) + cast('b' as char(1)), '1'+'2'
from TEST_KYLIN_FACT where TEST_KYLIN_FACT.TRANS_ID = 2
union all
select ORDER_ID + ORDER_ID,
cast(ORDER_ID as string) + ORDER_ID,
cast(ORDER_ID as char(100)) + cast(ORDER_ID as varchar),
LSTG_FORMAT_NAME + ORDER_ID,
LSTG_FORMAT_NAME + cast(cast(ORDER_ID as string) as varchar),
cast(LSTG_FORMAT_NAME as string) + cast(LSTG_FORMAT_NAME as char(9)),
cast(count(LSTG_FORMAT_NAME + cast(cast(ORDER_ID as string) as varchar)) as string) + LSTG_FORMAT_NAME
from TEST_KYLIN_FACT where TEST_KYLIN_FACT.TRANS_ID = 2 group by ORDER_ID, LSTG_FORMAT_NAME