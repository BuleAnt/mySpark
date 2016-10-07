package spark_streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * *背景描述 在广告点击计费系统中 我们在线过滤掉 黑名单的点击 进而保护广告商的利益
  * 只有效的广告点击计费
  */
object OnlineBlackListFilter {
	def main(args: Array[String]) {
		val conf = new SparkConf()
		conf.setAppName("OnlineBlackListFilter")
		conf.setMaster("spark://hadoop:7077")
		val ssc = new StreamingContext(conf, Seconds(30))

		/**
		  * 黑名单数据准备，实际上黑名单一般都是动态的，例如在Redis或者数据库中，
		  * 黑名单的生成往往有复杂的业务逻辑，具体情况算法不同，
		  * 但是在Spark Streaming进行处理的时候每次都能够访问完整的信息。
		  */
		val blackList = Array(("Spy", true), ("Cheater", true))
		val blackListRDD = ssc.sparkContext.parallelize(blackList, 8)

		val adsClickStream = ssc.socketTextStream("hadoop", 9999)

		/**
		  * 此处模拟的广告点击的每条数据的格式为：time、name
		  * （time，name）-->map->(name,（time，name）)
		  */
		val adsClickStreamFormatted = adsClickStream.map { ads => (ads.split(" ")(1), ads) }
		adsClickStreamFormatted.transform(userClickRDD => {
			// (name,（time，name）)leftOuterJoin(name, boolean)-->（name,((time,name), boolean)）
			// 通过leftOuterJoin操作既保留了左侧用户广告点击内容的RDD的所有内容， 又获得了相应点击内容是否在黑名单中
			val joinedBlackListRDD = userClickRDD.leftOuterJoin(blackListRDD)

			/**
			  * 进行filter过滤的时候，其输入元素是一个Tuple：（name,((time,name), boolean)）
			  * 其中第一个元素是黑名单的名称，第二元素的第二个元素是进行leftOuterJoin的时候是否存在的值。
			  * 如果存在的话，表示当前广告点击是黑名单，需要过滤掉，否则的话是有效点击内容；
			  */
			val validClicked = joinedBlackListRDD.filter(joinedItem => {
				if (joinedItem._2._2.getOrElse(false)) {
					false
				} else {
					true
				}

			})

			validClicked.map(validClick => {
				validClick._2._1
			})
		}).print

		/**
		  * 计算后的有效数据一般都会写入Kafka中，下游的计费系统会从kafka中pull到有效数据进行计费
		  */
		ssc.start()
		ssc.awaitTermination()
	}
}
