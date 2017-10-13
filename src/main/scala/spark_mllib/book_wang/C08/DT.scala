package spark_mllib.book_wang.C08

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.util.MLUtils

/**
  * 8-1 决策树
  * DTree.txt数据说明:
  * 第一列数据表示是否出去玩,后面若干个键值对分标表示对应的值
  * 这里的key值表示属性的序号,这样做的目的是防止有确性的出现.
  * 而vale是序号对应的具体值
  * trainClassifier参数说明:
  * input: RDD[LabeledPoint], 输入的数据集
  * numClasses: Int, 分类的数量,本利中只有出去和不出去,所以分为2类
  * categoricalFeaturesInfo: Map[Int, Int], 属性对的格式,这里只是淡出的键值对
  * impurity: String, 计算信息增益的形式
  * maxDepth: Int, 树的高度
  * maxBins: Int): 能够分裂的数据集合的数量
  */
object DT {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("DT")
    val sc = new SparkContext(conf)
    val data = MLUtils.loadLibSVMFile(sc, "data/DTree.txt")
    //设定分类数量
    val numClasses = 2
    //设定输入格式
    val categoricalFeaturesInfo = Map[Int, Int]()
    //设定信息增益计算方式
    val impurity = "entropy"
    //设定树高度
    val maxDepth = 5
    //设定分裂数据集
    val maxBins = 3
    val model = DecisionTree.trainClassifier(data, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins) //建立模型
    println(model.topNode) //打印决策树信息

  }
}
