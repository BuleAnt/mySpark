package spark_mllib.book_wang.C09

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.PowerIterationClustering
import org.apache.spark.rdd.RDD

/**
  * 9-3 快速迭代聚类
  * 快速迭代聚类是谱聚类的一种,谱聚类是最近聚类研究的一个热点,是建立在图论理论上的一种新聚类方法
  * 快速迭代聚类的基本原理是使用含有权重的无向线将样本数据链接在一张无向图中,
  * 之后按相似度进行划分,是的划分后的子图内部具有最大的相似度而不同子图具有最小县四度从而达到聚类的效果.
  */
object PIC {
  def main(args: Array[String]) {

    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("PIC ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val data = sc.textFile("c://u2.txt")
    //读取数据
    val pic = new PowerIterationClustering() //创建专用类
      .setK(3) //设定聚类数
      .setMaxIterations(20)
    //设置迭代次数
    val model = pic.run(data.asInstanceOf[RDD[(Long, Long, Double)]]) //创建模型
  }
}
