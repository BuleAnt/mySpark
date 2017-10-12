package spark_mllib.book_wang.C04

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 4-10 皮尔逊相关系数
  * 相关系数用来反映变量之间相关关系密切程度的统计指标
  * 在现实中一般要勇于对两组间的拟合和相似程度进行量化分析.
  * 常用的一般是皮尔逊相关系数,公式如下:
  * r = 协方差/方差乘积
  */
object testCorrect {
  def main(args: Array[String]) {
val conf = new SparkConf()                                       //创建环境变量
.setMaster("local")                                               //设置本地化处理
.setAppName("testCorrect ")                                    //设定名称
    val sc = new SparkContext(conf)                                  //创建环境变量实例
    val rddX = sc.textFile("data/x.txt")                                   //读取数据
        .flatMap(_.split(' ')                                           //进行分割
        .map(_.toDouble))                                           //转化为Double类型
    val rddY = sc.textFile("data/y.txt")                                   //读取数据
      .flatMap(_.split(' ')                                             //进行分割
      .map(_.toDouble))                                            //转化为Double类型
    val correlation: Double = Statistics.corr(rddX, rddY)                 //计算不同数据之间的相关系数
    println(correlation)                                              //打印结果
  }
}
