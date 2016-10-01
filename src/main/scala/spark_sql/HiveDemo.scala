package spark_sql

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-8-24.
  */
object HiveDemo {
  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("HiveDemo").setMaster("local"))
    val hiveCtx = new HiveContext(sc)
    //val rows = hiveCtx.sql("SELECT name,age FROM users")
    //val firstRow = rows.first()
    //println(firstRow.getString(0))

  val tweets = hiveCtx.jsonFile("/home/hadoop/test/spark/input/tweets.json")
    //tweets.registerTempTable("tweets")
    //val results= hiveCtx.sql("SELECT user.name,text FROM tweets")
    ///results.foreach(println)
    tweets.printSchema()
  }
}
