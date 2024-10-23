---
title: 如何测试 Kylin
language: zh-Hans
sidebar_label: 如何测试 Kylin
pagination_label: 如何测试 Kylin
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: development/how_to_debug_kylin_in_ide
pagination_next: development/how_to_package
showLastUpdateAuthor: true
showLastUpdateTime: true
keywords:
  - testing
draft: false
last_update:
  date: 10/24/2024
  author: Jiang Longfei
---

# 如何测试 Kylin

```shell
bash dev-support/unit_testing.sh
```

这个脚本将在大约1~1.5小时完成。输出将保存为 `ci-results-YYYY-mm-dd.txt`。

如果所有的测试完成, 命令行将会打印出测试结果:

```text
...
[INFO] --- maven-surefire-plugin:3.0.0-M5:test (default-test) @ kylin-sparder ---
[INFO] --- maven-surefire-plugin:3.0.0-M5:test (default-test) @ kylin-spark-common ---
[INFO] --- maven-surefire-plugin:3.0.0-M5:test (default-test) @ kylin-spark-it ---
<Failed test on following module>
<Failed cases statistics>
```
