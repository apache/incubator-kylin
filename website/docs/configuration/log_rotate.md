---
title: Log Rotate
language: en
sidebar_label: Log Rotate
pagination_label: Log Rotate
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - log rotate
draft: false
last_update:
    date: 08/16/2022
---

### Log Rotation Configuration

Kylin's log rotation is configured to manage the three log files: `shell.stderr`, `shell.stdout`, and `kylin.out` located in the `$KYLIN_HOME/logs/` directory.
**Any changes to the configurations below require a restart to take effect.**

| Property | Description | Default Value | Options |
| --- | --- | --- | --- |
| `kylin.env.max-keep-log-file-number` | Maximum number of log files to keep | 10 |  |
| `kylin.env.max-keep-log-file-threshold-mb` | Log file rotation threshold (in MB) | 256 |  |
| `kylin.env.log-rotate-check-cron` | Crontab time configuration for log rotation | `33 * * * *` |  |
| `kylin.env.log-rotate-enabled` | Enable log rotation using crontab | `true` | `false` |

### Default Log Rotation Strategy

To use the default log rotation strategy:

1. Set `kylin.env.log-rotate-enabled` to `true` (default).
2. Ensure users running Kylin can use the `logrotate` and `crontab` commands to add a scheduled task.

Kylin will:

* Add or update crontab tasks according to `kylin.env.log-rotate-check-cron` on startup or restart.
* Remove added crontab tasks on exit.

### Known Limitations

* If the default log rotation policy conditions are not met, Kylin will only perform log rolling checks at startup. This means that log files will be rotated based on the `kylin.env.max-keep-log-file-number` and `kylin.env.max-keep-log-file-threshold-mb` parameters every time the `kylin.sh start` command is executed. Note that prolonged Kylin runtime may result in excessively large log files.
* Using `crontab` to control log rotation may result in log loss during rotation if the log file is too large.
