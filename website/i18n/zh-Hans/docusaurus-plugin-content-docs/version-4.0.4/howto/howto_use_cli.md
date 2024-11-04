---
layout: docs
title:  Use Utility CLIs
categories: howto
permalink: /docs/howto/howto_use_cli.html
---
Kylin has some client utility tools. This document will introduce the following class: KylinConfigCLI.java, CubeMetaExtractor.java, CubeMetaIngester.java, CubeMigrationCLI.java and CubeMigrationCheckCLI.java. Before using these tools, you have to switch to the KYLIN_HOME directory. 

## KylinConfigCLI.java

### Function
KylinConfigCLI.java outputs the value of Kylin properties. 

### How to use 
After the class name, you can only write one parameter, `conf_name` which is the parameter name that you want to know its value.
```
./bin/kylin.sh org.apache.kylin.tool.KylinConfigCLI <conf_name>
```
For example: 
```
./bin/kylin.sh org.apache.kylin.tool.KylinConfigCLI kylin.server.mode
```
Result:
```
all
```

If you do not know the full parameter name, you can use the following command, then all parameters prefixed by this prefix will be listed:
```
./bin/kylin.sh org.apache.kylin.tool.KylinConfigCLI <prefix>.
```
For example: 
```
./bin/kylin.sh org.apache.kylin.tool.KylinConfigCLI kylin.job.
```
Result:
```
max-concurrent-jobs=10
retry=3
sampling-percentage=100
```

## CubeMetaExtractor.java

### Function
CubeMetaExtractor.java is to extract Cube related info for debugging / distributing purpose.  

### How to use
At least two parameters should be followed. 
```
./bin/kylin.sh org.apache.kylin.tool.CubeMetaExtractor -<conf_name> <conf_value> -destDir <your_dest_dir>
```
For example: 
```
./bin/kylin.sh org.apache.kylin.tool.CubeMetaExtractor -cube kylin_sales_cube -destDir /tmp/kylin_sales_cube
```
Result:
After the command is executed, the cube, project or hybrid you want to extract will be dumped in the specified path.

All supported parameters are listed below:  

| Parameter             | Description                                                                                         |
|-----------------------| :-------------------------------------------------------------------------------------------------- |
| allProjects           | Specify realizations in all projects to extract                                                     |
| compress              | Specify whether to compress the output with zip. Default true.                                      | 
| cube                  | Specify which Cube to extract                                                                       |
| destDir               | (Required) Specify the dest dir to save the related information                                     |
| hybrid                | Specify which hybrid to extract                                                                     |
| includeJobs           | Set this to true if want to extract job info/outputs too. Default false                             |
| includeSegmentDetails | Set this to true if want to extract segment details too, such as dict, tablesnapshot. Default false |
| includeSegments       | Set this to true if want extract the segments info. Default true                                    |
| onlyOutput            | When include jobs, only extract output of job. Default true                                         |
| packagetype           | Specify the package type                                                                            |
| project               | Which project to extract                                                    |


## CubeMetaIngester.java

### Function
CubeMetaIngester.java is to ingest the extracted cube meta data into another metadata store. It only supports ingest cube now. 

### How to use
At least two parameters should be specified. Please make sure the cube you want to ingest does not exist in the target project. 

Note: The zip file must contain only one directory after it has been decompressed.

```
./bin/kylin.sh org.apache.kylin.tool.CubeMetaIngester -project <target_project> -srcPath <your_src_dir>
```
For example: 
```
./bin/kylin.sh org.apache.kylin.tool.CubeMetaIngester -project querytest -srcPath /tmp/newconfigdir1/cubes.zip
```
Result:
After the command is successfully executed, the cube you want to ingest will exist in the srcPath.

All supported parameters are listed below:

| Parameter          | Description                                                                                                                                                                                        |
|--------------------| :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| forceIngest        | Skip the target Cube, model and table check and ingest by force. Use in caution because it might break existing cubes! Suggest to backup metadata store first. Default false.                      |
| overwriteTables    | If table meta conflicts, overwrite the one in metadata store with the one in srcPath. Use in caution because it might break existing cubes! Suggest to backup metadata store first. Default false. |
| project            | (Required) Specify the target project for the new cubes.                                                                                                                                           |
| srcPath            | (Required) Specify the path to the extracted Cube metadata zip file.                                                                                                                               |

## CubeMigrationCLI.java

## Function
Apache Kylin have provided migration tool to support migrating metadata across different clusters since version 2.0. Recently, we have refined and added new ability to CubeMigration tool, The list of enhanced functions is showed as below:
- Support migrating all cubes in source cluster
- Support migrating a whole project in source cluster
- Support migrating and upgrading metadata from older version to Kylin 4

### How to use
Please check: [How to migrate metadata to Kylin4](https://cwiki.apache.org/confluence/display/KYLIN/How+to+migrate+metadata+to+Kylin+4)