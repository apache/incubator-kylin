---
title: Overview
language: en
sidebar_label: Overview
sidebar_position: 0
pagination_label: Overview
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: quickstart/intro
keywords:
    - overview
draft: false
last_update:
    date: 09/27/2024
---

Apache Kylin is a leading open source OLAP engine for Big Data capable for sub-second query latency on trillions of records. Since being created and open sourced by eBay in 2014, and graduated to Top Level Project of Apache Software Foundation in 2015.
Kylin has quickly been adopted by thousands of organizations world widely as their critical analytics application for Big Data.

Kylin has following key strengths:

- High qerformance, high concurrency, sub-second query latency
- Unified big data warehouse architecture
- Seamless integration with BI tools
- Comprehensive and enterprise-ready capabilities

## New Features in Kylin 5.0

### 1. Internal Table
Kylin now support internal table, which is designed for flexible query and lakehouse scenarios.

>More details, please refer to [Internal Table](internaltable/intro.md)

### 2. Model & Index Recommendation

With recommendation engine, you don't have to be an expert of modeling. Kylin now can auto modeling and optimizing indexes from you query history.
You can also create model by importing sql text.

>More details, please refer to [Auto Modeling](model/rec/sql_modeling.md) and [Index Optimization](model/rec/optimize_index/intro.md)

### 3. Native Compute Engine

Start from version 5.0, Kylin has integrated Gluten-Clickhosue Backend(incubating in apache software foundation) as native compute engine. And use Gluten mergetree as the default storage format of internal table.
Which can bring 2~4x performance improvement compared with vanilla spark. Both model and internal table queries can get benefits from the Gluten integration.

>Know more about [Gluten-Clickhosue Backend](https://github.com/apache/incubator-gluten)

### 4. Streaming Data Source

Kylin now support Apache Kafka as streaming data source of model building. Users can create a fusion model to implement streaming-batch hybrid analysis.

## Significant Change
### 1. Metadata Refactory
In Kylin 5.0, we have refactored the metadata storage structure and the transaction process, removed the project lock and Epoch mechanism. This has significantly improved transaction interface performance and system concurrency capabilities.

To upgrade from 5.0 alpha, beta, follow the [Metadata Migration Guide](operations/system-operation/cli_tool/metadata_operation.md#migration)

The metadata migration tool for upgrading from Kylin 4.0 is not tested, please contact kylin user or dev mailing list for help.

## Other Optimizations and Improvements
Please refer to [Release Notes](release_notes.md) for more details.