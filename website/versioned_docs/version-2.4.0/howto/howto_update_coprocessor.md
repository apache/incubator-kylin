---
layout: docs24
title:  Update Coprocessor
categories: howto
permalink: /docs24/howto/howto_update_coprocessor.html
---

Kylin leverages HBase coprocessor to optimize query performance. After new versions released, the RPC protocol may get changed, so user need to redeploy coprocessor to HTable.

There's a CLI tool to update HBase Coprocessor:

```shell
$KYLIN_HOME/bin/kylin.sh org.apache.kylin.storage.hbase.util.DeployCoprocessorCLI default all
```
