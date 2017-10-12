package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-9 countByValue
  * countByValue方法是计算数据集中某个数据出现的个数
  * 并将其以map的形式返回.
  */
object countByValue {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("countByValue ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val arr = sc.parallelize(Array(1, 2, 3, 4, 5, 6))
    //创建数据集
    val result = arr.countByValue() //调用方法计算个数
    result.foreach(print) //打印结果
  }
}