package spark_mllib.book_wang.C13

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 13-2 所有数据在一起的均值和方差
  * 可以看到这里计算出了一个均值和方差
  */
object irisALL {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("irisAll ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val data = sc.textFile("data/iris.txt") //创建RDD文件路径
      .map(_.split('\t').head.toDouble) //转成Double类型
      .map(line => Vectors.dense(line))
    //转成Vector格式
    val summary = Statistics.colStats(data) //计算统计量
    println("全部Sepal.Length的均值为：" + summary.mean) //打印均值
    println("全部Sepal.Length的方差为：" + summary.variance) //打印方差
  }
}
