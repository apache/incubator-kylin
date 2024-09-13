#!/bin/bash

log_dir="$HIVE_HOME/logs"

if [ ! -d "$log_dir" ]; then
    mkdir -p $log_dir
fi

nohup $HIVE_HOME/bin/hive --service hiveserver2 > "$log_dir/hiveserver2.out" 2>&1 &
