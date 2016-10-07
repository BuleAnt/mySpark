package spark_streaming

import kafka.serializer.StringDecoder
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * KafkaDataTest
  */
object KafkaDataTest {
	def main(args: Array[String]): Unit = {
		org.apache.log4j.Logger.getLogger("org.apache.spark").setLevel(org.apache.log4j.Level.WARN)
		org.apache.log4j.Logger.getLogger("org.eclipse.jetty.server").setLevel(org.apache.log4j.Level.ERROR)

		val conf = new SparkConf().setAppName("stocker").setMaster("local[2]")
		val sc = new SparkContext(conf)

		val ssc = new StreamingContext(sc, Seconds(1))

		// Kafka configurations

		val topics = Set("mytopic")

		val brokers = "spark1:9092,spark2:9092,spark3:9092"

		val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers, "serializer.class" -> "kafka.serializer.StringEncoder")

		// Create a direct stream
		val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

		val urlClickLogPairsDStream = kafkaStream.flatMap(_._2.split(" ")).map((_, 1))

		val urlClickCountDaysDStream = urlClickLogPairsDStream.reduceByKeyAndWindow(
			(v1: Int, v2: Int) => {
				v1 + v2
			}, Seconds(60), Seconds(5))

		urlClickCountDaysDStream.print()

		ssc.start()
		ssc.awaitTermination()
	}
}
