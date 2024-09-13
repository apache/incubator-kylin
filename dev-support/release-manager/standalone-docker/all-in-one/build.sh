#!/bin/bash

TAG=5.0.0-GA

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ${DIR} || exit
echo "build image in dir "${DIR}

echo "package kylin in local for building image"
if [[ ! -d ${DIR}/package/ ]]; then
    mkdir -p ${DIR}/package/
fi

# The official apache kylin package has no Spark binary included, prepare manually with following steps:
# 1. Download apache kylin binary and extract
# 2. Execute sbin/download-spark-user.sh and should have a new spark folder at the root of kylin dir
# 3. Re-compress kylin folder and put it to the package dir for Dockerfile use
#
# wget https://archive.apache.org/dist/kylin/apache-kylin-5.0.0-GA/apache-kylin-5.0.0-GA-bin.tar.gz -P ${DIR}/package/
# tar zxf apache-kylin-5.0.0-GA-bin.tar.gz
# cd apache-kylin-5.0.0-GA-bin
# bash sbin/download-spark-user.sh
# tar -czf apache-kylin-5.0.0-GA-bin.tar.gz apache-kylin-5.0.0-GA-bin
# Notice - For mac tar command use: tar czf apache-kylin-5.0.0-GA-bin.tar.gz --no-mac-metadata apache-kylin-5.0.0-GA-bin
# to avoid AppleDouble format hidden files inside the compressed file

echo "start to build kylin standalone docker image"
docker build . -t apachekylin/apache-kylin-standalone:${TAG}

BUILD_RESULT=$?
if [ "$BUILD_RESULT" != "0" ]; then
  echo "Image build failed, please check"
  exit 1
fi
echo "Image build succeed"
