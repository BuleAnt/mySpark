#!/usr/bin/env bash

export KAFKA_HOME=/opt/modules/kafka

${KAFKA_HOME}/bin/kafka-console-consumer.sh \
--zookeeper hadoop:2181 \
--topic HelloKafka \
--from-beginning