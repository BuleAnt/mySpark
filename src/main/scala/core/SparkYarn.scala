package core

import org.apache.spark.SparkContext

/**
  * Created by hadoop on 16-6-30.
  */

object SparkYarn {
  def main(args: Array[String]) {
    val sc = new SparkContext
    val data = sc.textFile(args(0))
    data.filter(_.split(' ').length == 3)
      .map((_.split(' ')(1) -> 1))
      .reduceByKey(_ + _)
      .map(x => (x._2 -> x._1))
      .sortByKey(false)
      .map(y => (y._2 -> y._1))
      .saveAsTextFile(args(1))
  }
}
