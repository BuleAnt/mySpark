package spark_mllib.book_wang.C13

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.util.MLUtils

/**
  * 13-13 决策树算法--决定数据集的归类
  * 决策树是一种常用的挖掘方法,它用来研究特征数据的"信息熵"大小,
  * 从而确定在数据决策过程中那些数据起决定作用
  * 首先是对数据进行处理,决策树的数据处理需要标注数据的类别,如:
  * 1 1:4.7 2:3.2 3:1.3 4:0.2
  * 逗号前的数字是数据所属类别,冒号前的数字是第几个特征数据,冒号后的数字是特征数值
  */
object irisDecisionTree {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("irisDecisionTree ")
    //设定名称
    val sc = new SparkContext(conf)
    //输入数据集 构建一个LabeledPoint的RDD
    // val data = MLUtils.loadLibSVMFile(sc, "data/iris.txt")
    // 自己实现
    val data = sc.textFile("data/iris.txt").map(_.trim)
      .filter(line => !(line.isEmpty || line.startsWith("#")))
      .map { line =>
        val items = line.split('\t')
        val label = (items(items.length - 1) match {
          case "Iris-setosa" => 0
          case "Iris-versicolor" => 1
          case "Iris-virginica" => 2
        }).toDouble
        val features = Vectors.dense(items.slice(0, items.length - 1).map(_.toDouble))
        LabeledPoint(label, features)
      }

    //设定分类数量
    val numClasses = 3
    //设定输入格式
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "entropy"
    //设定信息增益计算方式
    val maxDepth = 5
    //设定树高度
    val maxBins = 3
    //设定分裂数据集
    val model = DecisionTree.trainClassifier(data, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)
    //建立模型
    val test = Vectors.dense(Array(7.2, 3.6, 6.1, 2.5))
    println("预测结果是:" + model.predict(test))
  }
}
