package spark_mllib.book_huang

import org.apache.log4j.{ Level, Logger }
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.classification.{ LogisticRegressionWithLBFGS, LogisticRegressionModel }
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils

/**
  * 介绍一篇很详细的文章
  * http://blog.csdn.net/lookqlp/article/details/51161640
  */
object logistic_regression {

  def main(args: Array[String]) {
    //1 构建Spark对象
    val conf = new SparkConf().setAppName("logistic_regression")
    val sc = new SparkContext(conf)
    Logger.getRootLogger.setLevel(Level.WARN)

    // 读取样本数据1，格式为LIBSVM format
    val data = MLUtils.loadLibSVMFile(sc, "dadta/sample_libsvm_data.txt")

    //样本数据划分训练样本与测试样本
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    //新建逻辑回归模型，并训练
    val model = new LogisticRegressionWithLBFGS().
      setNumClasses(10).
      run(training)
    model.weights
    model.intercept

    //对测试样本进行测试
    val predictionAndLabels = test.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        (prediction, label)
    }
    val print_predict = predictionAndLabels.take(20)
    println("prediction" + "\t" + "label")
    for (i <- 0 to print_predict.length - 1) {
      println(print_predict(i)._1 + "\t" + print_predict(i)._2)
    }

    // 误差计算
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val precision = metrics.precision
    println("Precision = " + precision)

    //保存模型
    val ModelPath = "data/logistic_regression_model"
    model.save(sc, ModelPath)
    val sameModel = LogisticRegressionModel.load(sc, ModelPath)

  }

}