package core

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-7-8.
  */
object Tranformattions {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Tranformations").setMaster("local")
    val sc = new SparkContext(conf)
    //val sc = sparkContext("Tranformations Oprations")

    //mapped.collect.foreach(println)
    mapTranformation(sc)

    //val filtered = nums.filter(item => item % 2 == 0)
    filterTranformation(sc)

    val bigData = Array("Scala Spark", "Java Hadoop", "Java Tachyon")
    val bigDataString = sc.parallelize(bigData)
    val words = bigDataString.flatMap(line => line.split(" "))
    words.collect.foreach(println)
    sc.stop()
  }

  def sparkContext(name: String) = {
    val conf = new SparkConf().setAppName(name).setMaster("local")
    val sc = new SparkContext(conf)
    sc
  }

  def mapTranformation(sc: SparkContext) = {
    val nums = sc.parallelize(1 to 10)
    val mapped = nums.map(item => 2 * item)
    mapped.collect.foreach(println)
  }

  def filterTranformation(sc: SparkContext): Unit = {
    val nums = sc.parallelize(1 to 10)
    val filtered = nums.filter(item => item % 2 == 0)
    filtered.collect.foreach(println)
  }

  def flatMapTranformation(sc: SparkContext): Unit = {
    val bigData = Array("Scala Spark", "Java Hadoop", "Java Tachyon")
    val bigDataString = sc.parallelize(bigData)
    val words = bigDataString.flatMap(line => line.split(" "))
    words.collect.foreach(println)
  }
}
