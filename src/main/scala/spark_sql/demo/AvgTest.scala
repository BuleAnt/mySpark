package spark_sql.demo

import org.apache.log4j.{Logger, Level}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.types.{StringType, DoubleType, StructField, StructType}

/**
  * @author Administrator
  */
object AvgTest {
	def main(args: Array[String]): Unit = {
		Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
		Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.ERROR)
		val conf = new SparkConf().setAppName("UDAF TEST").setMaster("local")

		val sc = new SparkContext(conf)

		val sqlContext = new SQLContext(sc)


		val nums = Seq(("a", 1.1), ("a", 2.1), ("b", 1.1))
		val numsRDD = sc.parallelize(nums, 2)

		val numsRowRDD = numsRDD.map { x => Row(x._1, x._2) }

		val schema = new StructType().add(StructField("id", StringType, nullable = false)).add(StructField("num", DoubleType, nullable = true))

		val numsDF = sqlContext.createDataFrame(numsRowRDD, schema)

		numsDF.registerTempTable("mytable")
		sqlContext.sql("select id,avg(num) from mytable  group by id").collect().foreach { x => println(s"id:${x(0)},avg:${x(1)}") }
		sqlContext.udf.register("myAvg", new MyAvg)
		sqlContext.sql("select id,myAvg(num) from mytable group by id ").collect().foreach { x => println(s"id:${x(0)},avg:${x(1)}") }

		sc.stop()
	}
}
