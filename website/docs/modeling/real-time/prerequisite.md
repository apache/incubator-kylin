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
    - realtime
    - fusion model
draft: false
last_update:
    date: 09/03/2024
---

Kylin Real-Time supports querying real-time streaming data, which achieves lower latency from data loading to query.

This article introduces hardware and software requirements for Kylin Real-Time.

### Network requirements

Connection between Kylin and Kafka clusters is enabled.

> [!NOTE]
>
> If there's no DNS server in LAN, you need to manually add the IP address and Hostname of Kafka nodes to the `/etc/hosts` file of the Kylin server. 

### Kylin and Kafka versions 

| **Product**          | **Version**                                                |
| -------------------- | ---------------------------------------------------------- |
| Kylin | 5.0 or higher version                                      |
| Kafka                | 0.11.0.X or higher version ([manual deployment required](https://kafka.apache.org/0110/documentation.html)) |

### Other requirements

- The time across Kylin and Kafka clusters are synchronized. On how to change the time for Kylin, see [Basic Configuration](../../installation/config/configuration.en.md). 

- When authentication mechanism is enabled on Hadoop or Kafka cluster, further configurations are needed: 

  %accordion%**When Kerberos is enabled on Hadoop and Kafka**%accordion%

  > [!NOTE]
  >
  > Make sure the Kerberos ticket to use is still valid during index building, or Kafka data cannot be consumed.

  1. Add the following Kafka configuration settings In the kylin.properties file.

     ```yaml
     kylin.kafka-jaas.enabled=true
     kylin.streaming.kafka-conf.security.protocol=SASL_PLAINTEXT
     kylin.streaming.kafka-conf.sasl.mechanism=GSSAPI
     kylin.streaming.kafka-conf.sasl.kerberos.service.name=kafka
     ```
     
  2. Create Kafka authentication file in path $KYLIN_HOME/conf/kafka_jaas.conf with the following configuration. This configuration has been validated with [CDH](../../installation/install_uninstall/install_on_cdh.en.md) 6.3. 
  
     ```yaml
     KafkaClient {
          com.sun.security.auth.module.Krb5LoginModule required
          useKeyTab=false
          useTicketCache=true
          serviceName="{serviceName}";
     };
     ```
     
     **{serviceName}**: service name, for example, kafka
  
  %/accordion%
  
  %accordion%**When Kerberos is enabled on Hadoop while SASL/PLAIN is enabled on Kafka** %accordion%
  
  1. Add the following Kafka configuration in the kylin.properties file.

     ```yaml
     kylin.kafka-jaas.enabled=true
     kylin.streaming.kafka-conf.security.protocol=SASL_PLAINTEXT
     kylin.streaming.kafka-conf.sasl.mechanism={mechanism}
     ```
     
     **{mechanism}**: encryption algorithm, for example, PLAIN or SCRAM-SHA-256
  
  2. Create Kafka authentication file in path $KYLIN_HOME/conf/kafka_jaas.conf with the following configuration. 
  
     ```yaml
     KafkaClient {
        {LoginModule} required
        username="{username}"
        password="{password}";
     };
     ```
  
     - **{LoginModule}**: class name of the login module, for example, `org.apache.kafka.common.security.scram.ScramLoginModule`
     - **{username}**: Kafka username
     - **{password}**: Kafka password
  
  %/accordion%