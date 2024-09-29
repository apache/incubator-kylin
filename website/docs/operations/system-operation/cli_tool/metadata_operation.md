---
title: Metadata Operation
language: en
sidebar_label: Metadata Operation
pagination_label: Metadata Operation
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - metadata operation
draft: false
last_update:
    date: 08/16/2022
---

Kylin instances are stateless services, and all state information is stored in metadata. Therefore, backing up and restoring metadata is a crucial part of operation and maintenance.

Metadata is divided into system level and project level. 

### Metadata Backup	{#metadata_backup}

In general, it is a good practice to back up metadata before each failure recovery or system upgrade. This can guarantee the possibility of rollback after the operation fails, and still maintain the stability of the system in the worst case.

In addition, metadata backup is also a tool for fault finding. When the system fails, the frontend frequently reports errors. By downloading and viewing metadata, it is often helpful to determine whether there is a problem with the metadata or not.

Metadata can be backed up via the command line, as follows:

- Metadata backup via **command line**

  Kylin provides a command line tool for backing up metadata, using the following methods:

  - Backup **system level** metadata

     ```sh
     $KYLIN_HOME/bin/metastore.sh backup METADATA_BACKUP_PATH
     ```
    Parameter Description:

    - `METADATA_BACKUP_PATH` - optional, represents the metadata storage path of the backup, the default value is `${KYLIN_HOME}/meta_backups/`
    
  - Backup **project level** metadata

     ```sh
     $KYLIN_HOME/bin/metastore.sh backup-project PROJECT_NAME METADATA_BACKUP_PATH
     ```

     Parameter Description:

     - `PROJECT_NAME` - required, the name of the project to be backed up, such as learn_kylin
     - `METADATA_BACKUP_PATH` - optional, represents the metadata storage path of the backup, the default value is `${KYLIN_HOME}/meta_backups/`
     

### Metadata Restore    {#metadata_restore}

Metadata recovery is required in Kylin with the **command line**.

- Restore **system level** metadata

  ```sh
  $KYLIN_HOME/bin/metastore.sh restore METADATA_BACKUP_PATH [--after-truncate]
  ```
  Example:
  ```sh
  ./bin/metastore.sh restore meta_backups/2019-12-19-14-18-01_backup/
  ```
  
  Parameter Description:
  - `METADATA_BACKUP_PATH` - required, represents the metadata path that are going to be recovered, the default value is `${KYLIN_HOME}/meta_backups/`
  - `--after-truncate` - optional, if this parameter is added, the system metadata will be completely restored, otherwise only the deleted and modified metadata will be restored, and the new metadata will still be retained.

- Restore **project level** metadata 

   ```sh
   $KYLIN_HOME/bin/metastore.sh restore-project PROJECT_NAME METADATA_BACKUP_PATH [--after-truncate]
   ```
  Example:
  ```sh
  ./bin/metastore.sh restore-project projectA meta_backups/2019-12-19-14-18-01_backup/
  ```

  Parameter Description:

   - `PROJECT_NAME` - required, represents the project name
   - `METADATA_BACKUP_PATH` - required, represents the metadata path that are going to be recovered, the default value is `${KYLIN_HOME}/meta_backups/`
   - `--after-truncate` - optional, if this parameter is added, the project metadata will be completely restored, otherwise only the deleted and modified metadata will be restored, and the new metadata will still be retained.
   

## Metadata Migration {#migration}

Since Kylin 5.0-alpha and Kylin 5.0.0 underwent a metadata refactoring, you will need to use this tool to perform a metadata migration on versions prior to 5.0.0. The steps for migration are as follows:

+ Backup the metadata
  ```shell
  $KYLIN_HOME/bin/metastore.sh backup METADATA_BACKUP_PATH
  ```
+ Perform metadata conversion
  ```shell
  $KYLIN_HOME/bin/kylin.sh org.apache.kylin.common.persistence.metadata.MigrateKEMetadataTool  {inputPath} {outputPath}
  ```
+ Restore the metadata
  ```shell
  $KYLIN_HOME/bin/metastore.sh restore METADATA_RESTORE_PATH
  ```
:::tip Tips
+ Configure the new KE and ensure it starts normally. Ensure that at least the following parameters are configured: `metadata_url`, `zookeeper`, other required parameters. If the metadata were stored in MySQL, provide the MySQL-related JAR package.
+ During metadata migration and import, a large number of intermediate values may be stored in memory. If you encounter OutOfMemory (OOM) issues, adjust the memory parameters and try again.
:::
