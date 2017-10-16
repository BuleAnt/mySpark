package spark_mllib.book_wang.C13

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 13-7 使用线性回归分析萼片长和宽
  * 13-8 使用MLlib自带的均方误差
  */
object irisLinearRegression {
  val conf = new SparkConf()
    .setMaster("local")
    .setAppName("irisLinearRegression ")
  val sc = new SparkContext(conf)

  def main(args: Array[String]) {
    val parsedData = sc.textFile("data/iris.txt")
      .map(_.split('\t'))
      .filter(arr => arr(arr.length - 1) == "Iris-setosa")
      .map(parts => LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).toDouble)))
      .cache()
    //加载数据
    val model = LinearRegressionWithSGD.train(parsedData, 10, 0.1) //创建模型
    println("回归公式为: y = " + model.weights + " * x + " + model.intercept) //打印回归公式

    // 13-8
    val valuesAndPreds = parsedData.map { point => {
      //创建均方误差训练数据
      val prediction = model.predict(point.features) //创建数据
      (point.label, prediction) //创建预测数据
    }
    }
    val MSE = valuesAndPreds.map { case (v, p) => math.pow(v - p, 2) }.mean() //计算均方误差
    println("均方误差结果为:" + MSE) //打印结果
  }
}
