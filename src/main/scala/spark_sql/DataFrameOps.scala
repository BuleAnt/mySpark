package spark_sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
	* 使用scala编写DataFrame
	*/
object DataFrameOps {
	def main(args: Array[String]) {
		val conf = new SparkConf()

		conf.setAppName("DataFrameOpsScala").setMaster("local")
		//conf.setMaster("spark://hadoop:7077")
		val sc = new SparkContext(conf)
		// sc.setLogLevel("WARN")
		val sqlContext = new SQLContext(sc)

		val df = sqlContext.read.json("/home/hadoop/Documents/workspaces/IdeaProjects/mySpark/src/main/resources/people.json")

		df.show()

		df.select("name").show()

		df.select(df("name"), df("age") + 10)

		df.filter(df("age") > 10).show()

		df.groupBy("age").count.show()
	}
}
