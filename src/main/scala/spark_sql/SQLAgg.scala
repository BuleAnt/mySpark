package spark_sql

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.functions._

/**
	* SQLAgg
	*/
object SQLAgg {

	def main(args: Array[String]) {

		val conf = new SparkConf()
		conf.setAppName("SparkSQLInnerFunctions")
		// conf.setMaster("spark://Master:7077")
		conf.setMaster("local")

		val sc = new SparkContext(conf)
		val sqlContext = new SQLContext(sc) //构建SQL上下文

		//要使用Spark SQL的内置函数，就一定要导入SQLContext下的隐式转换
		import sqlContext.implicits._

		// 模拟电商访问的数据，实际情况会比模拟数据复杂很多，最后生成RDD
		val userData = Array(
			"2016-3-27,001,http://spark.apache.org/,1000",
			"2016-3-27,001,http://Hadoop.apache.org/,1001",
			"2016-3-27,002,http://fink.apache.org/,1002",
			"2016-3-28,003,http://kafka.apache.org/,1020",
			"2016-3-28,004,http://spark.apache.org/,1010",
			"2016-3-28,002,http://hive.apache.org/,1200",
			"2016-3-28,001,http://parquet.apache.org/,1500",
			"2016-3-28,001,http://spark.apache.org/,1800")
		val userDataRDD = sc.parallelize(userData) //生成DD分布式集合对象

		//RDD-->rowRDD-->DataFrame
		val userDataRDDRow = userDataRDD.map(row => {
			val splited = row.split(",")
			Row(splited(0), splited(1).toInt, splited(2), splited(3).toInt)
		})
		val structTypes = StructType(Array(
			StructField("time", StringType, true),
			StructField("id", IntegerType, true),
			StructField("url", StringType, true),
			StructField("amount", IntegerType, true)
		))
		val userDataDF = sqlContext.createDataFrame(userDataRDDRow, structTypes)

		// 使用Spark SQL提供的内置函数对DataFrame进行操作，
		// 特别注意：内置函数生成Column对象,且自动进行CG(code generate)
		userDataDF.groupBy("time").agg('time, countDistinct('id))
			.map(row => Row(row(1), row(2))).collect.foreach(println)

		userDataDF.groupBy("time").agg('time, sum('amount)).show()
	}

}
