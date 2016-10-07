#!/usr/bin/env bash

for ((i = 1; i<= 3;i++))
do
	for((j = 1;j<=10;j++))
	do
	    echo "hello spark streaming "${i}${j} >> /home/hadoop/test/flume/spool/test${i}
	done
    sleep 2
done

#clean
rm -rf /home/hadoop/test/flume/*.COMPLETED
