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

--expected_model_hit=orders_model,orders_model,orders_model;
SELECT * FROM (SELECT (SELECT COUNT(DISTINCT O_TOTALPRICE) AS O_TOTALPRICE FROM TPCH.ORDERS_MODEL) AS O_TOTALPRICE
FROM TPCH.ORDERS_MODEL
GROUP BY O_TOTALPRICE) T1 INNER JOIN (SELECT O_TOTALPRICE FROM TPCH.ORDERS_MODEL) T2 ON T1.O_TOTALPRICE = T2.O_TOTALPRICE
