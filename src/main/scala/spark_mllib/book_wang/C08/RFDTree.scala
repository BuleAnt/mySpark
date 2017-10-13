package spark_mllib.book_wang.C08

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.util.MLUtils

/**
  * 8-2 随机森林
  * 随机森林是若干个数的集合,当随机森林在运行的时候,每当一个新的数据被传输到系统中,
  * 则会有随机森林中的每一棵决策树进行处理.如果结果是一个连续的常熟,
  * 则对每一颗的结果取得平均值作为结果.
  * 如果是非连续结果,则选择所有决策树计算最多的一项作为数据计算的结果.
  */
object RFDTree {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("DT2")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val data = MLUtils.loadLibSVMFile(sc, "data/DTree.txt") //输入数据集

    val numClasses = 2
    //设定分类的数量
    val categoricalFeaturesInfo = Map[Int, Int]()
    //设置输入数据格式
    val numTrees = 3
    //设置随机雨林中决策树的数目
    val featureSubsetStrategy = "auto"
    //设置属性在节点计算数
    val impurity = "entropy"
    //设定信息增益计算方式
    val maxDepth = 5
    //设定树高度
    val maxBins = 3 //设定分裂数据集

    val model = RandomForest.trainClassifier(data, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins) //建立模型

    model.trees.foreach(println) //打印每棵树的相信信息
  }
}
