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

select 100.00 * sum(case
		when p_type like 'PROMO%'
			then l_extendedprice
		else 0
	end) / sum(l_extendedprice * (1 - l_discount)) as promo_revenue
from
	tpch.lineitem inner join tpch.part on l_partkey = p_partkey
where
	l_shipdate >= '1995-08-01'
	and l_shipdate < '1995-09-01';
