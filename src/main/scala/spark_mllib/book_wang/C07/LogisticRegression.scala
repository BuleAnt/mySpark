package spark_mllib.book_wang.C07

import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 7-1 一元逻辑回归
  */
object LogisticRegression {
  val conf = new SparkConf()
    .setMaster("local")
    .setAppName("LogisticRegression ")
  //设定名称
  val sc = new SparkContext(conf)

  def main(args: Array[String]) {
    val data = sc.textFile("data/u.txt")
    //获取数据集路径
    val parsedData = data.map { line => //开始对数据集处理
      val parts = line.split('|') //根据逗号进行分区
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).toDouble)) //转化数据格式
    }.cache()
    //建立模型
    val model = LogisticRegressionWithSGD.train(parsedData, 50)
    //创建测试值
    val target = Vectors.dense(-1)
    //根据模型计算结果
    val resulet = model.predict(target)
    println(resulet) //打印结果
  }
}

