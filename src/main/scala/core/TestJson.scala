package core

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-8-24.
  */
object TestJson {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local").setAppName("TestJson")
    val sc = new SparkContext(conf)

   /* case class Person(name: String, lovesPandas: Boolean)
    val input =sc.textFile("file.json")
    val mapper = new ObjectMapper()
    val result = input.flatMap(record=>{
      try{
        Some(mapper.readValue(record,classOf[Person]))
      }catch {
        case e :Exception => None
      }
    })
    result.filter(p=>p.lovesPandas).map(mapper.writeValueAsString(_)).saveAsTextFile("/xx" )*/

/*    val path = "$SPARK_HOME/examples/src/main/resources/people.json"
    Source.fromFile(path).foreach(print)
    //获取到一个SchemaRDD
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    val jsonFile = sqlContext.jsonFile(path)
    jsonFile.printSchema()
   //SchemaRDD，就可以采用SQL
    jsonFile.registerTempTable("people")
    val teenagers = sqlContext.sql("SELECT name FROM people WHERE age >= 13 AND age <= 19")
    teenagers.foreach(println)*/
  }





}
