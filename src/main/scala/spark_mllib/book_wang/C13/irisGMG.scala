package spark_mllib.book_wang.C13

import org.apache.spark.mllib.clustering.GaussianMixture
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 13-11 使用高斯聚类器对数据进行聚类
  */
object irisGMG {
  def main(args: Array[String]) {

    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("irisGMG")
    val sc = new SparkContext(conf)
    val data = sc.textFile("data/iris.txt")
    val parsedData = data.map(s => Vectors.dense(
      s.trim.split('\t').slice(0, 4).map(_.toDouble)))//转化数据格式
      .cache()
    val model = new GaussianMixture().setK(2).run(parsedData) //训练模型

    for (i <- 0 until model.k) {
      println("weight=%f\nmu=%s\nsigma=\n%s\n" format //逐个打印单个模型
        (model.weights(i), model.gaussians(i).mu, model.gaussians(i).sigma)) //打印结果
    }
  }
}
