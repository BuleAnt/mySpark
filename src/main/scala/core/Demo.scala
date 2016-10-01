package core

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-8-16.
  */
object Demo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sc = new SparkContext(conf)
    val text = sc.textFile("file:///usr/local/spark/README.md")
    val result = text.flatMap(_.split(' ')).map((_, 1)).reduceByKey(_ + _).collect()
    result.foreach(println)
  }
}
