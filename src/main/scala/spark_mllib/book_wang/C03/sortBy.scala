package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-19 sortBy
  * 是一个常用的排序方法,主要功能是对已有的Rdd重新排序
  * 并将重新排序后的数据生成一个新的rdd
  */
object sortBy {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("sortBy ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    var str = sc.parallelize(Array((5, "b"), (6, "a"), (1, "f"), (3, "d"), (4, "c"), (2, "e"))) //创建数据集
    str = str.sortBy(word => word._1, true)
    //按第一个数据排序
    val str2 = str.sortBy(word => word._2, true) //按第二个数据排序
    str.foreach(print) //打印输出结果
    str2.foreach(print) //打印输出结果
  }
}
