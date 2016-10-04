package core

import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.util.Bytes
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
    val top5 = sortedPaired.map(item => (item._2, item._1)) take (5)
    top5.foreach(println)
  }

  def topNline(): Unit = {
    val sc = new SparkContext(new SparkConf().setAppName("TopN").setMaster("local"))
    val lines = sc.textFile("file:///home/hadoop/test/data/topN.dat")
    val kv = lines.map(line => (line.toInt, line)) //生成Key-Value便于排序
  }

  def topKseveOnHBase(): Unit = {
    val conf = new SparkConf().setAppName("SparkOnHBase").setMaster("local")
    val sc = new SparkContext(conf)

    val textFile = sc.textFile("file:/home/hadoop/test/data")
    val counts = textFile.flatMap(line => line.split("\t"))
      .map(word => (word, 1))
      .reduceByKey(_ + _)
      .map(x => (x._2, x._1))
      .sortByKey(false)
      .map(x => (x._2, x._1))

    val tableName = "test"
    val myConf = HBaseConfiguration.create()
    //myConf.set("hbase.zookeeper.quorum", "hadoop,hadoop1,hadoop2")
    //myConf.set("hbase.zookeeper.property.clientPort","2181")
    //myConf.set("hbase.defaults.for.version.skip","true")
    //myConf.set()
    myConf.set("hbase.rootdir", "file:///opt/modules/hbase-1.2.1/root")
    val conn = ConnectionFactory.createConnection(myConf)
    counts.foreachPartition(x => {
      val myTable = conn.getTable(TableName.valueOf(tableName))
      x.foreach(y => {
        val p = new Put(Bytes.toBytes(y._1))
        p.addColumn(Bytes.toBytes("col"), Bytes.toBytes("key"), Bytes.toBytes(y._2))
        myTable.put(p)
      })
      myTable.close()
    })
  }
}
