#!/usr/bin/env bash
/opt/modules/spark-1.6.1/bin/spark-submit --class spark_sql.SQLWindowFunction \
--files /opt/modules/spark-1.6.1/conf/hive-site.xml \
--driver-class-path /opt/modules/spark-1.6.1/lib/mysql-connector-java-5.1.24-bin.jar \
--master spark://hadoop:7077 /home/hadoop/Documents/export/mySpark/mySpark.jar