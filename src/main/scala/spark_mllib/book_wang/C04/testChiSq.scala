package spark_mllib.book_wang.C04

import org.apache.spark.mllib.linalg.{Matrices, Vectors}
import org.apache.spark.mllib.stat.Statistics

/**
  * 4-14 假设检验
  * 卡方检验是一种常用的的假设检验方法,能够较好地多数据集之间的拟合度,相关性和独立性进行验证.
  * Mlib规定常用的卡方检验使用的数据集一般为向量和矩阵.
  * 从结果上看,假设检验输出结果包含三个数据,分别为自由度,P值和统计量.
  * 自由度:总体参数估计量中变量值独立自由变化的数目
  * 统计量:不同方法下的统计量
  * P值:显著性差异指标
  * 方法:卡方检验使用方法
  *
  * 卡方检验使用了皮尔逊计算法对数据进行计算,得到最终结果P值,一般P<0.05是指数据集不存在显著性差异
  */
object testChiSq {
  def main(args: Array[String]) {
    val vd = Vectors.dense(1, 2, 3, 4, 5)
    //
    val vdResult = Statistics.chiSqTest(vd)
    println(vdResult)
    println("-------------------------------")
    val mtx = Matrices.dense(3, 2, Array(1, 3, 5, 2, 4, 6))
    val mtxResult = Statistics.chiSqTest(mtx)
    println(mtxResult)
  }
}
