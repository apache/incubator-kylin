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



SELECT log1p(0),
       log1p(abs(item_count) * abs(cast(price AS bigint)) + 1)
FROM test_kylin_fact
GROUP BY log1p(abs(item_count) * abs(cast(price AS bigint)) + 1),
         item_count,
         price
ORDER BY item_count,
         price LIMIT 10;
