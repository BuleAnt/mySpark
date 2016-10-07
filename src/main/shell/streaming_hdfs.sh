#!/usr/bin/env bash
/opt/modules/spark-1.6.1/bin/spark-submit --class spark_streaming.JWordCountOnline \
--master spark://hadoop:7077 /home/hadoop/Documents/export/mySpark/mySpark.jar
