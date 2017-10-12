package spark_mllib.book_wang.C04

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.random.RandomRDDs._

/**
  * 4-15 随机数
  * 随机数是统计分析常用的一些数据文件,一般用来检验随机算法和执行效率等
  * RandomRDDs类是随机数生成类
  * normalRDD随机生成100个随机数
  */
object testRandomRDD {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("testRandomRDD")
    val sc = new SparkContext(conf)
    val randomNum = normalRDD(sc, 100)
    randomNum.foreach(println)
  }
}
