package spark_streaming

import java.sql.{Connection, DriverManager}

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 使用foreachRDD操作
  * 内部foreachPartition通过连接池操作record
  */
object ForeachRDD2DB {
	def main(args: Array[String]) {
		val conf = new SparkConf().setAppName("OnlineForeachRDD2DB").setMaster("local[2]")
		val ssc = new StreamingContext(conf, Seconds(5))

		val lines = ssc.socketTextStream("hadoop", 9999)
		val words = lines.flatMap(_.split(" "))
		val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)

		// 注意:这里执行一次就结束
		wordCounts.foreachRDD { rdd =>
			rdd.foreachPartition { partitionOfRecords => {
				//连接池,直接调用java的代码
				val connection = ConnectionPool.getConnection()
				partitionOfRecords.foreach(record => {
					val sql = "insert into item_count(item,count) values('" + record._1 + "'," + record._2 + ")"
					val stmt = connection.createStatement
					println(sql)
					stmt.executeUpdate(sql)
				})
				ConnectionPool.returnConnection(connection)
			}
			}
		}
		ssc.start()
		ssc.awaitTermination()
	}
}


object JDBCConnect {
	def main(args: Array[String]) {
		// connect to the database named "mysql" on the localhost
		val driver = "com.mysql.jdbc.Driver"
		val url = "jdbc:mysql://localhost/mysql"
		val username = "root"
		val password = "root"

		// there's probably a better way to do this
		var connection: Connection = null

		try {
			// make the connection
			Class.forName(driver)
			connection = DriverManager.getConnection(url, username, password)

			// create the statement, and run the select query
			val statement = connection.createStatement()
			val resultSet = statement.executeQuery("SELECT host, user FROM user")
			while (resultSet.next()) {
				val host = resultSet.getString("host")
				val user = resultSet.getString("user")
				println("host, user = " + host + ", " + user)
			}
		} catch {
			case e: Throwable => e.printStackTrace()
		}
		connection.close()
	}
}
