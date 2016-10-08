#!/usr/bin/env bash
export SPARK_HOME=/opt/modules/spark-1.6.1
export EXPORT_PATH=/home/hadoop/Documents/export/mySpark


${SPARK_HOME}/bin/spark-submit --class spark_sql.SQL2Hive \
--files ${SPARK_HOME}/conf/hive-site.xml \
--driver-class-path ${SPARK_HOME}/lib/mysql-connector-java-5.1.24-bin.jar \
--master spark://hadoop:7077 ${EXPORT_PATH}/mySpark.jar