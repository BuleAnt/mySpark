#!/bin/bash
export SPARK_HOME=/opt/modules/spark-1.6.1
export EXPORT_PATH=/home/hadoop/Documents/export/mySpark

${SPARK_HOME}/bin/spark-submit --class rdd.WordCount_Cluster \
--master spark://hadoop:7010 ${EXPORT_PATH}/mySpark.jar

