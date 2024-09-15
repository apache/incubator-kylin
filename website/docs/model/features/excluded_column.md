---
title: Query History Recommendation
language: en
sidebar_label: Query History Recommendation
pagination_label: Query History Recommendation
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - optimization
draft: false
last_update:
    date: 09/18/2024
---

Exclude Column

 * For join relation, in snowflake model, A join B(on a1 = b1) and B join C (on b2 = c2), the foreign key b2 cannot be a exclude column, otherwise the join chain will be broken when proposing recommendations.
 * The design of computed columns indicates that precomputation is required, while the design of exclude columns indicates that precomputation is not required. So these two are incompatible with each other. Therefore, it is not recommended to define the computed columns contains exclude columns in principle. In particular, computed columns and measures that dependent on exclude columns  may result in incorrect data if they hit the index. 
 * Computed columns dependent on excluded columns are not reused for the intelligent recommendation. Moreover, with the setting of `kylin.metadata.only-reuse-user-defined-computed column=true`(by default false), only the customized computed columns will be used for recommendations, that is to say, no more computed columns started with `CC_AUTO` will be created in the model.
 * For queries, by default indexes answer the query with a higher priority than using the foreign key and snapshot. After the exclude column is enabled and setting `kylin.query.snapshot-preferred-for-table-exclusion=true`, the system preferentially selects the foreign key index to runtime join snapshot to response the query. When the '**Exclude Column Rule Setting**' is enabled, this is the default behavior. However, by setting it to false, index independent on exclude columns will be preferred to answer. For example, the model is A join B (on a1 = b1), the index1 is `{A.a1, A.a3}`, the index2 is `{A.a3, B.b1}`, and the query is `select A.a3, B.b1 from A join B on a1 = b1`, if the  '**Exclude Column Rule Setting**' is enabled, this query will hit index1 to runtime join snapshot of B, but if `kylin.query.snapshot-preferred-for-table-exclusion=false` this query will hit index2.
