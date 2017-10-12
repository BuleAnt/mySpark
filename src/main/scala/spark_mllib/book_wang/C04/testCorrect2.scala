package spark_mllib.book_wang.C04

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 4-11 斯皮尔曼相关系数
  * 公式: p_xy = 1- 6*∑(x_i-y_i)^2/n*(n^2-1)
  * n为数据个数,
  */
object testCorrect2 {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("testCorrect2 ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val rddX = sc.textFile("c://x.txt") //读取数据
      .flatMap(_.split(' ') //进行分割
      .map(_.toDouble))
    //转化为Double类型
    val rddY = sc.textFile("c://y.txt.txt") //读取数据
      .flatMap(_.split(' ') //进行分割
      .map(_.toDouble))
    //转化为Double类型
    val correlation: Double = Statistics.corr(rddX, rddY, " spearman ") //使用斯皮尔曼计算不同数据之间的相关系数
    println(correlation) //打印结果
  }
}
