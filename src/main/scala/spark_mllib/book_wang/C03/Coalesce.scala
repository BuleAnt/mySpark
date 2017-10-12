package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-7 分片存储 coalesce 方法
  * coalesce 方法将已经存储的数据重新分片后在进行存储
  * 源码第一个参数是将数据重新分成的片数,布尔参数指的是将数据分成更小的片时使用
  */
object Coalesce {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("Coalesce")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val arr = sc.parallelize(Array(1, 2, 3, 4, 5, 6))
    //创建数据集
    val arr2 = arr.coalesce(2, true)
    //将数据重新分区
    val result = arr.aggregate(0)(math.max(_, _), _ + _) //计算数据值
    println(result)
    //打印结果
    val result2 = arr2.aggregate(0)(math.max(_, _), _ + _) //计算重新分区数据值
    println(result2)
  } //打印结果
}
