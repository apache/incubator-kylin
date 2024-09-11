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
    date: 09/13/2022
---

Apache Kylin is a leading open source OLAP engine for Big Data capable for sub-second query latency on trillions of records. Since being created and open sourced by eBay in 2014, and graduated to Top Level Project of Apache Software Foundation in 2015.
Kylin has quickly been adopted by thousands of organizations world widely as their critical analytics application for Big Data.

Kylin has following key strengths:

- High Performance, Sub-second Query Latency
- Unified Big Data Warehouse Architecture
- Seamless Integration with BI tools
- Comprehensive and Enterprise-ready Capabilities

## New Features in Kylin 5.0

### 1. Internal Table
Kylin now support internal table, which is designed for flexible query and lakehouse scenarios.

>More details, please refer to [Internal Table](internaltable/intro.md)

### 2. Recommendation Engine

With recommendation engine, you don't have to be an expert of modeling. Kylin now can auto modeling and optimizing indexes from you query history.
You can also create model by importing sql text.

>More details, please refer to [Auto Modeling](modeling/auto_modeling/intro.md) and [Index Optimization](modeling/auto_modeling/optimize_index/intro.md)

### 3. Native Compute Engine

Start from version 5.0, Kylin has integrated Gluten-Clickhosue Backend(incubating in apache software foundation) as native compute engine. And use Gluten mergetree as the default storage format of internal table.
Which can bring 3~4x performance improvement compared with vanilla spark. Both model and internal table queries can get benefits from the Gluten integration.

>Know more about [Gluten-Clickhosue Backend](https://github.com/apache/incubator-gluten)

### 4. Streaming Data Source

Kylin now support Apache Kafka as streaming data source of model building. Users can create a fusion model to implement streaming-batch hybrid analysis.

## Other Optimizations and Improvements