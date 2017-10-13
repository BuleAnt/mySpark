package spark_mllib.book_wang.C11

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 11-1 SVD奇异值分解
  */
object SVD {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("SVD ")

    val sc = new SparkContext(conf) //创建环境变量实例

    val data = sc.textFile("data/11.txt") //创建RDD文件路径
      .map(_.split(' ') //按“ ”分割
      .map(_.toDouble)) //转成Double类型
      .map(line => Vectors.dense(line)) //转成Vector格式
    //读入行矩阵
    val rm = new RowMatrix(data)

    val SVD = rm.computeSVD(2, computeU = true) //进行SVD计算
    println(SVD) //打印SVD结果矩阵
  }
}

