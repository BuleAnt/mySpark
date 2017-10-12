package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-14 map方法
  * map可以对rdd数据集中数据逐个操作,他与flatmap不同之处在于,
  * flatmap是将数据集数据桌位一个整体处理,之后对其中的数据做计算.
  * 而map方法直接对数据集中的数据做单独处理
  */
object testMap {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("testMap ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    var arr = sc.parallelize(Array(1, 2, 3, 4, 5))
    //创建数据集
    val result = arr.map(x => List(x + 1)).collect() //进行单个数据计算
    result.foreach(println) //打印结果
  }
}
