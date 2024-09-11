---
title: Query Cache
language: en
sidebar_label: Query Cache
pagination_label: Query Cache
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - query cache settings
draft: false
last_update:
    date: 08/16/2022
---
Query caching is a crucial optimization technique for enhancing query performance. By leveraging various caching mechanisms, you can improve query performance at multiple levels, including process, node, and cross-node. For hit-model queries, caching is enabled by default, but to ensure data consistency, it is disabled by default when querying the data source directly.
Note that system restart is required for any query cache configuration changes to take effect.

### Caching Criteria

Kylin selectively caches query results to optimize performance while managing memory resources. By default, it only caches slow queries with suitable result sizes. The caching criteria are defined by the following parameters:

**Conditions for Caching**: A query must meet at least one of the following conditions (No.1, No.2, or No.3) and also satisfy the condition in No.4 to be eligible for caching.

|No |  Properties                         | Description                                                  | Default        | Default unit |
| ----| ---------------------------------- | ------------------------------------------------------------ | -------------- | ------- |
| 1|kylin.query.cache-threshold-duration          | Queries whose duration is above this value | 2000           | millisecond |
| 2|kylin.query.cache-threshold-scan-count          | Queries whose scan row count is above this value | 10240           | row |
| 3|kylin.query.cache-threshold-scan-bytes          | Queries whose scan bytes is above this value | 1048576           | byte |
| 4|kylin.query.large-query-threshold          | Queries whose result set size is below this value  | 1000000           | cell |


### Ehcache Cache

**By default, Kylin utilizes Ehcache as the query cache at each node or process level.** To customize the configuration, update the settings in `$KYLIN_HOME/conf/kylin.properties` within your Kylin installation directory.

You can configure Ehcache to control the query cache size and policy by modifying the following configuration item:

* Replace the default query cache configuration
* Control query cache size
* Define cache policy

| Properties | Description | Default |
| ----- | ---- | ----- |
| kylin.query.cache-enabled | Whether to enable query cache. When this property is enabled, the following properties take effect. | true    |
| kylin.cache.config | The path to ehcache.xml. To replace the default query cache configuration file, you can create a new file `xml`, for exemple `ekcache2.xml`, in the directory  `${KYLIN_HOME}/conf/`, and modify the value of this configuration item: `file://${KYLIN_HOME}/conf/ehcache2.xml` | classpath:ehcache.xml |

For more Ehcache configuration items, please refer to the official website: [Ehcache Documentation](https://www.ehcache.org/generated/2.9.0/html/ehc-all/#page/Ehcache_Documentation_Set%2Fehcache_all.1.017.html%23)

### Redis Cache

The default query cache in Kylin is process-level, which means it cannot be shared across different nodes or processes. This limitation can lead to inefficiencies in cluster deployment mode, where subsequent identical queries may be routed to different Kylin nodes and cannot leverage the cache from the initial query.

To overcome this limitation, you can configure a Redis cluster as a distributed cache, enabling cache sharing across all Kylin nodes. For optimal performance, we recommend using Redis 5.0 or 5.0.5.

| Properties                         | Description                                                  | Default        | Options |
| ---------------------------------- | ------------------------------------------------------------ | -------------- | ------- |
| kylin.cache.redis.enabled          | Whether to enable query cache by using Redis cluster.         | false          | true    |
| kylin.cache.redis.cluster-enabled  | Whether to enable Redis cluster mode.                         | false          | true    |
| kylin.cache.redis.hosts             | Redis host. If you need to connect to a Redis cluster, please use comma to split the hosts, such as, kylin.cache.redis.hosts=localhost:6379,localhost:6380 | localhost:6379 |         |
| kylin.cache.redis.expire-time-unit | Time unit for cache period. EX means seconds and PX means milliseconds. | EX             | PX      |
| kylin.cache.redis.expire-time      | Valid cache period.                                           | 86400          |         |
| kylin.cache.redis.reconnection.enabled | Whether to enable redis reconnection when cache degrades to ehcache | true | false |
| kylin.cache.redis.reconnection.interval | Automatic reconnection interval, in minutes | 60 | |
| kylin.cache.redis.password | Redis password | | |

**Limitation：**Due to metadata inconsistency between Query nodes and All/Job nodes, the redis cache swith `kylin.cache.redis.enabled=true` should be configured along with `kylin.server.store-type=jdbc`. 

:::info Caution
Redis passwords can be encrypted, please refer to: [Use MySQL as Metastore](../deployment/on-premises/rdbms_metastore/use_mysql_as_metadb.md)
:::