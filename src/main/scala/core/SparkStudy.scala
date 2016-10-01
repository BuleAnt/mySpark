package core

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-6-30.
  */
class SparkStudy {
  //val conf = SparkContext.setAppName(appName).setMaster(master)
  //val sc = new SparkContext(conf)
  //val rdd = sc.textFile("/").flatMap(_.split(" ")).map(_ =>(_,1)).reduceByKey(_ + _)

}

object SparkStudy {
  def main(args: Array[String]) {
    val logFile = "hdfs://hadoop:9000/user/spark/wc/input/data"
    val conf = new SparkConf()
      .setAppName("Simple Application")
      .setMaster("local")
    //.setMaster("spark://hadoop:7077")
    //.setJars(jars)
    //val sc = new SparkContext(conf)
    val sc = new SparkContext
    val logData = sc.textFile(logFile)
    val tempRdd = logData.filter(line => line.contains("100"))
    val numAs = tempRdd.count()
    //val numBs = logData.filter(line => line.contains("A")).count()
    println("lines with a: %s ".format(numAs))
    sc.stop()
  }
}
