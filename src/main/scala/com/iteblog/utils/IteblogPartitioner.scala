package com.iteblog.utils

import org.apache.spark.Partitioner

/**
  * User: 过往记忆
  * Time: 下午23:34
  * bolg: https://www.iteblog.com
  * 本文地址：https://www.iteblog.com/archives/1368
  * 过往记忆博客，专注于hadoop、hive、spark、shark、flume的技术博客，大量的干货
  * 过往记忆博客微信公共帐号：iteblog_hadoop
  */

class IteblogPartitioner(numParts: Int) extends Partitioner {
	override def numPartitions: Int = numParts

	override def getPartition(key: Any): Int = {
		val domain = new java.net.URL(key.toString).getHost()
		val code = (domain.hashCode % numPartitions)
		if (code < 0) {
			code + numPartitions
		} else {
			code
		}
	}

	override def equals(other: Any): Boolean = other match {
		case iteblog: IteblogPartitioner =>
			iteblog.numPartitions == numPartitions
		case _ =>
			false
	}

	override def hashCode: Int = numPartitions
}
