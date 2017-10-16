package spark_mllib.book_wang.C13

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 13-1 均值与方差分析
  * Sepal.Length	Sepal.Width	Petal.Length	Petal.Width	Species
  * 下面对鸢尾花数据集第一个数据萼片长度(sepal length)做分析,
  * 这里由于所有数据在一个统计表里,可以通过判断过滤的筛选出来
  *
  */
object irisMean {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("irisMean ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val data = sc.textFile("data/iris.txt") //创建RDD文件路径
      .map(_.split("\t"))
      .filter(arr => arr(arr.length - 1) == "Iris-setosa")
      .map(_.head.toDouble) //转成Double类型
      .map(line => Vectors.dense(line))
    //转成Vector格式
    val summary = Statistics.colStats(data) //计算统计量
    println("setosa中Sepal.Length的均值为：" + summary.mean) //打印均值
    println("setosa中Sepal.Length的方差为：" + summary.variance) //打印方差
  }
}
