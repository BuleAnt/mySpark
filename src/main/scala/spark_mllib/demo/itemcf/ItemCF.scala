package spark_mllib.demo.itemcf

import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 实现Item-based协同过滤算法
  * Created by noah on 17-9-4.
  */
object ItemCF {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("ItemCF Demo")
    val sc = new SparkContext(conf)
    val data = sc.textFile("/home/noah/workspaces/spark/messag_2017-08-19_sec.log")
    val parsedData = data.map(_.split(" ") match {
      case Array(user, item, rate) => MatrixEntry(user.toLong - 1, item.toLong - 1, rate.toDouble)
    })

    val ratings = new CoordinateMatrix(parsedData)
    // 计算Item相似度
    val similarities = ratings.toRowMatrix().columnSimilarities(0.1)
    // 计算项目1的平均评分
    val ratingsOfItem1 = ratings.transpose().toRowMatrix().rows.collect()(0).toArray
    val avgRatingOfItem1 = ratingsOfItem1.sum / ratingsOfItem1.length
    //计算用户对其他项目的加权平均评分
    val ratingsOfUser1 = ratings.toRowMatrix().rows.collect()(0).toArray.drop(0)
    val weights = similarities.entries.filter(_.i == 0).sortBy(_.j).map(_.value).collect()
    val weightedR = (0 to 2).map(t => weights(t) * ratingsOfUser1(t)).sum / weights.sum
    println("Rating of User 1 toword item 1 is :" + (avgRatingOfItem1 + weightedR))
  }

}
