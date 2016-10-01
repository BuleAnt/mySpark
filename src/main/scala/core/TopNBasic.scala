package core

import org.apache.spark.{SparkConf, SparkContext}

/**
  * 基础TopN
  * Created by hadoop on 16-7-11.
  */
object TopNBasic {
  def main(args: Array[String]) {

    val sc = new SparkContext(new SparkConf().setAppName("TopN").setMaster("local"))
    //    val lines = sc.textFile("file:///home/hadoop/test/data/topN.dat")
    //val pairs = lines.map(line => (line.toInt, line)) //生成Key-Value便于排序
    //    val sortedPaired = pairs.sortByKey(false)
    //    val sortedData = sortedPaired.map(pair => pair._2) //过滤出排序后的内容本身
    //    val top5 = sortedData.take(5) //获取排名前5的元素内容
    //    top5.foreach(println)

    val lines = sc.textFile("file:///home/hadoop/test/data/data2")
    val pairs = lines.map(line => (line.split("\t")(1).toInt, line.split("\t")(0).toString))
    val sortedPaired = pairs.sortByKey(false)
    val top5 = sortedPaired.map(item=>(item._2,item._1)) take (5)
    top5.foreach(println)
  }
}
