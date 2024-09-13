---
title: Query Specific Model
language: en
sidebar_label: Query Specific Model
pagination_label: Query Specific Model
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - model priority
draft: false
last_update:
    date: 08/17/2022
---

Query Hint is an advanced feature that enables users to specify **model priority**. When multiple models can answer a query, Kylin automatically selects the best one. However, users can override this default behavior by using a model priority hint in their SQL queries, allowing them to choose specific models for specific queries.

### Hint Syntax

``` sql
SELECT /*+ MODEL_PRIORITY(model1, model2) */ col1, col2
from table;
```
The hint starts with `/*+` and ends with `*/`. And the hint must be placed right after the `SELECT` clause.

`MODEL_PRIORITY(model1, model2, ...)` specifies the model priorities, `MODEL_PRIORITY(...)` accept a list of model names with descending priority. The model that doesn't appear in the hint will be assigned with the lowest priority.
The model priority hint will be valid for the entire query. Only the first hint occurred in the SQL will be valid.

During the model matching, if multiple model is capable to answer the query. kylin will use the model with the highest priority specified in the model priority hint

Supported starts from kylin 5.0


### Example

```sql
SELECT /*+ MODEL_PRIORITY(model1, model2) */ col1, col2 
from table;
```
If both model1 and model2 is capable to answer the query, model1 will be the chosen for this query.
