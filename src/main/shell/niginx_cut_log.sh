#!/bin/bash
## 零点执行该脚本
## Nginx 日志文件所在的目录
LOGS_PATH=/usr/local/nginx/logs
LOGS_NAME="access.log"
PID_PATH="/usr/local/nginx/nginx.pid"
## 获取昨天的 yyyy-MM-dd
YESTERDAY=$(date -d "yesterday" +%Y-%m-%d)
LAST_WEEK=$(date --date="LAST WEEK" +"%Y-%m-%d")

## 移动文件
mv ${LOGS_PATH}/${LOGS_NAME} ${LOGS_PATH}/${LOGS_NAME}_${YESTERDAY}.log
## 向 Nginx 主进程发送 USR1 信号。USR1 信号是重新打开日志文件
kill -USR1 `cat ${PID_PATH}`
