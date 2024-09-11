---
title: Basic Configuration
language: en
sidebar_label: Basic Configuration
pagination_label: Basic Configuration
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - Basic Configuration
draft: false
last_update:
    date: 09/12/2024
---

This chapter provides an in-depth look at frequently used configurations in Kylin. The following sections outline the main topics covered:

### Kylin Properties File

The `kylin.properties` file is a crucial configuration file in Kylin, containing key settings that impact the application's behavior. This section provides an in-depth explanation of commonly used properties, enabling you to optimize your Kylin setup.

| Properties                                                   | Description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| kylin.circuit-breaker.threshold.model                        | The maximum number of models allowed to be created in a single project, the default value is `100` |
| kylin.circuit-breaker.threshold.project                      | The maximum number of projects allowed to be created, the default value is `100` |
| kylin.circuit-breaker.threshold.query-result-row-count       | This parameter is the maximum number of rows in the result set returned by the SQL query. The default is `2000000`. If the maximum number of rows is exceeded, the backend will throw an exception |
| kylin.diag.task-timeout                                      | The subtask timeout time for the diagnostic package, whose default value is 3 minutes |
| kylin.diag.task-timeout-black-list                           | Diagnostic package subtask timeout blacklist (the values are separated by commas). The subtasks in the blacklist will be skipped by the timeout settings and will run until it finished. The default value is `METADATA`, `LOG` <br />The optional value is as below: <br />METADATA, AUDIT_LOG, CLIENT, JSTACK, CONF, HADOOP_CONF, BIN, HADOOP_ENV, CATALOG_INFO, SYSTEM_METRICS, MONITOR_METRICS, SPARK_LOGS, SPARDER_HISTORY, KG_LOGS, LOG, JOB_TMP, JOB_EVENTLOGS |
| kylin.diag.obf.level                                         | The desensitization level of the diagnostic package. `RAW` means no desensitization, `OBF` means desensitization. Configuring `OBF` will desensitize sensitive information such as usernames and passwords in the `kylin.properties` file (please refer to the [Diagnosis Kit Tool](../operations/system-operation/cli_tool/diagnosis.md) chapter), The default value is `OBF`. |
| kylin.engine.sanity-check-enabled                            | Configure Kylin whether to open Sanity Check during indexes building. The default value is `true` |
| kylin.engine.spark-conf.spark.driver.host                    | Configure the IP of the node where the Kylin is located |
| kylin.engine.spark-conf.spark.sql.view-truncate-enabled=true | Allow spark view to lose precision during construction, the default value is false |
| kylin.env                                                    | The usage of the Kylin instance is specified by this property. Optional values include `DEV`, `PROD` and `QA`, among them `PROD` is the default one. In `DEV` mode some developer functions are enabled. |
| kylin.env.ip-address                                         | When the network address of the node where the Kylin service is located has the ipv6 format, you can specify the ipv4 format through this configuration item. The default is `0.0.0.0` |
| kylin.env.hdfs-working-dir                                   | Working path of Kylin instance on HDFS is specified by this property. The default value is `/kylin` on HDFS, with table name in metadata path as the sub-directory. For example, suppose the metadata path is `kylin_metadata@jdbc`, the HDFS default path should be `/kylin/kylin_metadata`. Please make sure the user running Kylin instance has read/write permissions on that directory. |
| kylin.env.zookeeper-connect-string                           | This parameter specifies the address of ZooKeeper. There is no default value. **This parameter must be manually configured before starting Kylin instance**, otherwise Kylin will not start. |
| kylin.garbage.storage.cuboid-layout-survival-time-threshold  | This property specifies the threshold of invalid files on HDFS. When executing the command line tool to clean up the garbage, invalid files on HDFS that exceed this threshold will be cleaned up. The default value is `7d`, which means 7 days. Invalid files on HDFS include expired indexes, expired snapshots, expired dictionaries, etc. At the same time, indexes with lower cost performance will be cleaned up according to the index optimization strategy. |
| kylin.garbage.storage.executable-survival-time-threshold     | This property specifies the threshold for the expired job. The metadata of jobs that have exceeded this threshold and have been completed will be cleaned up. The default is `30d`, which means 30 days. |
| kylin.influxdb.address                                       | This property specifies the address of InfluxDB. The default is `localhost:8086`. |
| kylin.influxdb.password                                      | This property specifiess the password of InfluxDB. The default is `root`. |
| kylin.influxdb.username                                      | This property specifies the username of InfluxDB. The defaul is `root`. |
| kylin.job.finished-notifier-url                              | When the building job is completed, the job status information will be sent to the url via HTTP request |
| kylin.job.max-concurrent-jobs                                | Kylin has a default concurrency limit of **20** for jobs in a single project. If there are already too many running jobs reaching the limit, the new submitted job will be added into job queue. Once one running job finishes, jobs in the queue will be scheduled using FIFO mechanism. |
| kylin.job.retry                                              | This property specifies the auto retry times for error jobs. The default value is 0, which means job will not auto retry when it's in error. Set a value greater than 0 to enable this property and it applies on every step within a job and it will be reset if that step is finished. |
| kylin.job.retry-interval                                     | This property specifies the time interval to retry an error job and the default value is `30000` ms. This property is valid only when the job retry property is set to be 1 or above. |
| kylin.metadata.url                                           | Kylin metadata path is specified by this property. The default value is `kylin_metadata` table in PostgreSQL while users can customize it to store metadata into any other table. When deploying multiple Kylin instances on a cluster, it's necessary to specify a unique path for each of them to guarantee the isolation among them. For example, the value of this property for Production instance could be `kylin_metadata_prod`, while that for staging instance could be `kylin_metadata_staging`, so that Production instance wouldn't be interfered by operations on staging instance. |
| kylin.metadata.ops-cron                                      | This parameter specifies the timing task cron expression for timed backup metadata and garbage cleanup. The default value is `0 0 0 * * *`. |
| kylin.metadata.audit-log.max-size                            | This parameter specifies the maximum number of rows in the audit-log. The default value is `500000`. |
| kylin.metadata.compress.enabled                              | This parameter specifies whether to compress the contents of metadata and audit log. The default value is `true`. |
| kylin.metrics.influx-rpc-service-bind-address                | If the property `# bind-address = "127.0.0.1:8088"` was modified in the influxdb's configuration file, the value of this should be modified at the same time. This parameter will influence whether the diagnostic package can contain system metrics. |
| kylin.query.async-query.max-concurrent-jobs                  | When configuring the asynchronous query queue, the maximum number of asynchronous query jobs. When the number of jobs reaches the limit, the asynchronous query reports an error. The default value  is 0, which means there is no limit to the number of asynchronous query jobs. |
| kylin.query.auto-model-view-enabled                          | Automatically generate views for model. When the config is on, a view will be generated for each model and user can query on that view. The view will be named with {project_name}.{model_name} and contains all the tables defined in the model and all the columns referenced by the dimension and measure of the table. |
| kylin.query.convert-create-table-to-with                     | Some BI software will send Create Table statement to create a permanent or temporary table in the data source. If this setting is set to `true`, the create table statement in the query will be converted to a with statement, when a later query utilizes the table that the query created in the previous step, the create table statement will be converted into a subquery, which can hit on an index if there is one to serve the query. |
| kylin.query.engine.spark-scheduler-mode                      | The scheduling strategy of query engine whose default is FAIR (Fair scheduler). The optional value is SJF (Smallest Job First scheduler). Other value is illegal and FAIR strategy will be used as the default strategy. |
| kylin.query.force-limit                                      | Some BI tools always send query like `select * from fact_table`, but the process may stuck if the table size is extremely large. `LIMIT` clause helps in this case, and setting the value of this property to a positive integer make Kylin append `LIMIT` clause if there's no one. For instance the value is `1000`, query `select * from fact_table` will be transformed to `select * from fact_table limit 1000`. This configuration can be overridden at **project** level. |
| kylin.query.init-sparder-async                               | The default value is `true`，which means that sparder will start asynchronously. Therefore, the Kylin web service and the query spark service will start separately; If set to `false`, the Kylin web service will be only available after the sparder service has been started. |
| kylin.query.layout.prefer-aggindex                           | The default value is `true`, which means that when index comparison selections are made for aggregate indexes and detail indexes, aggregate indexes are preferred. |
| kylin.query.match-partial-inner-join-model                   | The default value is `false`, which means that the multi-table inner join model does not support the SQL which matches the inner join part partially. For example: Assume there are three tables A, B, and C . By default, the SQL `A inner join B` can only be answered by the model of A inner join B or the model of A inner join B left join C. The model of A inner join B inner join C cannot answer it. If this parameter is set to `true`, the SQL of A inner join B can be answered with the model of A inner join B or A inner join B left join C, or it can also be answered with the model of A inner join B inner join C. |
| kylin.query.match-partial-non-equi-join-model                | default to `false` ，currently if the model contains non-equi joins, the query can be matched with the model only if it contains all the non-equi joins defined in the model. If the config is set to `true`, the query is allowed to contain only part of the non-equi joins. e.g. model: A left join B non-equi left join C. When the config is set to `false`, only query with the complete join relations of the model can be matched with the model. When the config is set to `true`, query like A left join B can also be matched with the model. |
| kylin.query.max-result-rows                                  | This property specifies the maximum number of rows that a query can return. This property applies on all ways of executing queries, including Web UI, Asynchronous Query, JDBC Driver and ODBC Driver. This configuration can be overridden at **project** level. For this property to take effect, it needs to be a positive integer less than or equal to 2147483647. The default value is 0, meaning no limit on the result. <br />Below is the priority:<br />SQL limit > min(前端 limit, kylin.query.max-result-rows) > kylin.query.force-limit |
| kylin.query.memory-limit-during-collect-mb                   | Limit the memory usage when getting query result in Kylin，the unit is megabytes, defaults to 5400mb |
| kylin.query.queryhistory.max-size                            | The total number of records in the query history of all projects, the default is 10000000 |
| kylin.query.queryhistory.project-max-size                    | The number of records in the query history retained of a single project, the default is 1000000 |
| kylin.query.queryhistory.survival-time-threshold             | The number of records in the query history retention time of all items, the default is 30d, which means 30 days, and other units are also supported: millisecond ms, microsecond us, minute m or min, hour h |
| kylin.query.realization.chooser.thread-core-num              | The number of core threads of the model matching thread pool in the query engine, the default is 5. It should be noted that when the number of core threads is set to less than 0, this thread pool will be unavailable, which will cause the entire query engine to be unavailable |
| kylin.query.realization.chooser.thread-max-num               | The maximum number of threads in the model matching thread pool in the query engine, the default is 50. It should be noted that when the maximum number of threads is set to be less than or equal to 0 or less than the number of core threads, this thread pool will be unavailable, which will cause the entire query engine to be unavailable |
| kylin.query.replace-count-column-with-count-star             | The default value is `false` , which means that COUNT(column) measure will hit a model only after it has been set up in the model. If COUNT(column) measure is called in SQL while not having been set up in the model, this parameter value can be set to `true`, then the system will use COUNT(constant) measure to replace COUNT(column) measure approximately. COUNT(constant) measure takes all Null value into calculation. |
| kylin.query.replace-dynamic-params-enabled                   | Whether to enable dynamic parameter binding for JDBC query, the default value is false, which means it is not enabled. For more, please refer to [Kylin JDBC Driver](#TODO) |
| kylin.query.spark-job-trace-enabled                          | Enable the job tracking log of spark. Record additional information about spark: Submission waiting time, execution waiting time, execution time and result acquisition time are displayed in the timeline of history. |
| kylin.query.spark-job-trace-timeout-ms                       | Only for the job tracking log of spark. The longest waiting time of query history. If it exceeds, the job tracking log of spark will not be recorded. |
| kylin.query.spark-job-trace-cache-max                        | Only for the job tracking log of spark. The maximum number of job tracking log caches in spark. The elimination strategy is LRU，TTL is kylin.query.spark-job-trace-timeout-ms + 20000 ms. |
| kylin.query.spark-job-trace-parallel-max                     | Only for the job tracking log of spark. Spark's job tracks the concurrency of log processing, "Additional information about spark" will be lost if the concurrency exceeds this limit. |
| kylin.query.timeout-seconds                                  | Query timeout, in seconds. The default value is `300` seconds. If the query execution time exceeds 300 seconds, an error will be returned: `Query timeout after: 300s`. The minimum value is `30` seconds, and the configured value less than `30` seconds also takes effect according to `30` seconds. |
| kylin.query.use-tableindex-answer-non-raw-query              | The default value is `false`, which means that the aggregate query can only be answered with the aggregate index. If the parameter is set to `true`, the system allows the corresponding table index to be used to answer the aggregate query. |
| kylin.scheduler.schedule-job-timeout-minute                  | Job execution timeout period. The default is `0` minute. This property is valid only when the it is set to be 1 or above. When the job execution exceeds the timeout period, it will change to the Error status. |
| kylin.security.user-password-encoder                         | Encryption algorithm of user password. The default is the BCrypt algorithm. If you want to use the Pbkdf2 algorithm, configure the value to <br />org.springframework.security.crypto.<br />password.Pbkdf2PasswordEncoder. <br />Note: Please do not change this configuration item arbitrarily, otherwise the user may not be able to log in |
| kylin.server.cors.allow-all                                  | allow all corss origin requests(CORS). `true` for allowing any CORS request, `false` for refusing all CORS requests. Default to `false`. |
| kylin.server.cors.allowed-origin                             | Specify a whitelist that allows cross-domain, default all domain names (*), use commas (,) to separate multiple domain names. This parameter is valid when `kylin.server.cors.allow-all`=true |
| kylin.server.mode                                            | There are three modes in Kylin, `all` , `query` and `job`, and you can change it by modifying the property. The default value is `all`. For `query` mode, it can only serves queries. For`job` mode, it can run building jobs and execute metadata operations and cannot serve queries. `all` mode can handle both of them. |
| kylin.source.hive.databases                                  | Configure the database list loaded by the data source. There is no default value. Both the system level and the project level can be configured. The priority of the project level is greater than the system level. |
| kylin.storage.columnar.spark-conf.spark.sql.view-truncate-enabled | Allow spark view to lose precision when loading tables and queries, the default value is false |
| kylin.storage.columnar.spark-conf.spark.yarn.queue           | This property specifies the yarn queue which is used by spark query cluster. |
| kylin.storage.columnar.spark-conf.spark.master               | Spark deployment is normally divided into **Spark on YARN**, **Spark on Mesos**, and **standalone**. We usually use Spark on YARN as default. This property enables Kylin to use standalone deployment, which could submit jobs to the specific spark-master-url. |
| kylin.storage.columnar.spark-conf.spark.driver.host          | Configure the IP of the node where the Kylin is located |
| kylin.storage.columnar.spark-conf.spark.sql.cartesianPartitionNumThreshold | Threshold for Cartesian Partition number in Spark Execution Plan. Query will be terminated if Cartesian Partition number reaches or exceeds the threshold. If this value is set to empty or negative, the threshold will be set to spark.executor.cores * spark.executor.instances * 100. |
| kylin.storage.quota-in-giga-bytes                            | This property specifies the storage quota for each project. The default is `10240`, in gigabytes. |
| kylin.web.timezone                                           | Time zone used for Kylin Rest service is specified by this property. The default value is the time zone of the local machine's system. You can change it according to the requirement of your application. For more details, please refer to https://en.wikipedia.org/wiki/List_of_tz_database_time_zones with the `TZ database name` column. |
| kylin.web.export-allow-admin                                 | Whether to allow Admin user to export query results to a CSV file, the default is true. |
| kylin.web.export-allow-other                                 | Whether to allow non-Admin user to export query results to a CSV file, the default is true. |
| kylin.web.stack-trace.enabled                                | The error prompts whether the popup window displays details. The default value is false. Introduced in: 4.1.1 |
| kylin.web.session.secure-random-create-enabled               | The default is false. Use UUID to generate sessionId, and use JDK's SecureRandom random number to enable sessionId after MD5 encryption, please use the upgrade session table tool to upgrade the session table first otherwise the user will report an error when logging in. |
| kylin.web.session.jdbc-encode-enabled                        | The default is false, sessionId is saved directly into the database without encryption, and sessionId will be encrypted and saved to the database after opening. Note: If the encryption function is configured, Please use the upgrade session table tool to upgrade the session table first, otherwise the user will report an error when logging in. |
| server.port                                                  | This parameter specifies the port used by the Kylin service. The default is `7070`. |
| server.address                                               | This parameter specifies the address used by the Kylin service. The default is `0.0.0.0`. |


### Modifying Kylin Properties

To override default configurations, create a `kylin.properties.override` file in the `$KYLIN_HOME/conf` directory. This file allows you to customize settings at runtime.

**Best Practice:** During system upgrades, retain the `kylin.properties.override` file with the new `kylin.properties` version to ensure a seamless transition.

**Important Note:** Parameters defined in `kylin.properties` or `kylin.properties.override` are global and loaded by default when Kylin starts. After modifying these files, restart Kylin for the changes to take effect.


### Modifying JVM Settings

In the `$KYLIN_HOME/conf/setenv.sh.template` file, you'll find a sample setting for the `KYLIN_JVM_SETTINGS` environment variable. This default setting uses relatively little memory, but you can adjust it according to your own environment. The default JVM configuration is as follows:

```properties
export KYLIN_JVM_SETTINGS="-server -Xms1g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=16m -XX:+PrintFlagsFinal -XX:+PrintReferenceGC -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintAdaptiveSizePolicy -XX:+UnlockDiagnosticVMOptions -XX:+G1SummarizeConcMark  -Xloggc:$KYLIN_HOME/logs/kylin.gc.$$  -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=64M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${KYLIN_HOME}/logs"
```

If you want to modify this configuration, you need to create a copy of the template file, name it `setenv.sh`, and place it in the `$KYLIN_HOME/conf/` directory. You can then modify the configuration in the new file as needed. 

The parameter `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${KYLIN_HOME}/logs` generates logs when an `OutOfMemory` error occurs. The default log file path is `${KYLIN_HOME}/logs`, but you can modify it if required.
```bash
export JAVA_VM_XMS=1g        #The initial memory of the JVM when kylin starts.
export JAVA_VM_XMX=8g        #The maximum memory of the JVM when kylin starts.
export JAVA_VM_TOOL_XMS=1g   #The initial memory of the JVM when the tool class is started.
export JAVA_VM_TOOL_XMX=8g   #The maximum memory of the JVM when the tool class is started.
```

The `JAVA_VM_TOOL_XMS` and `JAVA_VM_TOOL_XMX` environment variables control the initial and maximum memory of the JVM when running tool classes. If these variables are not set, they will default to the values of `JAVA_VM_XMS` and `JAVA_VM_XMX`, respectively.

Please note that some special tool classes, such as `guardian.sh`, `check-2100-hive-acl.sh`, and `get-properties.sh`, are not affected by these configurations. To take advantage of these new configurations, you need to manually set the `JAVA_VM_TOOL_XMS` and `JAVA_VM_TOOL_XMX` variables when upgrading from an older version.


### Modifying Spark Configurations

Kylin relies heavily on Apache Spark for both query and job building. To customize Spark configurations, use the following prefixes:

* `kylin.storage.columnar.spark-conf` for queries
* `kylin.engine.spark-conf` for builds

These prefixes can be used to customize various Spark settings, such as memory allocation, execution parameters, and more. For a detailed explanation of Spark configuration, please refer to the official documentation: [Spark Configuration](https://spark.apache.org/docs/latest/configuration.html).

**1. Query-Related Configurations**

```shell
kylin.storage.columnar.spark-conf.executor.instances
kylin.storage.columnar.spark-conf.executor.cores
kylin.storage.columnar.spark-conf.executor.memory
kylin.storage.columnar.spark-conf.executor.memoryOverhead
kylin.storage.columnar.spark-conf.sql.shuffle.partitions
kylin.storage.columnar.spark-conf.driver.memory
kylin.storage.columnar.spark-conf.driver.memoryOverhead
kylin.storage.columnar.spark-conf.driver.cores
```

**2. Job Building-Related Configurations**

```shell
kylin.engine.spark-conf.spark.executor.instances
kylin.engine.spark-conf.spark.executor.cores
kylin.engine.spark-conf.spark.executor.memory
kylin.engine.spark-conf.spark.executor.memoryOverhead
kylin.engine.spark-conf.spark.sql.shuffle.partitions
kylin.engine.spark-conf.spark.driver.memory
kylin.engine.spark-conf.spark.driver.memoryOverhead
kylin.engine.spark-conf.spark.driver.cores
```

**3. Switching to Minimal Configurations**

Under **$KYLIN_HOME/conf/**, there are two sets of configurations ready for use: `production` and `minimal`. The former is the default configuration, recommended for production environments. The latter uses minimal resources, suitable for sandbox or single-node environments with limited resources. To switch to `minimal` configurations, please uncomment the following configuration items in `$KYLIN_HOME/conf/kylin.properties` and restart Kylin to take effect:
```shell
# Kylin provides two configuration profiles: minimal and production(by default).
# To switch to minimal: uncomment the properties
#kylin.storage.columnar.spark-conf.spark.driver.memory=512m
#kylin.storage.columnar.spark-conf.spark.executor.memory=512m
#kylin.storage.columnar.spark-conf.spark.executor.memoryOverhead=512m
#kylin.storage.columnar.spark-conf.spark.executor.extraJavaOptions=-Dhdp.version=current -Dlog4j.configurationFile=spark-executor-log4j.xml -Dlog4j.debug -Dkylin.hdfs.working.dir=${kylin.env.hdfs-working-dir} -Dkap.metadata.identifier=${kylin.metadata.url.identifier} -Dkap.spark.category=sparder -Dkap.spark.project=${job.project} -XX:MaxDirectMemorySize=512M
#kylin.storage.columnar.spark-conf.spark.yarn.am.memory=512m
#kylin.storage.columnar.spark-conf.spark.executor.cores=1
#kylin.storage.columnar.spark-conf.spark.executor.instances=1
#kylin.storage.columnar.spark-conf.spark.sql.adaptive.coalescePartitions.minPartitionNum=1
```

### Sparder Canary Overview

Sparder Canary is a health monitoring component for the Sparder application, which is the Spark engine used for querying in Kylin. It periodically checks the status of the running Sparder instance and automatically initiates the creation of a new instance if any abnormalities are detected, such as unexpected termination or loss of responsiveness. This ensures that the Sparder application remains in a healthy and functional state at all times.

| Properties                                                      | Description                                                         |
| ----------------------------------------------------------- | ------------------------------------------------------------ |
| kylin.canary.sqlcontext-enabled                             | Whether to enable the Sparder Canary function, the default is `false`                   |
| kylin.canary.sqlcontext-threshold-to-restart-spark          | When the number of abnormal detection times exceeds this threshold, restart spark context                   |
| kylin.canary.sqlcontext-period-min                          | Check interval, default is `3` minutes                                 |
| kylin.canary.sqlcontext-error-response-ms                   | Single detection timeout time, the default is `3` minutes, if single detection timeout means no response from spark context                                   |
| kylin.canary.sqlcontext-type                                | The detection method, the default is `file`, this method confirms whether the spark context is still running normally by writing a parquet file to the directory configured by `kylin.env.hdfs-working-dir` . It can also be configured as `count` to confirm whether the spark context is running normally by performing an accumulation operation|                                 |
