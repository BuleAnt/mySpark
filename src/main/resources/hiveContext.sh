#!/usr/bin/env bash
spark-submit --class spark_sql.SQL2Hive \
--files /opt/modules/spark-1.6.1/conf/hive-site.xml \
--driver-class-path /opt/modules/spark-1.6.1/lib/mysql-connector-java-5.1.24-bin.jar \
--master spark://Master:7077 \
/home/hadoop/Documents/export/mySpark.jar