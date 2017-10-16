package spark_mllib.book_wang.C13

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 13-10 使用Kmeans算法进行聚类分析
  * 可以看到,最终打印三组,每组数据4个值,可以组成一个数据中心,
  * 通过距离和数量的设置,即可形成一个数据分来结果
  */
object irisKmeans {
  def main(args: Array[String]) {

    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("irisKmeans ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val data = sc.textFile("data/iris.txt")
    //输入数据集
    val parsedData = data.map(s => Vectors.dense(s.split('\t').slice(0, 4).map(_.toDouble)))
      .cache() //数据处理

    val numClusters = 3
    //最大分类数
    val numIterations = 20
    //迭代次数
    val model = KMeans.train(parsedData, numClusters, numIterations) //训练模型
    model.clusterCenters.foreach(println) //打印中心点坐标

  }
}
