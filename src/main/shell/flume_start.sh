#!/usr/bin/env bash

rm -rf /opt/modules/flume-1.6.0/tmp

/opt/modules/flume-1.6.0/bin/flume-ng agent \
--conf /opt/modules/flume-1.6.0/bin/flume-ng/conf \
--conf-file /opt/modules/flume-1.6.0/bin/flume-ng/conf/sparksink.properties \
--name a1 -Dflume.root.logger=DEBUG,console

#--conf ../conf/flume-conf.properties \
