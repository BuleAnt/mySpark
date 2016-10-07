package rdd

import org.apache.spark.{SparkConf, SparkContext}

/**
  * BroadcastDemo
  */
object BroadcastDemo {
	def main(args: Array[String]) {
		val sc = new SparkContext(new SparkConf().setAppName("testBroadcast"))
		// val signPrefixes = sc.broadcast(loadCallSignTalbe())
		//val countryContactCounts =countactCounts.map
	}

}
