#!/usr/bin/env bash

export KAFKA_HOME=/opt/modules/kafka
${KAFKA_HOME}/bin/kafka-topics.sh  \
--describe --zookeeper hadoop:2181 \
--topic HelloKafka