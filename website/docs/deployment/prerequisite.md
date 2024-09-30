---
title: Prerequisite
language: en
sidebar_label: Prerequisite
pagination_label: Prerequisite
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords: 
  - prerequisite
draft: false
last_update:
    date: 09/13/2022
---

To ensure system performance and stability, we recommend you run Kylin on a dedicated Hadoop cluster.

Prior to installing Kylin, please check the following prerequisites are met.

- Environment
    - [Supported Hadoop Distributions](#hadoop)
    - [Java Environment](#java)
    - [Account Authority](#account)
    - [Metastore Configuration](#metadata)
    - [Check Zookeeper](#zookeeper)
    - [Network Port Requirements](#ports)
- Recommended Resource and Configuration
    - [Hadoop Cluster Resource Allocation](#resource)
    - [Recommended Hardware Configuration](#hardware)
    - [Recommended Linux Distribution](#linux)
    - [Recommended Client Configuration](#client)



### <span id="hadoop">Supported Hadoop Distributions</span>

The following Hadoop distributions are verified to run on Kylin.

- Apache Hadoop 3.2.1


Kylin requires some components, please make sure each server has the following components.

- Hive
- HDFS
- Yarn
- ZooKeeper

#### Prepare Environment

First, **make sure you allocate sufficient resources for the environment**. Please refer to [Prerequisites](docs/deployment/prerequisite.md) for detailed resource requirements for Kylin. Moreover, please ensure that `HDFS`, `YARN`, `Hive`, `ZooKeeper` and other components are in normal state without any warning information.

#### Additional configuration required for Apache Hadoop version

Add the following two configurations in `$KYLIN_HOME/conf/kylin.properties`:

- `kylin.env.apache-hadoop-conf-dir` Hadoop conf directory in Hadoop environment
- `kylin.env.apache-hive-conf-dir` Hive conf directory in Hadoop environment

#### Jar package required by Apache Hadoop version

In Apache Hadoop 3.2.1, you also need to prepare the MySQL JDBC driver in the operating environment of Kylin.

Download MySQL 8.0 JDBC driver：https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.30/mysql-connector-java-8.0.30.jar.
Please place the JDBC driver in the `$KYLIN_HOME/lib/ext` directory.

### <span id="java">Java Environment</span>

Kylin requires:

- Requires your environment's default JDK version is 8 （JDK 1.8_162 or above small version）

```shell
java -version
```

You can use the following command to check the JDK version of your existing environment, for example, the following figure shows JDK 8

![JDK version](images/jdk.png)

### <span id="account">Account Authority</span>

The Linux account running Kylin must have the required access permissions to the cluster. These permissions include:

* Read/Write permission of HDFS
* Create/Read/Write permission of Hive table

Verify the user has access to the Hadoop cluster with account `KyAdmin`. Test using the steps below:

1. Verify the user has HDFS read and write permissions

   Assuming the HDFS storage path for model data is `/kylin`, set it in `conf/kylin.properties` as:

   ```properties
   kylin.env.hdfs-working-dir=/kylin
   ```

   The storage folder must be created and granted with permissions. You may have to switch to HDFS administrator (usually the `hdfs` user),  to do this:

   ```shell
   su hdfs
   hdfs dfs -mkdir /kylin
   hdfs dfs -chown KyAdmin /kylin
   hdfs dfs -mkdir /user/KyAdmin 
   hdfs dfs -chown KyAdmin /user/KyAdmin
   ```
   Verify the `KyAdmin` user has read and write permissions

   ```shell
   hdfs dfs -put <any_file> /kylin
   hdfs dfs -put <any_file> /user/KyAdmin   
   ```

2. Verify the `KyAdmin` user has Hive read and write permissions

   Let's say you want to store a Hive table `t1` in Hive database `kylinDB`, The `t1` table contains two fields `id, name`.

   Then verify the Hive permissions:

   ```shell
   #hive
   hive> show databases;
   hive> use kylinDB;
   hive> show tables;
   hive> insert into t1 values(1, "kylin");
   hive> select * from t1;
   ```

### <span id="metadata">Prepare Metadata DB</span>

A configured metastore is required for this product.

We recommend using PostgreSQL 10.7 as the metastore, which is provided in our package. Please refer to [Use PostgreSQL as Metastore (Default)](./rdbms_metastore/usepg_as_metadb.md) for installation steps and details.

If you want to use your own PostgreSQL database, the supported versions are below:

- PostgreSQL 9.1 or above

You can also choose to use MySQL but we currently don't provide a MySQL installation package or JDBC driver. Therefore, you need to finish all the prerequisites before setting up. Please refer to [Use MySQL as Metastore](./rdbms_metastore/use_mysql_as_metadb.md) for installation steps and details. The supported MySQL database versions are below:

- MySQL 5.1-5.7
- MySQL 5.7 (recommended)

### <span id="zookeeper">Prepare Zookeeper</span>

The following steps can be used to quickly verify the connectivity between ZooKeeper and Kylin after Kerberos is enabled.
1. Find the ZooKeeper working directory on the node where the ZooKeeper Client is deployed
2. Add or modify the Client section to the `conf/jaas.conf` file:

   ```shell
   Client {
     com.sun.security.auth.module.Krb5LoginModule required
     useKeyTab=true
     keyTab="/path/to/keytab_assigned_to_kylin"
     storeKey=true
     useTicketCache=false
     principal="principal_assigned_to_kylin";
   };
   ```
3. `export JVMFLAGS="-Djava.security.auth.login.config=/path/to/jaas.conf"`
4. `bin/zkCli.sh -server ${kylin.env.zookeeper-connect-string}`
5. Verify that the ZooKeeper node can be viewed normally, for example: `ls /`
6. Clean up the new Client section in step 2 and the environment variables `unset JVMFLAGS` declared in step 3

If you download ZooKeeper from the non-official website, you can consult the operation and maintenance personnel before performing the above operations.

### <span id="ports">Network Port Requirements</span>
Kylin needs to communicate with different components. The following are the ports that need to be opened to Kylin. This table only includes the default configuration of the Hadoop environment, and does not include the configuration differences between Hadoop platforms.

| Component            | Port          | Function                                                     | Required |
| -------------------- | ------------- | ------------------------------------------------------------ | -------- |
| SSH                  | 22            | SSH to connect to the port of the virtual machine where Kylin is located | Y        |
| Kylin                | 7070          | Kylin access port                                            | Y        |
| Kylin                | 7443          | Kylin HTTPS access port                                      | N        |
| HDFS                 | 8020          | HDFS receives client connection RPC port                     | Y        |
| HDFS                 | 50010         | Access HDFS DataNode, data transmission port                 | Y        |
| Hive                 | 10000         | HiveServer2 access port                                      | N        |
| Hive                 | 9083          | Hive Metastore access port                                   | Y        |
| Zookeeper            | 2181          | Zookeeper access port                                        | Y        |
| Yarn                 | 8088          | Yarn Web UI access port                                      | Y        |
| Yarn                 | 8090          | Yarn Web UI HTTPS access port                                | N        |
| Yarn                 | 8050 / 8032   | Yarn ResourceManager communication port                      | Y        |
| Spark                | 4041          | Kylin query engine Web UI default port        | Y        |
| Spark                | 18080         | Spark History Server port                                    | N        |
| Spark                | (1024, 65535] | The ports occupied by Spark Driver and Executor are random   | Y        |
| Influxdb             | 8086          | Influxdb HTTP port                                           | N        |
| Influxdb             | 8088          | Influxdb RPC port                                            | N        |
| PostgreSQL           | 5432          | PostgreSQL access port                                       | Y        |
| MySQL                | 3306          | MySQL access port                                            | Y        |


### <span id="resource">Hadoop Cluster Resource Allocation</span>

To ensure Kylin works efficiently, please ensure the Hadoop cluster configurations satisfy the following conditions:

* `yarn.nodemanager.resource.memory-mb` larger than 8192 MB
* `yarn.scheduler.maximum-allocation-mb` larger than 4096 MB
* `yarn.scheduler.maximum-allocation-vcores` larger than 5

If you need to run Kylin in a sandbox or other virtual machine environment, please make sure the virtual machine environment has the following resources:

- No less than 4 processors

- Memory is no less than 10 GB

- The value of the configuration item `yarn.nodemanager.resource.cpu-vcores` is no less than 8

### <span id="hardware">Recommended Hardware Configuration</span>

We recommend the following hardware configuration to install Kylin:

- 16 vCore, 64 GB memory
- At least 500GB disk
- For network port requirements, please refer to the [Network Port Requirements](#ports) chapter.

### <span id="linux">Recommended Linux Distribution</span>

We recommend using the following version of the Linux operating system:

- Ubuntu 18.04 and above (recommend LTS version)
- Red Hat Enterprise Linux 6.4+ and above
- CentOS 6.4+ and above

### <span id="client">Recommended Client Configuration</span>

- Operating System: macOS / Windows 7 and above
- RAM: 8G or above
- Browser version:
    + Chrome 45 or above
    + Internet Explorer 11 or above
