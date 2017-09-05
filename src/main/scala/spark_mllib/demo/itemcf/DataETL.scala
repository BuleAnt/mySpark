package spark_mllib.demo.itemcf

import java.net.URLDecoder
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by noah on 17-9-4.
  */
object DataETL {

  def decode(value: String): String = URLDecoder.decode(value, "UTF8")

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("ItemCF Demo")
    val sc = new SparkContext(conf)
    val data = sc.textFile("/home/noah/workspaces/spark/messag_2017-08-19_sec.log")
    val record = data.map(line => {
      val split = line.split("\"messag \" ")
      val msg = split(1).split(" ")(0).replace("\"", "").split("\\|\\|").map(decode)
      msg
    }).filter(msg => {
      !msg(3).isEmpty && !msg(3).equals("_") && !msg(5).isEmpty &&
        (msg(2).equals("广告") || msg(2).equals("导航"))
    }).map(msg => (msg(3) + "_" + msg(5), 1)).countByKey()
    record.foreach(println)
    //.foreach(msg => println(msg(3), msg(5)))
    /* val item_hot = record.filter(_ (4).isEmpty).map(msg => (msg(5) + "||" + msg(4), 1))
     val group = item_hot.countByKey().foreach(println)*/
  }
}
