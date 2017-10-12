package spark_mllib.book_wang.C04

import org.apache.spark._
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.RowMatrix

/**
  * 4-5 行矩阵
  * 行矩阵是最基本的一种矩阵类型,是以行作为基本方向的矩阵存储格式.列的作用相对较小.
  * 可以理解为行矩阵是一个巨大的特征向量的集合.
  * 每行就是一个具有相同格式的向量数据,而且每一行的向量内容都可以单独取出来进行操作.
  */
object testRowMatrix {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      //设定名称
      .setAppName("testRowMatrix")
    //创建环境变量实例
    val sc = new SparkContext(conf)
    val rdd = sc.textFile("data/a.txt") //创建RDD文件路径
      .map(_.split(' ') //按“ ”分割
      .map(_.toDouble)) //转成Double类型
      .map(line => Vectors.dense(line)) //转成Vector格式

    val rm = new RowMatrix(rdd) //读入行矩阵
    println(rm.numRows()) //打印列数
    println(rm.numCols()) //打印行数
  }
}
