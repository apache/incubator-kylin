---
title: Single Node Mode
language: en
sidebar_label: Single Node Mode
pagination_label: Single Node Mode
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - deployment mode
draft: false
last_update:
    date: 08/12/2022
---

### 1. Prepare installation environment
Create a KYLIN_HOME path and a Linux account for Kylin.

:::info Example
- The installation location is `/usr/local/`
- Linux account to run Kylin is `KyAdmin`.
:::

follow the [Prerequisite](prerequisite.md) and finish the environment preparation.

### 2. Download Kylin binary package.

   Please download official release binary from [Download Page](../download.md) . 

   If you want to package from source code, please refer to [How To Package](../development/how_to_package.md).

   Copy and unzip Kylin package to your target server.

:::info Example
   cd /usr/local
   tar -zxvf apache-kylin-[Version].tar.gz
:::

   The decompressed directory should be exported as `$KYLIN_HOME` on your server.
:::info Example
   export KYLIN_HOME=/usr/local/apache-kylin-[Version]/
:::

### 4. Download Kylin built-in Spark

:::info Example
   bash $KYLIN_HOME/sbin/download-spark-user.sh
:::

   There will be a `spark` directory under `$KYLIN_HOME` .

### 5. Configure Metadata DB.

   You can use either MySQL or PostgresQL as a metadata DB. 

    * [Use PostgreSQL as Metastore](../deployment/rdbms_metastore/usepg_as_metadb.md).
    * [Use MySQL as Metastore](../deployment/rdbms_metastore/use_mysql_as_metadb.md).

   For production environments, we recommend setting up a dedicated metadata DB to ensure reliability.

### 6. Create a working directory on HDFS and grant permissions.

   The default working directory is `/kylin`. Also ensure the Linux account has access to its home directory on HDFS. Meanwhile, create directory `/kylin/spark-history` to store the spark log files.

   ```sh
   hadoop fs -mkdir -p /kylin
   hadoop fs -chown root /kylin
   hadoop fs -mkdir -p /kylin/spark-history
   hadoop fs -chown root /kylin/spark-history
   ```

   You can modify working directory in `$KYLIN_HOME/conf/kylin.properties`.
   :::info Example
   kylin.env.hdfs-working-dir=`hdfs://${nameservice}/kylin`
   :::

:::note Note
If you do not have the permission to create `/kylin/spark-history`, you can configure `kylin.engine.spark-conf.spark.eventLog.dir` and `kylin.engine.spark-conf.spark.history.fs.logDirectory` with an available directory.
:::

### <span id="configuration">Quick Configuration</span>

In the `conf` directory under the root directory of the installation package, you should configure the parameters in the file `kylin.properties` as follows:

1. According to the PostgreSQL configuration, configure the following metadata parameters. Pay attention to replace the corresponding ` {metadata_name} `, `{host} `, ` {port} `, ` {user} `, ` {password} ` value, the maximum length of `metadata_name` allowed is 28.

   :::info Example
   kylin.metadata.url=`{metadata_name}@jdbc,driverClassName=org.postgresql.Driver,url=jdbc:postgresql://{host}:{port}/kylin,username={user},password={password}`
   :::

   For more PostgreSQL configuration, please refer to [Use PostgreSQL as Metastore](../deployment/rdbms_metastore/usepg_as_metadb.md). For information for MySQL configuration, please refer to [Use MySQL as Metastore](../deployment/rdbms_metastore/use_mysql_as_metadb.md).

   :::note Note
   please name the `{metadata_name}` with characters, numbers, or underscores. 
   The name should start with characters.
   :::

2. When executing jobs, Kylin will submit the build task to Yarn. You can set and replace `{queue}` in the following parameters as the queue you actually use, and require the build task to be submitted to the specified queue.

   :::info Example
   kylin.engine.spark-conf.spark.yarn.queue=`{queue_name}`
   :::


