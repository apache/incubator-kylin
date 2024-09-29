---
title: Gluten Configuration
language: en
sidebar_label: Gluten Configuration
pagination_label: Gluten Configuration
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - Basic Configuration
draft: false
last_update:
    date: 09/30/2024
---

This chapter introduces gluten configuration

:::warning Important
In Kylin both query and build use Apache Spark as compute engine, but they use different configurations.<br></br>
For query engine, you should add the prefix `kylin.storage.columnar.spark-conf`.<br></br>
For build engine, add prefix `kylin.engine.spark-conf`.
:::

| Key                                                                                                | Default Value                      | Description                                                                                |
|----------------------------------------------------------------------------------------------------|------------------------------------|--------------------------------------------------------------------------------------------|
| spark.gluten.enabled                                                                               | true                               | Open or close gluten in spark                                                              |
| spark.gluten.sql.columnar.libpath                                                                  | `${KYLIN_HOME}/server/libch.so`    | `libch.so` path for spark driver                                                           |
| spark.memory.offHeap.enabled                                                                       | true                               |                                                                                            |
| spark.memory.offHeap.size                                                                          | 12g                                |                                                                                            |
| spark.gluten.sql.columnar.backend.ch.runtime_config.storage_configuration.disks.hdfs.endpoint      |                                    | Gluten hdfs name service                                                                   |
| spark.gluten.sql.columnar.backend.ch.runtime_config.storage_configuration.disks.hdfs.metadata_path | /tmp/ch_metadata_kylin             | Gluten metadata storage path, need read/write access                                       |
| spark.gluten.sql.columnar.backend.ch.runtime_config.storage_configuration.disks.hdfs_cache.path    | /tmp/hdfs_cache_kylin              | Gluten cache path, need read/write access                                                  |
| spark.gluten.sql.columnar.backend.ch.runtime_config.storage_configuration.disks.hdfs_cache.max_size | 256Gi                              | Gluten local cache size                                                                    |
| spark.gluten.sql.columnar.backend.ch.runtime_config.path                                           | /tmp/gluten_default                | Gluten data processing path，need read/write access                                         |
| spark.gluten.sql.columnar.backend.ch.runtime_config.tmp_path                                       | /tmp/kyligence_glt/tmp_ch          | Gluten data processing tmp path，need read/write access                                     |
| spark.gluten.sql.columnar.backend.ch.runtime_config.use_current_directory_as_tmp                   | true                               | use current relative path as data processing tmp path, should be "True" on yarn            |
| spark.gluten.sql.columnar.backend.ch.runtime_config.hdfs.libhdfs3_conf                             | /etc/hadoop/conf/hdfs-site.xml     | haddop_conf path in executor container                                                     |
| spark.gluten.sql.columnar.executor.libpath                                                         | libch.so                           | `libch.so` file path in executor，should be `$SPARK_HOME/libch.so` in spark standalone mode |
| spark.executorEnv.LD_PRELOAD=$PWD/libch.so                                                         | $PWD/libch.so                      | executor environment variable， should be `$SPARK_HOME/libch.so` in spark standalone mode   |
| spark.gluten.sql.executor.jar.path                                                                 | `${KYLIN_HOME}/lib/ext/gluten.jar` | `gluten.jar` file path                                                                     |
| spark.gluten.sql.columnar.backend.ch.runtime_config.reuse_disk_cache                               | false                              | re-use local cache or not                                                                  |
| spark.gluten.sql.columnar.backend.ch.runtime_config.hdfs.hadoop_security_authentication            | NONE                               | authentication of Gluten data reading，`KERBEROS` or `NONE`                                 |