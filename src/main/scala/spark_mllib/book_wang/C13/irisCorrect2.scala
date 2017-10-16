package spark_mllib.book_wang.C13

import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 13-5 对不同类别植物的相同特性进行分析
  * 可以看到不同种类的同种特性之间,只有很低的相关性
  * 因此可以更清楚地查看到这些相同特性之间相关系数的关系
  * 13-6 相同??
  */
object irisCorrect2 {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("irisCorrect2")
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
      .filter(arr => arr(arr.length - 1) == "Iris-versicolor")
      .map(_ (0).toDouble)
    //转化为Double类型
    val correlation: Double = Statistics.corr(dataX, dataY) //计算不同数据之间的相关系数
    println("setosa和versicolor中Sepal.Length的相关系数为：" + correlation) //打印相关系数                                      
  }
}
