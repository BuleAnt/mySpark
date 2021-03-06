package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-8 repartition方法
  * repartition和coalesce方法类似,均将数据重新分区组合
  */
object Repartition {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("Repartition ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    var arr = sc.parallelize(Array(1, 2, 3, 4, 5, 6)) //创建数据集
    arr = arr.repartition(3) //重新分区
    println(arr.partitions.length)
  } //打印分区数
}
