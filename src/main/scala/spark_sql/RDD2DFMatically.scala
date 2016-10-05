package spark_sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructType, StructField, StringType}

/**
	* sqlContext.createDataFrame的方式
	* rdd-->map(Row)-->row RDD
	* -->sqlContext.createDataFrame(代码构建StructType,rowRDD)-->DataFrame
	*/
object RDD2DFMatically {

	def main(args: Array[String]): Unit = {
		val conf = new SparkConf()
		conf.setAppName("RDD2DFMatically")
		conf.setMaster("local")
		val sc = new SparkContext(conf)
		val sqlContext = new SQLContext(sc)
		val strRdd = sc.textFile("src/main/resources/persons.txt")
		//val schema = "id name age".split(" ")

		//在StructType.apply()的参数为StructField数组,这里通过map对List循环操作,将其元素转换为StructField
		val schema = StructType(List("id", "name", "age").map(fieldName => StructField(fieldName, StringType)))
		//id,name,age
		val rowRDD = strRdd.map(_.split(",")).map(p => Row(p(0), p(1).trim, p(2)))

		val peopleDataFrame = sqlContext.createDataFrame(rowRDD, schema)
		peopleDataFrame.registerTempTable("people")
		var results = sqlContext.sql("select name from people")
		results.map(t => "Name: " + t(0)).collect().foreach(println)

		results = sqlContext.sql("select * from people")
		results.map(_.getValuesMap(List("id", "name", "age"))).collect().foreach(println)
	}
}
