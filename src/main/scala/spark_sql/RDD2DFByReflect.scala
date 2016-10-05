package spark_sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
	* Created by hadoop on 16-10-2.
	*/
object RDD2DFByReflect {

	case class Person(id: Int, name: String, age: Int)

	def main(args: Array[String]): Unit = {
		val conf = new SparkConf()
		conf.setAppName("RDD2DataFrameByProgrammaticallyScala").setMaster("local")
		val sc = new SparkContext(conf)
		val sqlContext = new SQLContext(sc)
		import sqlContext.implicits._


		val people = sc.textFile("src/main/resources/persons.txt").map(_.split(","))
			.map(p => Person(p(0).trim.toInt, p(1), p(2).trim.toInt)).toDF()

		people.registerTempTable("people")

		val selectDF = sqlContext.sql("select name,age from people where age >=6 and age <=19")

		selectDF.map(t => "Name:" + t(0)).collect().foreach(println)

		selectDF.map(t => "Name:" + t.getAs[String]("name")).collect().foreach(println)

		selectDF.map(_.getValuesMap[Any](List("name", "age"))).collect().foreach(println)

		sqlContext.sql("select * from people").map(_.getValuesMap(List("name", "age", "id"))).collect().foreach(println)

	}

}
