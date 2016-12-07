package spark_streaming.demo

import java.util.Properties

import kafka.producer.ProducerConfig
import org.json.JSONArray

import scala.util.Random

/**
  * 首先我们要生产数据源，实际的场景下，会有上报好的日志数据，
  * 这里，我们就直接写一个模拟数据类，实现代码如下所示：
  */
object KafkaIPLoginProducer {
//	private val uid = Array("123dfe", "234weq", "213ssf")
//
//	private val random = new Random()
//
//	private var pointer = -1
//
//	def getUserID(): String = {
//		pointer = pointer + 1
//		if (pointer >= users.length) {
//			pointer = 0
//			uid(pointer)
//		} else {
//			uid(pointer)
//		}
//	}
//
//	def plat(): String = {
//		random.nextInt(10) + "10"
//	}
//
//	def ip(): String = {
//		random.nextInt(10) + ".12.1.211"
//	}
//
//	def country(): String = {
//		"中国" + random.nextInt(10)
//	}
//
//	def city(): String = {
//		"深圳" + random.nextInt(10)
//	}
//
//	def location(): JSONArray = {
//		JSON.parseArray("[" + random.nextInt(10) + "," + random.nextInt(10) + "]")
//	}
//
//	def main(args: Array[String]): Unit = {
//		val topic = "test_data3"
//		val brokers = "dn1:9092,dn2:9092,dn3:9092"
//		val props = new Properties()
//		props.put("metadata.broker.list", brokers)
//		props.put("serializer.class", "kafka.serializer.StringEncoder")
//
//		val kafkaConfig = new ProducerConfig(props)
//		val producer = new Producer[String, String](kafkaConfig)
//
//		while (true) {
//			val event = new JSONObject()
//
//			event
//					.put("_plat", "1001")
//					.put("_uid", "10001")
//					.put("_tm", (System.currentTimeMillis / 1000).toString())
//					.put("ip", ip)
//					.put("country", country)
//					.put("city", city)
//					.put("location", JSON.parseArray("[0,1]"))
//			printlsn("Message sent: " + event)
//			producer.send(new KeyedMessage[String, String](topic, event.toString))
//
//			event
//					.put("_plat", "1001")
//					.put("_uid", "10001")
//					.put("_tm", (System.currentTimeMillis / 1000).toString())
//					.put("ip", ip)
//					.put("country", country)
//					.put("city", city)
//					.put("location", JSON.parseArray("[0,1]"))
//			println("Message sent: " + event)
//			producer.send(new KeyedMessage[String, String](topic, event.toString))
//
//			event
//					.put("_plat", "1001")
//					.put("_uid", "10002")
//					.put("_tm", (System.currentTimeMillis / 1000).toString())
//					.put("ip", ip)
//					.put("country", country)
//					.put("city", city)
//					.put("location", JSON.parseArray("[0,1]"))
//			println("Message sent: " + event)
//			producer.send(new KeyedMessage[String, String](topic, event.toString))
//
//			event
//					.put("_plat", "1002")
//					.put("_uid", "10001")
//					.put("_tm", (System.currentTimeMillis / 1000).toString())
//					.put("ip", ip)
//					.put("country", country)
//					.put("city", city)
//					.put("location", JSON.parseArray("[0,1]"))
//			println("Message sent: " + event)
//			producer.send(new KeyedMessage[String, String](topic, event.toString))
//			Thread.sleep(30000)
//		}
//	}
}