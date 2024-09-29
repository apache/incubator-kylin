---
title: Deployment Overview
language: en
sidebar_label: Deployment Overview
pagination_label: Deployment Overview
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - Intro
draft: false
last_update:
    date: 09/30/2024
---

This chapter will introduce how to deploy kylin on different mode.

To get started, let's first understand the main components of Kylin:

### 1. Metadata Management
Kylin support using MySQL or PostgresQL as metadata database. It stores all the metadata of Kylin System, including projects, tables, models, indexes, jobs, users etc.

Besides, some user's data like query history and audit log are also stored in the metadata database.

### 2. Data Storage Engine
Kylin support using HDFS or S3 compatible file system as the data storage engine.

Though you can re-use your data source hadoop cluster, we recommend using an independent HDFS cluster as the data storage component to get better performance.

#### 2.1 Local Cache
From version 5.0, Kylin support internal table and using Apache Gluten(Clickhouse backend) as native compute engine, which relies on local cache to accelerate query performance.   

The soft affinity of local cache is enabled by default, which means Kylin will automatically distribute the cached data files evenly across all the spark worker nodes in the cluster.

We recommend using SSD disk for local cache storage to get better performance.

### 3. Data Compute Engine
Kylin uses the built-in Apache Spark as the data compute engine. It can run on Yarn or Standalone mode.

You can find the download script in the `${KYLIN_HOME}/sbin` directory.

#### 3.1 Cache Re-use
As we introduced above in [2.1 Local Cache](#21-local-cache), Gluten use local cache to accelerate query performance. To reduce the cache loading time in first time launching, we recommend to set up an independent Spark Standalone cluster as the query compute cluster. So that Kylin(Gluten) can re-use the old cache data after a restart.

We don't recommend to run on hadoop yarn because the yarn container allocation is not under controlled. So the local cache re-use is not supported on yarn mode. 

While for data loading job, it's free for you to run on yarn or standalone cluster since it doesn't need cache re-use.

### 4. Kylin Server
Kylin Server is the front-end service and query client of Kylin. It provides a web UI for users to manage tables, design models, trigger jobs and maintain the configurations.

It has three server modes:

| Server Mode | Description                                                                                                        |
|-------------|--------------------------------------------------------------------------------------------------------------------|
| query       | query node only provides query service, all the job and transaction request will be transferd to job or all nodes. |
| job         | job node don't provide query service, but only served for job and transaction request.                             |
| all         | all node can serve all the query, job and transaction service.                                                     |

you can configure the `server.mode` property in the `kylin.properties` file.

#### 4.1 Query Node
One Kylin query node has a built-in spark application, which is named as 'Sparder'. Its driver runs inside the Kylin JVM and executors run on query cluster(Yarn or Spark Standalone).

#### 4.2 Job Node
Kylin data loading job support both yarn client or cluster mode. The difference is in client mode the driver process run on the Kylin node and will utilize some local resources accordingly.