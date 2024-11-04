---
title: 概述
language: zh-Hans
sidebar_label: 概述
sidebar_position: 0
pagination_label: 概述
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: quickstart/intro
keywords:
  - overview
draft: false
last_update:
  date: 10/24/2024
  author: Jiang Longfei
---

Apache Kylin 是一款领先的大数据开源 OLAP 引擎，能够对万亿条记录进行亚秒级查询。自2014年由 eBay 创建并开源以来，并于2015年毕业成为
Apache 软件基金会顶级项目。

Kylin 已迅速被全球数千家组织机构广泛使用，作为大数据的关键分析应用程序。

Kylin 具有以下主要优势：

- 高性能，高并发性，查询延迟亚秒级
- 统一的大数据仓库架构
- 与BI工具无缝集成
- 全面的企业级能力

## Kylin 5.0 新功能

### 1. 内表

Kylin 现在支持内表，专为灵活查询和湖仓设计。

> 更多详情，请参考 [内表](internaltable/intro.md)

### 2. 模型索引推荐

With recommendation engine, you don't have to be an expert of modeling. Kylin now can auto modeling and optimizing
indexes from you query history.
You can also create model by importing sql text.

> 更多详情，请参考 [自动建模](model/rec/sql_modeling.md) 和 [索引优化](model/rec/optimize_index/intro.md)

### 3. Native 计算引擎

Start from version 5.0, Kylin has integrated Gluten-Clickhosue Backend(incubating in apache software foundation) as
native compute engine. And use Gluten mergetree as the default storage format of internal table.
Which can bring 2~4x performance improvement compared with vanilla spark. Both model and internal table queries can get
benefits from the Gluten integration.

> 更多详情，请参考 [Gluten-Clickhosue Backend](https://github.com/apache/incubator-gluten)

### 4. 流式数据源

Kylin now support Apache Kafka as streaming data source of model building. Users can create a fusion model to implement
streaming-batch hybrid analysis.

## 重大变化

### 1. 元数据重构

在 Kylin 5.0 中，我们重构了元数据存储结构和事务处理流程，移除了项目锁和 Epoch 机制。这显著提升了事务接口性能和系统并发能力。

如果从 5.0
alpha、beta版本升级，请参考 [元数据迁移指南](operations/system-operation/cli_tool/metadata_operation.md#migration) 。

Kylin 4.0 升级使用的元数据迁移工具未经过测试，请联系 Kylin 用户或开发者邮件列表寻求帮助。

## 其他优化和改进

更多详情，请参考 [发行说明](release_notes.md) 。