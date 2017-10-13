package spark_mllib.book_wang.C11

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 11-2 主成分分析
  * Principal Component Analysis 是指讲多个变量通过现行变换以选出较少数重要变量的一种多远统计分析方法,
  * 又称主成分分析.
  * MLlib的PCA算法与SVD类似,同样建立在行矩阵智商的数据处理方法
  */
object PCA {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("PCA ")
    val sc = new SparkContext(conf)

    val data = sc.textFile("data/11.txt")
      .map(_.split(' ')
        .map(_.toDouble))
      .map(line => Vectors.dense(line))
    val rm = new RowMatrix(data)
    //提取主成分，设置主成分个数
    val pc = rm.computePrincipalComponents(3)
    //创建主成分矩阵
    val mx = rm.multiply(pc)
    mx.rows.foreach(println)
  }
}
