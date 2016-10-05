package rdd

import org.apache.spark.{SparkConf, SparkContext}

/**
	* Created by hadoop on 16-8-24.
	*/
object BroadcastDemo {
	def main(args: Array[String]) {
		val sc = new SparkContext(new SparkConf().setAppName("testBroadcast"))
		// val signPrefixes = sc.broadcast(loadCallSignTalbe())
		//val countryContactCounts =countactCounts.map
	}

}
