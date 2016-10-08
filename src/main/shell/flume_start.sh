#!/usr/bin/env bash
export FLUME_HOME=/opt/modules/flume-1.6.0
rm -rf ${FLUME_HOME}/tmp

${FLUME_HOME}/bin/flume-ng agent \
--conf ${FLUME_HOME}/conf \
--conf-file ${FLUME_HOME}/conf/sparksink.properties \
--name a1 -Dflume.root.logger=DEBUG,console

#--conf ../conf/flume-conf.properties \
