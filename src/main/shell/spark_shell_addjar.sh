#!/usr/bin/env bash

export SPARK_HOME=/opt/modules/spark-1.6.1
export EXPORT_PATH=/home/hadoop/Documents/export/mySpark

#spark-shell 添加依赖包
${SPARK_HOME}/bin/spark-shell local \
--jars ${EXPORT_PATH}/mySpark.jar
#spark-shell使用yarn模式，并使用队列
# /opt/modules/spark-1.6.1/bin/spark-shell --master yarn-client --queue wz111