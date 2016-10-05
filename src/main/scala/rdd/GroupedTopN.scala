package rdd

import org.apache.spark.{SparkConf, SparkContext}

/**
	* GroupedTopN
	*/
object GroupedTopN {
	def main(args: Array[String]) {
		val sc = new SparkContext(new SparkConf().setAppName("TopN").setMaster("local"))
		//  sc.setLogLevel("OFF")
		val lines = sc.textFile("src/main/resources/text.txt")
		val groupRDD = lines.map(line => (line.split(" ")(0), line.split(" ")(1).toInt)).groupByKey()
		val top5 = groupRDD.map(pair => (pair._1, pair._2.toList.sortWith(_ > _).take(5))).sortByKey()
		top5.collect().foreach(pair => {
			println(pair._1 + ":")
			pair._2.foreach(println)
			println("*****************")
		})

	}
}
