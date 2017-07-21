package spark_sql

import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * join直接通过DataFrame
  */
object DFJoin {

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

object DF2RDDJoin {

  case class Person(name: String, score: Int, age: Int)

  def main(args: Array[String]) {

    val sc = new SparkContext(new SparkConf().setAppName("DF2RDDJoin").setMaster("local"))
    val sqlContext = new SQLContext(sc)

    //read(.json)-->DataFrame-->register-->sql-->DF
    //-->.rdd()-->rowRDD-->map(row(0).collect-->Array[Any]
    val peopleScoreDF = sqlContext.read.format("json").load("src/test/resources/my_peoples.json")
    peopleScoreDF.registerTempTable("personScores")
    val scoresDF = sqlContext.sql("select name,score from personScores where score > 90")
    val scoresArray = scoresDF.rdd.map(row => row(0)).collect()

    //jsonArr-->JsonRDD-->read.json(JsonRDD)-->DF
    val peopleInfo = Array("{\"name\":\"Michael\",\"age\":20}", "{\"name\":\"Andy\",\"age\":17}", "{\"name\":\"Justin\",\"age\":19}")
    val peopleInfoRDD = sc.parallelize(peopleInfo)
    val peopleInfoDF = sqlContext.read.json(peopleInfoRDD)
    peopleInfoDF.registerTempTable("peopleInfo")

    //foreach Array-->sqlStr
    var sqlStr = "select name,age from peopleInfo where name in ("
    for (i <- 0 to scoresArray.length - 2) {
      sqlStr += ("'" + scoresArray(i) + "',")
    }
    sqlStr += "'" + scoresArray(scoresArray.length - 1) + "')"
    println(sqlStr)
    val nameAgeDF = sqlContext.sql(sqlStr)

    val nameScoresRDD = scoresDF.rdd.map(row => (row.getAs[String]("name"), row.getAs[Long]("score")))
    val nameAgeRDD = nameAgeDF.rdd.map(row => (row.getAs[String]("name"), row.getAs[Long]("age")))

    //rdd join
    val resultRDD = nameAgeRDD.join(nameScoresRDD)
    //rdd to DataFrame
    import sqlContext.implicits._
    //value部分顺序与join先后对应,这里为:[name,(age,score)]
    val resultDF = resultRDD.map(row => Person(row._1, row._2._1.asInstanceOf[Int], row._2._2.asInstanceOf[Int])).toDF

    resultDF.show
  }

}
