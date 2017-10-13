package spark_mllib.book_wang.C12

import org.apache.spark.mllib.feature.{IDF, HashingTF}
import org.apache.spark.{SparkContext, SparkConf}

/**
  * 12-1 特征提取

  */
object TF_IDF {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("TF_IDF ")
    val sc = new SparkContext(conf)
    val documents = sc.textFile("data/12.txt").map(_.split(" ").toSeq)
    //首先创建TF计算实例
    val hashingTF = new HashingTF()
    //计算文档TF值
    val tf = hashingTF.transform(documents).cache()
    //创建IDF实例并计算
    val idf = new IDF().fit(tf)
    //计算TF_IDF词频
    val tf_idf = idf.transform(tf)
    tf_idf.foreach(println)

  }
}
