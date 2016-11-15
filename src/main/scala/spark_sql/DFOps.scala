package spark_sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 使用scala编写DataFrame
  */
object DFOps {
	def main(args: Array[String]) {
		val conf = new SparkConf()

		conf.setAppName("DataFrameOpsScala").setMaster("local")
		//conf.setMaster("spark://hadoop:7077")
		val sc = new SparkContext(conf)
		// sc.setLogLevel("WARN")
		val sqlContext = new SQLContext(sc)

		val df = sqlContext.read.json("src/test/cache/HOT_TOP_LIST_0_v21.json")

		df.registerTempTable("temp")
		//sqlContext.sql("select * from temp").collect.foreach(println)
		//df.show()
		//df.describe("BATCH_RES")
		//df.select("BATCH_RES.DATA").show()
		//df.select("name").show()

		//df.select(df("name"), df("age") + 10)

		//df.filter(df("age") > 10).show()

		//df.groupBy("age").count.show()
	}
}