3. Configure ZooKeeper.

   Kylin uses ZooKeeper for service discovery, pleaser refer to [Service Discovery](cluster_mode.md#sd) for more details.

   Configure property in `${KYLIN_HOME}\conf\kylin.properties(.override)`. 

   :::info Example
   kylin.env.zookeeper-connect-string=10.1.2.1:2181,10.1.2.2:2181,10.1.2.3:2181
   :::

   If you use ACL for Zookeeper, need setting the follow configuration

   | Properties                                                  | Description                                                                                                          |
      | ------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
   | kylin.env.zookeeper-acl-enabled                             | Whether to enable Zookeeper ACL. The default value is disabled.                                                      |
   | kylin.env.zookeeper.zk-auth                                 | The user password and authentication method used by Zookeeper. The default value is empty.                           |
   | kylin.env.zookeeper.zk-acl                                  | ACL permission setting. The default value is `world:anyone:rwcda`. By default, all users can perform all operations. |

   If you need to encrypt kylin.env.zookeeper.zk-auth , you can do it like this：

   1. run following commands in `${KYLIN_HOME}`, it will print encrypted value
   :::info Example
    ./bin/kylin.sh org.apache.kylin.tool.general.CryptTool -e AES -s `<value>`
   :::
   2. Add the property in `${KYLIN_HOME}\conf\kylin.properties(.override)`
    ```
    kylin.env.zookeeper.zk-auth=ENC('${encrypted_value}')
    ```
4. Configure Gluten
   Apache Gluten is required by internal table feature, it's enabled by default. Add the following config to your `${KYLIN_HOME}\conf\kylin.properties(.override)`
   :::info Example
   **gluten for query**<br></br>
   kylin.storage.columnar.spark-conf.spark.gluten.sql.columnar.backend.ch.runtime_config.storage_configuration.disks.hdfs.endpoint=hdfs://olivernameservice/<br></br>
   **gluten for build**<br></br>
   kylin.engine.spark-conf.spark.gluten.sql.columnar.backend.ch.runtime_config.storage_configuration.disks.hdfs.endpoint=hdfs://olivernameservice/
   :::

5. Configure Query & Build Cluster on Spark Standalone

   ```properties
   # Query on Stand Alone
   kylin.storage.columnar.spark-conf.spark.master=spark://${SPARK_MASTER_HOST}:7077
   kylin.storage.columnar.spark-conf.spark.gluten.sql.columnar.backend.ch.runtime_config.hdfs.libhdfs3_conf={path for hdfs-site.xml}
   kylin.storage.columnar.spark-conf.spark.gluten.sql.columnar.executor.libpath={path for libch.so}
   kylin.storage.columnar.spark-conf.spark.executorEnv.LD_PRELOAD={path for libch.so}
   kylin.storage.columnar.spark-conf.spark.gluten.sql.columnar.backend.ch.runtime_config.reuse_disk_cache=false （worker 不能保证只有一个 app）
   kylin.storage.columnar.spark-conf.spark.gluten.sql.executor.jar.path={path for gluten.jar}
   # Build on Stand Alone
   kylin.engine.spark-conf.spark.master=spark://${SPARK_MASTER_HOST}:7077
   kylin.engine.spark-conf.spark.gluten.sql.columnar.backend.ch.runtime_config.hdfs.libhdfs3_conf={path for hdfs-site.xml}
   kylin.engine.spark-conf.spark.gluten.sql.columnar.executor.libpath={path for libch.so}
   kylin.engine.spark-conf.spark.executorEnv.LD_PRELOAD={path for libch.so}
   kylin.engine.spark-conf.spark.gluten.sql.columnar.backend.ch.runtime_config.reuse_disk_cache=false (worker 不能保证只有一个 app)
   kylin.engine.spark-conf.spark.gluten.sql.driver.jar.path={path for gluten.jar}
   kylin.engine.spark-conf.spark.gluten.sql.executor.jar.path={path for gluten.jar}
   ```

6. (optional) Configure Spark Client node information
   Since Spark is started in yarn-client mode, if the IP information of Kylin is not configured in the hosts file of the Hadoop cluster, please add the following configurations in `kylin.properties`:
   `kylin.storage.columnar.spark-conf.spark.driver.host={hostIp}`
   `kylin.engine.spark-conf.spark.driver.host={hostIp}`

You can modify the `{hostIp}` according to the following example:
  ```properties
  kylin.storage.columnar.spark-conf.spark.driver.host=10.1.3.71
  kylin.engine.spark-conf.spark.driver.host=10.1.3.71
  ```
