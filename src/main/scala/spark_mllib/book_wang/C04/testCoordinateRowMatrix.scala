package spark_mllib.book_wang.C04

import org.apache.spark._
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}

/**
  * 4-7 坐标矩阵
  * 坐标矩阵是一种带有坐标表及的矩阵,其中每个具体数据都有一组坐标标示
  * 其格式如下: (x:Long,y.txt:Long, value:Double)
  * 从格式上看,x,y分别标示坐标轴,value是具体内容
  * 坐标矩阵一般用于数据比较多且数据较为分散的情形,即矩阵中含0或者某个具体值较多的情况下.
  */
object testCoordinateRowMatrix {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("testIndexedRowMatrix") //设定名称

    val sc = new SparkContext(conf)
    //创建环境变量实例
    val rdd = sc.textFile("data/a.txt") //创建RDD文件路径
      .map(_.split(' ') //按“ ”分割
      .map(_.toDouble)) //转成Double类型
      .map(vue => (vue(0).toLong, vue(1).toLong, vue(2))) //转化成坐标格式
      .map(vue2 => MatrixEntry(vue2._1, vue2._2, vue2._3))
    //转化成坐标矩阵格式
    val crm = new CoordinateMatrix(rdd) //实例化坐标矩阵
    println(crm.entries.foreach(println)) //打印数据
  }
}
