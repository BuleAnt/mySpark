#!/bin/bash
/opt/modules/spark-1.6.1/bin/spark-submit --class rdd.WordCount_Cluster \
--master spark://hadoop:7010 /home/hadoop/Documents/export/mySpark/mySpark.jar

