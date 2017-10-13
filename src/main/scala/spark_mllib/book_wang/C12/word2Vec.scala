package spark_mllib.book_wang.C12

import org.apache.spark.mllib.feature.Word2Vec
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 12-2 词量化
  *
  */
object word2Vec {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("word2Vec ")

    val sc = new SparkContext(conf)
    val documents = sc.textFile("data/12.txt").map(_.split(" ").toSeq)
    //创建词向量实例
    val word2vec = new Word2Vec()
    val model = word2vec.fit(documents) //训练模型
    println(model.getVectors)
    //打印向量模型
    val synonyms = model.findSynonyms("spar", 2) //寻找spar的相似词
    for (synonym <- synonyms) {
      //打印找到的内容
      println(synonym)
    }
  }
}


