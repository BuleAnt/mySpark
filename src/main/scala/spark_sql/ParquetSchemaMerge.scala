package spark_sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
	* http://spark.apache.org/docs/1.6.1/sql-programming-guide.html#schema-merging
	*/
object ParquetSchemaMerge {
	def main(args: Array[String]) {
		val conf = new SparkConf()
		conf.setAppName("RDD2DataFrameByProgrammaticallyScala")
		conf.setMaster("local")
		val sc = new SparkContext(conf)
		val sqlContext = new SQLContext(sc)

		import sqlContext.implicits._
		val df1 = sc.makeRDD(1 to 5).map(i => (i, i * 2)).toDF("single", "double")
		df1.write.parquet("target/test_table/key=1")

		val df2 = sc.makeRDD(6 to 10).map(i => (i, i * 3)).toDF("single", "triple")
		df2.write.parquet("target/test_table/key=2")

		val df3 = sqlContext.read.option("mergeSchema", "true").parquet("target/test_table")
		df3.printSchema()
		df3.show()
	}
}
