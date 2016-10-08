#!/bin/bash
if [ $# -lt 1 -o ! -e $1 ] ;then
  echo can not get the path $1
  exit;
fi

arg=$1
# get absolute parent path
parentDir=`dirname ${arg}`
if [ -d ${arg} ];then
	dir=`cd ${arg} && pwd`
	parentDir=${dir%/*}
fi
absoluteDir=`cd ${parentDir} && pwd`


# get host and foreach scp
# slave=`cat /opt/modules/hadoop-2.7.2/etc/hadoop/slaves`
user=`whoami`
for i in hadoop1 hadoop2 ;do
  echo -----coping ${arg} to ${i} ------;
  scp -r ${arg} ${user}@${i}:${absoluteDir}
done