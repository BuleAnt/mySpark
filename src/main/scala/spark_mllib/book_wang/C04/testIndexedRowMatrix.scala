package spark_mllib.book_wang.C04

import org.apache.spark._
import org.apache.spark.mllib.linalg.distributed.{IndexedRow, RowMatrix, IndexedRowMatrix}
import org.apache.spark.mllib.linalg.{Vector, Vectors}

/**
  * 4-6 带有行索引的行矩阵
  * 从4-5可以看到,单纯的航矩阵对内容无法进行直接显示,当然可以通过调用其方法显示内部数据内容.
  * 有时候,为了方便在系统调试的过程中对航矩阵的内容进行观察和显示,MLlib提供了另外一个矩阵形式
  * 即:带有行索引的行矩阵
  * 运行结果可以看出,第一行显示的IndexedRowMatrix实例化的类型,
  * 第二,三行显示的是矩阵在计算机中存储的具体内容
  * 除此之外,IndexedRowMatrix还有转化成其他矩阵的功能,
  * 例如toRowMatrix将其转化成单纯的航矩阵.
  * toCoordinateMatrix将其转化成坐标矩阵.
  * toBlockMatrix将其转化成块矩阵
  */
object testIndexedRowMatrix {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("testIndexedRowMatrix")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val rdd = sc.textFile("data/a.txt") //创建RDD文件路径
      .map(_.split(' ') //按“ ”分割
      .map(_.toDouble)) //转成Double类型
      .map(line => Vectors.dense(line)) //转化成向量存储
      .map((vd) => new IndexedRow(vd.size, vd))
    //转化格式
    val irm = new IndexedRowMatrix(rdd) //建立索引行矩阵实例
    println(irm.getClass) //打印类型
    println(irm.rows.foreach(println)) //打印内容数据
  }
}
