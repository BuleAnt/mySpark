#!/usr/bin/env bash
export KAFKA_HOME=/opt/modules/kafka
${KAFKA_HOME}/bin/kafka-topics.sh \
--create --zookeeper hadoop:2181 \
--replication-factor 1 --partitions 1 \
--topic UserLogs

##伪分布式由于只有一个Broker,所以replication只能设置为1