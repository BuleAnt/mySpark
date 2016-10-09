#!/usr/bin/env bash

export SPARK_HOME=/opt/modules/spark-1.6.1
export EXPORT_PATH=/home/hadoop/Documents/export/mySpark

${SPARK_HOME}/bin/spark-submit --class spark_streaming.JWordCountOnline \
--master spark://hadoop:7077 ${EXPORT_PATH}/mySpark.jar