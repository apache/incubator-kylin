---
layout: docs-cn
title:  "实用 CLI 工具"
categories: howto
permalink: /cn/docs/howto/howto_use_cli.html
---
Kylin 提供一些方便实用的工具类。这篇文档会介绍以下几个工具类：KylinConfigCLI.java，CubeMetaExtractor.java，CubeMetaIngester.java，CubeMigrationCLI.java 和 CubeMigrationCheckCLI.java。在使用这些工具类前，首先要切换到 KYLIN_HOME 目录下。

## KylinConfigCLI.java

### 作用
KylinConfigCLI 工具类会将您输入的 Kylin 参数的值输出。 

### 如何使用
类名后只能写一个参数，conf_name 即您想要知道其值的参数名称。
{% highlight Groff markup %}
./bin/kylin.sh org.apache.kylin.tool.KylinConfigCLI <conf_name>
{% endhighlight %}
例如： 
{% highlight Groff markup %}
./bin/kylin.sh org.apache.kylin.tool.KylinConfigCLI kylin.server.mode
{% endhighlight %}
结果：
{% highlight Groff markup %}
all
{% endhighlight %}
如果您不知道参数的准确名称，您可以使用以下命令，然后所有以该前缀为前缀的参数的值都会被列出。
{% highlight Groff markup %}
./bin/kylin.sh org.apache.kylin.tool.KylinConfigCLI <prefix>.
{% endhighlight %}
例如：
{% highlight Groff markup %}
./bin/kylin.sh org.apache.kylin.tool.KylinConfigCLI kylin.job.
{% endhighlight %}
结果：
{% highlight Groff markup %}
max-concurrent-jobs=10
retry=3
sampling-percentage=100
{% endhighlight %}

## CubeMetaExtractor.java

### 作用
CubeMetaExtractor.java 用于提取与 cube 相关的信息以达到调试/分发的目的。  

### 如何使用
类名后至少写两个参数。
{% highlight Groff markup %}
./bin/kylin.sh org.apache.kylin.tool.extractor.CubeMetaExtractor -<conf_name> <conf_value> -destDir <your_dest_dir>
{% endhighlight %}
例如：
{% highlight Groff markup %}
./bin/kylin.sh org.apache.kylin.tool.extractor.CubeMetaExtractor -cube querycube -destDir /root/newconfigdir1
{% endhighlight %}
结果：
命令执行成功后，您想要抽取的 cube/project/hybrid 将会存在于您指定的 destDir 目录中。

下面会列出所有支持的参数：

| Parameter                                             | Description                                                                                         |
| ----------------------------------------------------- | :-------------------------------------------------------------------------------------------------- |
| allProjects                                           | Specify realizations in all projects to extract                                                     |
| compress <compress>                                   | Specify whether to compress the output with zip. Default true.                                      | 
| cube <cube>                                           | Specify which Cube to extract                                                                       |
| destDir <destDir>                                     | (Required) Specify the dest dir to save the related information                                     |
| hybrid <hybrid>                                       | Specify which hybrid to extract                                                                     |
| includeJobs <includeJobs>                             | Set this to true if want to extract job info/outputs too. Default false                             |
| includeSegmentDetails <includeSegmentDetails>         | Set this to true if want to extract segment details too, such as dict, tablesnapshot. Default false |
| includeSegments <includeSegments>                     | Set this to true if want extract the segments info. Default true                                    |
| onlyOutput <onlyOutput>                               | When include jobs, only extract output of job. Default true                                         |
| packagetype <packagetype>                             | Specify the package type                                                                            |
| project <project>                                     | Specify realizations in which project to extract                                                    |
| submodule <submodule>                                 | Specify whether this is a submodule of other CLI tool. Default false.                               |

## CubeMetaIngester.java

### 作用
CubeMetaIngester.java 将提取的 cube 注入到另一个 metadata store 中。目前其只支持注入 cube。  

### 如何使用
类名后至少写两个参数。请确保您想要注入的 cube 在要注入的 project 中不存在。注意：zip 文件解压后必须只能包含一个目录。
{% highlight Groff markup %}
./bin/kylin.sh org.apache.kylin.tool.CubeMetaIngester -project <target_project> -srcPath <your_src_dir>
{% endhighlight %}
例如：
{% highlight Groff markup %}
./bin/kylin.sh org.apache.kylin.tool.CubeMetaIngester -project querytest -srcPath /root/newconfigdir1/cubes.zip
{% endhighlight %}
结果：
命令执行成功后，您想要注入的 cube 将会存在于您指定的 srcPath 目录中。

下面会列出所有支持的参数：

| Parameter                         | Description                                                                                                                                                                                        |
| --------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| forceIngest <forceIngest>         | Skip the target cube, model and table check and ingest by force. Use in caution because it might break existing cubes! Suggest to backup metadata store first. Default false.                      |
| overwriteTables <overwriteTables> | If table meta conflicts, overwrite the one in metadata store with the one in srcPath. Use in caution because it might break existing cubes! Suggest to backup metadata store first. Default false. |
| project <project>                 | (Required) Specify the target project for the new cubes.                              
| srcPath <srcPath>                 | (Required) Specify the path to the extracted Cube metadata zip file.                                                                                                                               |

## CubeMigrationCLI.java

### 作用
自 Apache kylin 2.0 以来提供了迁移工具来支持跨不同集群迁移元数据。在 kylin4.0 中，我们对 CubeMigration 工具进行了改进并添加了新功能，增强功能列表如下所示：
-支持迁移源集群中的所有多维数据集
-支持在源集群中迁移整个项目
-支持将元数据从旧版本迁移和升级到Kylin 4

### 如何使用
请参考文档：[How to migrate metadata to Kylin4](https://cwiki.apache.org/confluence/display/KYLIN/How+to+migrate+metadata+to+Kylin+4)