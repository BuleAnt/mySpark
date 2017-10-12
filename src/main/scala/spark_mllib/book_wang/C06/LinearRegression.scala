package spark_mllib.book_wang.C06

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 6-2 线性回归
  * 数据 lpsa2.data 逗号前数值是根据不同数据求出的结果值,
  * 而每个系数的x值一次被排列在其后,这些就是数据的收集规则
  * train 方法第一个参数接收一个RDD[LabeledPoint]
  * numIterations 是整体模型的迭代次数,理论上迭代次数越多则模型的拟合度越高,但随之而来的迭代需要的市场越长
  * stepSize是随机梯度下降算法中的步进系数,代表每次迭代过程中模型的整体修正程度
  */
object LinearRegression {
  val conf = new SparkConf()
    .setMaster("local")
    .setAppName("LinearRegression ")
  val sc = new SparkContext(conf)

  def main(args: Array[String]) {
    val data = sc.textFile("data/lpsa2.data")
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }.cache()
    //转化数据格式
    val model = LinearRegressionWithSGD.train(parsedData, 100, 0.1)
    //建立模型
    val result = model.predict(Vectors.dense(2)) //通过模型预测模型
    println(result) //打印预测结果
  }

}
