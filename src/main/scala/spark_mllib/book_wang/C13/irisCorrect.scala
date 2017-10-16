package spark_mllib.book_wang.C13

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 13-4 计算相关系数
  * Sepal.Length	Sepal.Width	Petal.Length	Petal.Width	Species
  * Iris-setosa 的Sepal.Length 和 Sepal.Width 相关系数
  * 其他类似
  */
object irisCorrect {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("irisCorrect ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val dataX = sc.textFile("data/iris.txt")
      .map(_.split('\t'))
      .filter(arr => arr(arr.length - 1) == "Iris-setosa")
      .map(_ (0).toDouble)
    //转化为Double类型
    val dataY = sc.textFile("data/iris.txt")
      .map(_.split('\t'))
      .filter(arr => arr(arr.length - 1) == "Iris-setosa")
      .map(_ (1).toDouble)
    //转化为Double类型
    val correlation: Double = Statistics.corr(dataX, dataY) //计算不同数据之间的相关系数
    println(correlation) //打印结果
  }
}
