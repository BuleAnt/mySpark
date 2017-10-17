package spark_mllib.book_wang.C13

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.tree.RandomForest

/**
  * 随机森林进行归类--决定数据集归类的分布式方法
  * 当数据量较大的时候,随机森林是一个能够充分利用分布式集群的决策树算法.
  */
object irisRFDTree {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("irisRFDTree")
    val sc = new SparkContext(conf)
    // val data = MLUtils.loadLibSVMFile(sc, "c://2LinearRegression.txt")
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
    //设定分类的数量
    val numClasses = 3
    //设置输入数据格式
    val categoricalFeaturesInfo = Map[Int, Int]()
    //设置随机雨林中决策树的数目
    val numTrees = 3
    //设置属性在节点计算数
    val featureSubsetStrategy = "auto"
    //设定信息增益计算方式
    val impurity = "entropy"
    //设定树高度
    val maxDepth = 5
    //设定分裂数据集
    val maxBins = 3
    //建立模型
    val model = RandomForest.trainClassifier(data, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    model.trees.foreach(println) //打印每棵树的相信信息
  }
}
