package rdd

import java.sql.{DriverManager, ResultSet}

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-8-24.
  */
object JdbcRDD {

  def createConnection() = {
    Class.forName("com.mysql.jdbc.Driver").newInstance()
    DriverManager.getConnection("jdbc:mysql://localhost/test?user=holden")
  }

  def extractValues(r: ResultSet) = {
    (r.getInt(1), r.getInt(2))
  }

  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local").setAppName("TestJson")
    val sc = new SparkContext(conf)
    // val data = new JdbcRDD(sc, createConnection(), "SELECT * FROM panda WHERE ? <= id AND id <+ ?",
    // lowerBound = 1, upperBound = 3, numPartitions = 2, mapRow = extractValues)
    // println(data.collect().toList)
  }

}
