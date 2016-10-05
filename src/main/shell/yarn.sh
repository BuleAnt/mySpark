#!/usr/bin/env bash
# 运行时使用yarn分配资源，并设置--num-executors参数
nohup /opt/modules/spark-1.6.1/bin/bin/spark-submit
--name mergePartition
--class main.scala.week2.mergePartition
--num-executors 30
--master yarn
mergePartition.jar >server.log 2>&1 &