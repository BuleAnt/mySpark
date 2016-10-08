#!/usr/bin/env bash
export KAFKA_HOME=/opt/modules/kafka

${KAFKA_HOME}/bin/kafka-console-producer.sh \
--broker-list hadoop:9092 \
--topic HelloKafka