---
title: Spark RPC Encryption
language: en
sidebar_label: Spark RPC Encryption
pagination_label: Spark RPC Encryption
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - spark rpc communication encryption
draft: false
last_update:
    date: 08/16/2022
---

Kylin supports enabling communication encryption between Spark nodes, which enhances the security of internal communication and prevents specific security attacks. This feature is disabled by default. To enable it, follow the steps below:

### How to Configure

Ensure that RPC communication encryption is enabled in the Spark cluster by referring to the [Spark Security documentation](https://spark.apache.org/docs/latest/security.html#authentication).

Add the following configurations in the `$KYLIN_HOME/conf/kylin.properties` file to enable Kylin nodes and Spark cluster communication encryption:

   ```properties
   ### spark rpc encryption for build jobs
   kylin.storage.columnar.spark-conf.spark.authenticate=true
   kylin.storage.columnar.spark-conf.spark.authenticate.secret=kylin
   kylin.storage.columnar.spark-conf.spark.network.crypto.enabled=true
   kylin.storage.columnar.spark-conf.spark.network.crypto.keyLength=256
   kylin.storage.columnar.spark-conf.spark.network.crypto.keyFactoryAlgorithm=PBKDF2WithHmacSHA256

   ### spark rpc encryption for query jobs
   kylin.engine.spark-conf.spark.authenticate=true
   kylin.engine.spark-conf.spark.authenticate.secret=kylin
   kylin.engine.spark-conf.spark.network.crypto.enabled=true
   kylin.engine.spark-conf.spark.network.crypto.keyLength=256
   kylin.engine.spark-conf.spark.network.crypto.keyFactoryAlgorithm=PBKDF2WithHmacSHA256
   ```

### How to Verification

After completing the configuration, restart Kylin and verify that both query and build tasks are executed successfully.
