package spark_mllib.book_wang.C07

import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 7-2 线性回归处理
  * libSVM的数据格式: Label 1:value 2:value ...
  * Label是类别的标识,比如途中的0或1,可genuine自己随意定,本例为回归,所以定义为0和1
  * Value是要训练的数据,从分类的角度来说就是特征值,数据之间使用空格隔开.
  * 而每个":"用于标注向量的序号和向量值
  *
  */
object LogisticRegression2 {
  val conf = new SparkConf()
    .setMaster("local")
    .setAppName("LogisticRegression2 ")
  val sc = new SparkContext(conf)

  def main(args: Array[String]) {
    val data = MLUtils.loadLibSVMFile(sc, "data/sample_libsvm_data.txt")
    //读取数据文件
    val model = LogisticRegressionWithSGD.train(data, 50) //训练数据模型
    println(model.weights.size) //打印θ值
    println(model.weights) //打印θ值个数
    println(model.weights.toArray.filter(_ != 0).size) //打印θ中不为0的数
  }
}
