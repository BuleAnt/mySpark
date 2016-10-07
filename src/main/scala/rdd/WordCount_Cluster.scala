package rdd

import org.apache.spark.{SparkConf, SparkContext}

/**
  * 使用Scala开发集群运行的Spark WordCount程序
  */
object WordCount_Cluster {
	def main(args: Array[String]) {


		val conf = new SparkConf()
		conf.setAppName("My First Spark App!")
		conf.setMaster("spark://hadoop:7077")

		val sc = new SparkContext(conf)

		//读取本地文件,并设置为一个partition
		val lines = sc.textFile("/home/hadoop/Documents/workspaces/IdeaProjects/mySpark/src/main/resources/text.txt")
		//val lines = sc.textFile("/user/spark/wc/input/data",3) //读取HDFS文件,并切分成不同的partition

		val words = lines.flatMap { line => line.split(" ") }
		val pairs = words.map { word => (word, 1) }
		val wordCounts = pairs.reduceByKey(_ + _)
		wordCounts.collect.foreach(wordNumberPair => println(wordNumberPair._1 + " : " + wordNumberPair._2))
		sc.stop()
	}
}
