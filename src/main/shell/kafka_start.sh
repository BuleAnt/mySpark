#!/usr/bin/env bash
export KAFKA_HOME=/opt/modules/kafka
nohup ${KAFKA_HOME}/bin/kafka-server-start.sh ${KAFKA_HOME}/config/server.properties &