#!/usr/bin/env bash
spark-submit --class com.study.spark.WordCount --master spark://hadoop:7010 /home/hadoop/Documents/workspaces/export/wordcount.jar

