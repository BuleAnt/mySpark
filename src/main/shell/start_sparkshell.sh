#!/usr/bin/env bash
#spark-shell 添加依赖包
/opt/modules/spark-1.6.1/bin/spark-shell local \
--jars /home/hadoop/Documents/export/mySpark/mySpark.jar
#spark-shell使用yarn模式，并使用队列
# /opt/modules/spark-1.6.1/bin/spark-shell --master yarn-client --queue wz111