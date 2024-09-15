---
title: Scalar Subquery
language: en
sidebar_label: Scalar Subquery
pagination_label: Scalar Subquery
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - recommendation
    - rule
draft: false
last_update:
    date: 09/02/2024
---

In many business data analysis scenarios, constant subquery is a common SQL usage.
You can start the recommended optimization for scenarios where constant subqueries are included in SQL join relationships through the following configuration. This parameter supports to configure at system-level or project-level:

```properties
## The default value of the parameter is false, which means the function is not enabled.
kylin.query.scalar-subquery-join-enabled = true
```

### How to Use

The following takes the sample data set of Kylin as an example. For more information about the sample data set, please refer to [Sample Dataset](../../quickstart/tutorial.md).

```sql
SELECT D_DATEKEY, DATE_TIME, count(*), sum(D_YEAR) sy
FROM (SELECT '1995-03-01' AS DATE_TIME
      UNION ALL
      SELECT '1995-03-02' AS DATE_TIME
      UNION ALL
      SELECT '1995-03-03' AS DATE_TIME) t1
         LEFT JOIN SSB.DATES t2 ON t2.D_DATEKEY <= t1.DATE_TIME 
         AND t2.D_DATEKEY >= CONCAT(SUBSTR(t1.DATE_TIME, 1, 8), '01')
GROUP BY D_DATEKEY, DATE_TIME
```

Kylin can not generate an index containing the measure `SUM(D_YEAR)` through recommendation engine. However, with the switch turned on, an index containing the dimension `D_DATEKEY` and the measure `SUM(D_YEAR)`, `count(*)` can be generated, achieving SQL acceleration.

### Known Limitation

1. When the aggregate function is `count distinct`, index recommendation and SQL modeling are not currently supported. For example, when `sum(D_YEAR)` becomes `count(distinct D_YEAR)` in the above example, it cannot be recommended;
2. When the parameter of the aggregate function is an expression, index recommendation and SQL modeling are not currently supported. For example, when `sum(D_YEAR)` becomes `sum(D_YEAR+1)` in the above example, it cannot be recommended.
