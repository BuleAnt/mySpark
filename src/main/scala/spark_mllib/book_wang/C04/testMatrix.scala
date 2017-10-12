package spark_mllib.book_wang.C04

import org.apache.spark.mllib.linalg.{Matrix, Matrices}

/**
  * 4-4 本地矩阵
  * 大数据运算中,为了更好提升计算效率,可以更多的使用矩阵运算进行处理.
  * 部分单机中的本地军阵就是个很好的存储方法
  */
object testMatrix {
  def main(args: Array[String]) {
    // 2 行 3 列矩阵
    val mx = Matrices.dense(2, 3, Array(1,2,3,4,5,6))                    //创建一个分布式矩阵
    println(mx)                                                     //打印结果
  }
}
