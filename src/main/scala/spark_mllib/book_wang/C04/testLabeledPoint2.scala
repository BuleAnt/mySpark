package spark_mllib.book_wang.C04

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark._
import org.apache.spark.mllib.util.MLUtils

/**
  * 4-3 调用MLUtils.loadLibSVMFile方法
  * 除了使用以上两种方法建立向量标签,MLlib还支持直接从数据库中获取固定格式的数据集方法,
  * 其格式如下:
  * label index1:value1 index2:value2 ...
  * 这里label代表给定的标签,index是索引数,value是每个索引代表的数值
  */
object testLabeledPoint2 {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local").setAppName("SparkPi")
    //建立本地环境变量
    val sc = new SparkContext(conf) //建立Spark处理
    val mu = MLUtils.loadLibSVMFile(sc, "data/2.txt") //读取文件
    mu.foreach(println) //打印内容
  }
}
