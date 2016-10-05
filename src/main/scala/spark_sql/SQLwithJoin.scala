package spark_sql

import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
	* join直接通过DataFrame
	*/
object SparkSQLwithJoinScala {

	def main(args: Array[String]): Unit = {
		val conf = new SparkConf()
			.setAppName("SparkSQLwithJoinScala")
			.setMaster("local")

		val sc = new SparkContext(conf)
		val sqlContext = new SQLContext(sc)

		val srcDF = sqlContext.read.format("json").load("hdfs://Master:9000/sparksql/people.json")

		val peopleJson = Array("{\"name\":\"Michael\",\"age\":20}",
			"{\"name\":\"Andy\",\"age\":17}",
			"{\"name\":\"Justin\",\"age\":19}")
		val joinRDD = sc.parallelize(peopleJson)
		val destDF = sqlContext.read.json(joinRDD)

		secondJoin(srcDF, destDF)
	}

	def secondJoin(sourceDF: DataFrame, destDF: DataFrame): Unit = {
		sourceDF.registerTempTable("people")
		destDF.registerTempTable("joinPeople")
		// 直接对DataFrame进行join
		val result = sourceDF.filter(sourceDF("score") > 90).join(destDF, sourceDF("name") === destDF("name"), "inner")
		val rdd = result.map(row => row.getAs[String]("name") + ": " + row.getAs[String]("age") + ": " + row.getAs[String]("score"))
		rdd.foreach(println)
	}

}

//test
object JoinShellDemo {
	def main(args: Array[String]) {

		val sc = new SparkContext(new SparkConf().setAppName("SparkSQLwithJoinScala").setMaster("local"))
		val sqlContext = new SQLContext(sc)

		val personScoresDF = sqlContext.read.format("json").load("src/main/resources/my_people.json")
		personScoresDF.registerTempTable("personScores")
		val execellentStudentDF = sqlContext.sql("select name,score from personScores where score > 90")
		val execellentStudents = execellentStudentDF.rdd.map(row => row(0)).collect()

		val peopleInformations = Array("{\"name\":\"Michael\",\"age\":20}", "{\"name\":\"Andy\",\"age\":17}", "{\"name\":\"Justin\",\"age\":19}")
		val peopleInformationsRDD = sc.parallelize(peopleInformations)
		val peopleInformationsDF = sqlContext.read.json(peopleInformationsRDD)
		peopleInformationsDF.registerTempTable("peopleInfo")

		var sqlText = "select name,age from peopleInfo where name in ("
		for (i <- 0 until execellentStudents.length) {
			sqlText += ("'" + execellentStudents(i) + "'"); if (i < execellentStudents.length - 1) sqlText += ","
		}
		sqlText += ")"
		val execellentNameAgeDF = sqlContext.sql(sqlText)

		val nameScoreRDD = execellentStudentDF.rdd.map(row => (row.getAs("name"), row.getAs("score")))
		val nameAgeRDD = execellentNameAgeDF.rdd.map(row => (row.getAs("name"), row.getAs("age")))

		//val resultRDD = nameScoreRDD.join(nameAgeRDD)
		case class Person(id: Int, name: String)
		//resultRDD.map(x => JPerson(x._1, x._2.toString()))
		//TODO

	}

}
