package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-13 flatMap方法
  * 对rdd数据集进行整体操作的特殊方法,返回一个整体数据集
  */
object flatMap {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("flatMap")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    var arr = sc.parallelize(Array(1, 2, 3, 4, 5))
    //创建数据集
    val result = arr.flatMap(x => List(x + 1)).collect() //进行数据集计算
    result.foreach(println) //打印结果
  }
}

