#!/usr/bin/env bash
#!/bin/bash


PREFIX="/opt/modules/flume-1.6.0"
AGENT="a1"

start(){
echo  starting
ps aux |grep -v grep|grep ${PREFIX} -q
if [ $? -eq 0 ];then
    echo flume is already running
    exit 1
fi
cd ${PREFIX}
bin/flume-ng agent -n ${AGENT} -c conf -f conf/nginx_agent.properties 2>/dev/null &
}

stop(){
ps aux |grep -v grep|grep ${PREFIX} -q
if [ $? -ne 0 ];then
    echo flume is not running
else
    ps aux |grep -v grep|grep ${PREFIX}|awk '{print $2}'|xargs kill -15
    sleep 3
    ps aux |grep -v grep|grep ${PREFIX} -q
    [ $? -eq 0 ]  && ps aux |grep flume|grep -v grep|awk '{print $2}'|xargs kill -9
fi
}

status(){
ps aux |grep -v grep|grep ${PREFIX} -q
if [ $? -eq 0 ];then
    echo flume is running
else
    echo flume is not running
fi
}


case $1 in
    start)
        start
    ;;
    stop)
        stop
    ;;
    restart)
        stop
        sleep 1
        start
    ;;
    status)
        status
    ;;
    *)
        echo "Useage : $0 [start|stop|restart|status]"
    ;;
esac