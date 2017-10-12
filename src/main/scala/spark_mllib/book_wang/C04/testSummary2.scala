package spark_mllib.book_wang.C04

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 4-9 距离计算
  * normL1 和normL2代表欧几里得距离和曼哈顿距离
  * 欧几里得距离是一个常用的距离定义,在m维空间中两点的真是距离,或向量自然长度,公式为:
  * x = (x_1^2+x_2^2+...+x_n^2)^(1/2)
  * 曼哈顿距离用来表明两个点标准坐标系上的绝对轴距总和.公式如下:
  * x = x_1+x_2+x_3+...+x_n
  * 则根据以上,可得数据的距离:
  * normL1 = 1+2+3+4+5=15
  * normL2 = (1+4+9+16)^(1/2)=7.416
  */
object testSummary2 {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("testSummary2")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val rdd = sc.textFile("data/b.txt") //创建RDD文件路径
      .map(_.split(' ') //按“ ”分割
      .map(_.toDouble)) //转成Double类型
      .map(line => Vectors.dense(line))
    //转成Vector格式
    val summary = Statistics.colStats(rdd) //获取Statistics实例
    println(summary.normL1) //计算曼哈段距离
    println(summary.normL2) //计算欧几里得距离
  }
}

