package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-15
  * groupBy 方法讲传入的数据进行分组,
  * 分组的依据是作为参数传入的计算方法
  */
object groupBy {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("groupBy ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    var arr = sc.parallelize(Array(1, 2, 3, 4, 5)) //创建数据集
    arr.groupBy(myFilter(_), 1) //设置第一个分组
    arr.groupBy(myFilter2(_), 2) //设置第二个分组
  }

  def myFilter(num: Int): Unit = {
    //自定义方法
    num >= 3 //条件
  }

  def myFilter2(num: Int): Unit = {
    //自定义方法
    num < 3 //条件
  }

}
