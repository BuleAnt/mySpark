package spark_mllib.book_wang.C13

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 13-3 计算每个数据集的单独特性
  * MLlib统计方法中有一种专门用于统计宏观量的数据格式,
  * 即整体向量距离的计算方法,分别为曼哈顿距离和欧几里得距离.
  * 这两个量用来计算向量的整体程度
  *
  */
object irisNorm {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("irisNorm")
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
    println("setosa中Sepal的曼哈顿距离的值为：" + summary.normL1) //计算曼哈顿距离
    println("setosa中Sepal的欧几里得距离的值为：" + summary.normL2) //计算欧几里得距离
  }
}
