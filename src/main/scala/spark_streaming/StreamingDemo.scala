package spark_streaming

import org.apache.spark.SparkConf
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Flume-Kafka-Spark Streaming-Hive
  */

case class MessageItem(name: String, age: Int)

object StreamingDemo {

	def main(args: Array[String]) {
		if (args.length < 2) {
			System.err.println("please input you Kafka Broker list and topic to receive")
			System.exit(1)
		}
		val conf = new SparkConf()
		conf.setMaster("local[4]").setAppName("Demo")

		val ssc = new StreamingContext(conf, Seconds(5))
		val Array(brokers: String, topicsList: String) = args

		val kafkaParams = Map[String, String] {
			"metadata.broker.list" -> brokers
		}
		val topics = topicsList.split(",").toSet

	/*	KafkaUtils.createDirectStream(ssc, kafkaParams, topics).map(_._2.asInstanceOf[String].split(",")).foreachRDD(rdd => {
			val hiveContext = new HiveContext(rdd.context)

			import hiveContext.implicits._
			rdd.map(record => MessageItem(record(0).trim, record(1).trim.toInt))
					.toDF().registerTempTable("temp")
			hiveContext.sql("select count(*) from temp").show
			//TODO 把数据写入到hive中,通过Java技术访问hive中的内容,
			//TODO 通过Flume做原始数据收集,Flume会作为Kafka的Producer,把数据写入到Kafka中供本程序使用
		})
*/
		ssc.start()
		ssc.awaitTermination()
		ssc.stop()
	}
}

