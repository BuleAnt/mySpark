package spark_mllib.book_wang.C13

import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * 13-12 使用贝叶斯分类器分析对数据集进行分类处理
  */
object irisBayes {
  def main(args: Array[String]) {

    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("irisBayes")
    val sc = new SparkContext(conf)
    //读取数据集
    //val data = MLUtils.loadLabeledPoints(sc, "data/iris.txt")
    val data = sc.textFile("data/iris.txt").map {
      line =>
        val parts = line.split('\t')
        val label = (parts(parts.length - 1) match {
          case "Iris-setosa" => 0
          case "Iris-versicolor" => 1
          case "Iris-virginica" => 2
        }).toDouble
        val features = Vectors.dense(parts.slice(0, parts.length - 1).map(_.toDouble))
        LabeledPoint(label, features)
    }
    //训练贝叶斯模型
    val model = NaiveBayes.train(data, 1.0)
    //创建待测定数据
    val test = Vectors.dense(7.3, 2.9, 6.3, 1.8)
    //“测试数据归属在类别:” +
    val result = model.predict(test)
    print(result) //打印结果
  }
}
