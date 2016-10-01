package core

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-8-24.
  */
object RDDDemo {
  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("RDDDemo").setMaster("local"))
    val input = sc.parallelize(List(1, 2, 3, 4))
    val result = input.map(x => x * x)
    println(result.collect().mkString(","))

    val lines = sc.parallelize(List("Hello world ", "hi"))
    val words = lines.flatMap(line => line.split(","))
    words.first()


  }

}
