package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-12 filter
  * filter 用来对数据进行过滤
  */
object filter {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("filter")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    var arr = sc.parallelize(Array(1, 2, 3, 4, 5))
    //创建数据集
    val result = arr.filter(_ >= 3) //进行筛选工作
    result.foreach(println) //打印最终结果
  }
}
