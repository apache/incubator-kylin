---
title: Intelligent Recommendation Principles
language: en
sidebar_label: Intelligent Recommendation Principles
pagination_label: Intelligent Recommendation Principles
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: modeling/auto_modeling/intro
pagination_next: null
keywords:
    - recommendation
draft: false
last_update:
    date: 09/02/2024
---

Kylin Intelligent Recommendation function, covers the complete model design process, including creating models, generating indexes, and continuously delivering optimization recommendations. It ensures the model structures and indexes adapt to the ever-changing business needs. This article will introduce the principles of Intelligent Recommendation.

### Modeling based on SQL 

Kylin automatically parses your SQL statements for dimensions, measures, and other critical information, and then guides you through the modeling process. Moreover, Kylin will suggest converting existing models that match certain SQL queries into recommendations, to help control model numbers.

You can enter SQL statements in either of the following ways: 

- [Web UI](data_modeling_by_SQL.md): Upload the SQL statements as a TXT or SQL file.

- [API](../../restapi/model_api/intro.md#sql): Call an API to upload the SQL statements as parameter values. 

### Index optimization based on query history

Kylin recommendation engine optimizes indexes based on business data characteristics and user queries. It also combines a scoring mechanism and a set of rules to push the analysis results as optimization recommendations. 

#### Scoring mechanism 

Kylin uses a built-in scoring mechanism to evaluate the potential value of indexes. Indexes with a high score will be temporarily recorded in the internal system and then filtered based on the recommendation generation rules. The following table lists the scoring mechanism that Kylin supports. 

| **Scoring Mechanism**                                        | **Effective condition**                                                                                                  | **Formula**                               | Description                                                  |
| :----------------------------------------------------------- |:-------------------------------------------------------------------------------------------------------------------------| :---------------------------------------- | :----------------------------------------------------------- |
| Based on query times within the latest time window (default) | Supported from version 5.0 onwards                                                                                       | `$$Revenue = \sum_{timeWindow}queryCount$$` | ● **Revenue** is the potential benefit of accepting the recommendations.<br />● **queryCount** is the query times of the recommendation. |
| Based on the weighted average duration from query history    | The value of parameter  `kylin.smart.update-cost-method` should be changed to `TIME_DECAY` in file **kylin.properties**. | `$$Revenue = \Sigma t_k \times e^{-k}$$ `   | ● **Revenue** is the potential benefit of accepting the recommendations.<br /> ● **tk** is the average duration of the query that hits this pattern on the kth day. |

#### Recommendation generation rules

After the above scoring, Kylin will filter indexes based on:

| **Rule**    | **Description**                                              |
| ----------- | ------------------------------------------------------------ |
| System rule | Queries that already exactly hit the index or cannot be matched with existing models will be filtered out to improve index usage. |
| Custom rule | You can also filter indexes based on query duration, hit rules, number of recommendations, etc. For details, please refer to [Set recommendation rules](rule_setting.md). |

#### Recommendation types

Kylin mainly provides recommendations on index addition or deletion, and related dimension, measure, and computed column adjustments. Click the **Model List** page to check and evaluate whether to accept the pushed recommendations.  

| Type           | Description                                                                                                                                                                                                                  |
| -------------- |------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Add indexes    | Add indexes that match queries exactly to reduce response times for related queries.                                                                                                                                         |
| Delete indexes | Delete redundant or low-frequency indexes to save storage capacity or lower model expansion rate. For more information, see [Index Optimization Strategies](optimize_index/index_optimization.md). |

### FAQ

- Question: When system resources are limited, which parameters can I use to control the resources consumed by Intelligent Recommendation?

  Answer: You can reduce the resources that Intelligent Recommendation uses by lowering the value of the following two parameters in system [configuration file](../../configuration/configuration.md). 

  ```
  # This parameter is used for controlling the maximum queries processed by each scheduled task.
  kylin.favorite.query-history-accelerate-max-size=100000
  
  # This parameter is used for controlling the maximum queries processed by each batch.
  kylin.query.query-history-stat-batch-size=1000
  ```

