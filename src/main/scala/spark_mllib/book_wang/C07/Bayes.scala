package spark_mllib.book_wang.C07

import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * 7-7 MLlib 朴素贝叶斯
  * 需要说明的是,训练模型主要有三个变量,labels是标签类别,
  * pi存储各个label的先验概率,theta是各个类别中的条件概率
  * 而预测部分代码为:
  * val test = Vectors.dense(0,0,10)
  * val result = model.predict(test)
  */
object Bayes {
  def main(args: Array[String]) {

    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("Bayes ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val data = MLUtils.loadLabeledPoints(sc, "data/bayes.txt")
    //读取数据集
    val model = NaiveBayes.train(data, 1.0) //训练贝叶斯模型
    model.labels.foreach(println) //打印label值
    model.pi.foreach(println) //打印先验概率
  }
}
