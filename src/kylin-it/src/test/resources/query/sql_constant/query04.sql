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

select to_date(date '2023-06-08'),
       to_date('2023-06-08', 'yyyy-MM-dd'),
       to_date('2023-06-08', 'yyyy-MM'),
       to_date('2023-06-08', 'yyyy'),
       to_date('20230608', 'yyyyMMdd'),
       to_date('2023-06-08'),
       to_timestamp(timestamp '2023-06-08 23:24:25'),
       to_timestamp('2023-06-08 23:24:25'),
       to_timestamp('2023-06-08 23:24:25', 'yyyy-MM-dd HH:mm'),
       to_timestamp('2023-06-08 23:24:25', 'yyyy-MM-dd HH'),
       to_timestamp('2023-06-08 23:24:25', 'yyyy-MM-dd'),
       to_timestamp('2023-06-08 23:24:25', 'yyyy-MM'),
       to_timestamp('2023-06-08 23:24:25', 'yyyy')
