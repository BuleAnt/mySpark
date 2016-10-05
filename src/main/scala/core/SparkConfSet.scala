package core

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
	* Created by hadoop on 16-10-5.
	*/
object SparkConfSet {

	case class ParquetFormat(usr_id: BigInt, install_ids: String)

	def main(args: Array[String]) {

		// 设置消息尺寸最大值
		System.setProperty("spark.akka.frameSize", "1024")
		val conf = new SparkConf().setAppName("WriteParquet")
		//与yarn结合时设置队列
		conf.set("spark.yarn.queue", "myQueue")
		val sc = new SparkContext(conf)
		val sqlContext = new SQLContext(sc)


		sc.setLocalProperty("spark.scheduler.pool", "my")


		//读取impala的parquet，对String串的处理
		sqlContext.setConf("spark.sql.parquet.binaryAsString", "true")

		//parquetfile的写
		val appRdd = sc.textFile("hdfs://").map(_.split("\t")).map(r => ParquetFormat(r(0).toLong, r(1)))
		//repartition(1)写文件时，将所有结果汇集到一个文件
		sqlContext.createDataFrame(appRdd).repartition(1).write.parquet("hdfs://")
		//rdd如果使用toDF需要隐式转换import sqlContext.implicits._


		//parquetfile的读
		val parquetFile = sqlContext.read.parquet("hdfs://")
		parquetFile.registerTempTable("install_running")
		val data = sqlContext.sql("select user_id,install_ids from install_running")
		data.map(t => "user_id:" + t(0) + " install_ids:" + t(1)).collect().foreach(println)

		//如果重复使用的rdd，使用cache缓存
		data.cache


	}
}
