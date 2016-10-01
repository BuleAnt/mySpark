package spark_sql

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-8-24.
  */
object TestParquet {
  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("TestParquet").setMaster("local"))
    val hiveCtx = new HiveContext(sc)
    val users = hiveCtx.parquetFile("/opt/single/spark-compiled/examples/src/main/resources/users.parquet")
    users.show()
    users.printSchema()
  }

}
