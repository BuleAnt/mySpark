package spark_mllib.book_wang.C09

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 9-1 Kmeans 算法
  * train参数说明:
  * data: RDD[Vector], 输入的数据集;
  * k: Int, 聚类分成的数据集数;
  * maxIterations: Int, 最大迭代次数
  *  model.clusterCenters.foreach(println) 打印训练后模型的中心点
  */
object Kmeans {
  def main(args: Array[String]) {

    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("Kmeans ")
    val sc = new SparkContext(conf)

    val data = sc.textFile("data/Kmeans.txt")
    val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble)))
      .cache() //数据处理

    val numClusters = 2
    //最大分类数
    val numIterations = 20
    //迭代次数
    val model = KMeans.train(parsedData, numClusters, numIterations) //训练模型
    model.clusterCenters.foreach(println) //打印中心点坐标

  }
}
