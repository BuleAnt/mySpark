package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-17 reduce方法
  * reduce与map不同的是,它在处理数据时需要两个参数
  */
object testReduce {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("testReduce")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    var str = sc.parallelize(Array("one", "two", "three", "four", "five"))
    //创建数据集
    val result = str.reduce(_ + _) //进行数据拟合
    result.foreach(print) //打印数据结果
  }
}
