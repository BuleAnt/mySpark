package spark_mllib.book_wang.C13

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 13-9 使用逻辑回归分析长与宽之间的关系
  *
  */
object irisLogicRegression {
  val conf = new SparkConf() //创建环境变量
    .setMaster("local") //设置本地化处理
    .setAppName("irisLogicRegression ")
  val sc = new SparkContext(conf)
  val parsedData = sc.textFile("data/iris.txt")
    .map(_.split('\t'))
    .filter(arr => arr(arr.length - 1) == "Iris-setosa")
    .map(parts => LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).toDouble)))
    .cache()
  //创建模型
  val model = LinearRegressionWithSGD.train(parsedData, 20)
  val valuesAndPreds = parsedData.map { point => {
    //创建均方误差训练数据
    val prediction = model.predict(point.features) //创建数据
    (point.label, prediction) //创建预测数据
  }
  }
  val MSE = valuesAndPreds.map { case (v, p) => math.pow(v - p, 2) }.mean() //计算均方误差
  println("均方误差结果为:" + MSE) //打印结果
}

