package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-10 countByKey
  * countByKey方法与countByValue方法有本质的区别
  * countByKey是计算数据中元数据键值对key出现的个数
  */
object countByKey {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("countByKey ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    var arr = sc.parallelize(Array((1, "cool"), (2, "good"), (1, "bad"), (1, "fine")))
    //创建数据集
    val result = arr.countByKey() //进行计数
    result.foreach(print) //打印结果
  }
}
