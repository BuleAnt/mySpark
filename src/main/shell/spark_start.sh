#!/usr/bin/env bash
export HADOOP_HOME=/opt/modules/hadoop-2.7.2
export SPARK_HOME=/opt/modules/spark-1.6.1
export HIVE_HOME=/opt/modules/hive-1.2.1

${HADOOP_HOME}/sbin/start-dfs.sh
${HADOOP_HOME}/sbin/start-yarn.sh
##start hiveClient interface. hive-site.xml
# bin/hive --service hiveserver2 &

sudo service mysqld start
sleep 3

${HIVE_HOME}/bin/hive --service metastore &



##Start Spark
# $SPARK_HOME/sbin/start-all.sh
${SPARK_HOME}/sbin/start-master.sh -h hadoop -p 7077 -\
# --webui-port 8010 --properties-file ${SPARK_HOME}/conf/spark-defaults.conf

# $SPARK_HOME/sbin/start-slaves.sh
${SPARK_HOME}/sbin/start-slave.sh -h hadoop -p 7078 \
# --webui-port 8011 -c 8 -m 8G -d ${SPARK_HOME}/work \
#--properties-file ${SPARK_HOME}/conf/spark-defaults.conf spark://hadoop:7077

${SPARK_HOME}/sbin/start-history-server.sh

##Start Spark-Shell Local Mode
# $SPARK_HOME/bin/spark-shell --master local[4]

#Start Spark-Shell Standalon Mode

#${SPARK_HOME}/bin/spark-shell \
#--master spark://hadoop:7077

##submit jar Local Mode
# $SPARK_HOME/bin/spark-submit --master local[*] /path/to/examples.jar  1000
##standalone Mode client/cluster
# $SPARK_HOME/bin/spark-submit --master spark://hadoop:7077 --deploy-mode client  --executor-memory 4G --total-executor-cores 4 /path/to/examples.jar  1000
##yarn Mode, conf hadoop
# $SPARK_HOME/bin/spark-submit --master yarn --deploy-mode cluster  --executor-memory 4G --num-executors 4 /path/to/examples.jar  1000
