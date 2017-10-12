package spark_mllib.book_wang.C04

import org.apache.spark.mllib.linalg.{Vector, Vectors}

/**
  * 4-1 本地向量集
  * MLlib使用本地化存储类型是向量,这里的向量主要有两类构成:
  * 稀疏数据集(spares) 和 密集型数据集 (dense)
  * dense方法可以立即为MLlib专用的一种几何形式,与Array类似
  *
  * spare方法是讲给定数据Array(9,5,3,7)分解成4个部分进行处理,
  * 对应值分别属于程序中vs的向量对应值.
  * 首先第一个参数4,代表输入数据大小,一般要大于等于输入数据值;
  * 第三个参数Array(9,5,2,7)是输入数据值,这里一般要求将其作为一个array类型的数据进行输入;
  * 第二个参数Array(0,1,2,3)是数据vs下表的数据值,这里严格要求按序增加的方法增加数据.
  * 注:
  * 在MLlib数据支持格式中,目前仅支持赠书与浮点型数据其他类型数据均不在支持范围内
  * 这与MLlib的主要用途有关,其目的是用于数值计算,
  */
object testVector {
  def main(args: Array[String]) {
    val vd: Vector = Vectors.dense(2, 0, 6) //建立密集向量
    println(vd(2)) //打印稀疏向量第3个值

    val vs: Vector = Vectors.sparse(4, Array(0, 1, 2, 3), Array(9, 5, 2, 7)) //建立稀疏向量
    println(vs(2)) //打印稀疏向量第3个值
  }
}
