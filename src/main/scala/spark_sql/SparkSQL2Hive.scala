package spark_sql

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
	* Spark SQL操作Hive
	*/
object SparkSQL2Hive {
	def main(args: Array[String]): Unit = {
		val conf = new SparkConf()
		conf.setAppName("SparkSQL2Hive") //设置应用程序的名称，在程序运行的监控界面可以看到名称
		conf.setMaster("spark://Master:7077")
		val sc = new SparkContext(conf)


		val hiveContext = new HiveContext(sc)
		hiveContext.sql("use hive") //前面创建好了这个数据库
		//第一张学生姓名年龄表
		hiveContext.sql("DROP TABLE IF EXISTS people") //删除同名的Table
		hiveContext.sql("CREATE TABLE IF NOT EXISTS people(name STRING,age INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\t' LINES TERMINATED BY '\\n'") //创建自定义的表
		//把本地数据加载到Hive数据仓库中，发生了数据的拷贝。也可以通过LOAD DATA INPATH去获得HDFS等上面的数据到Hive，发生了数据的移动。
		hiveContext.sql("LOAD DATA LOCAL INPATH 'examples/src/main/resources/people.txt' INTO TABLE people") //导入数据

		//第二张学生姓名成绩表
		hiveContext.sql("DROP TABLE IF EXISTS peoplescores")
		hiveContext.sql("CREATE TABLE IF NOT EXISTS peoplescores(name STRING,score INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\t' LINES TERMINATED BY '\\n'")
		hiveContext.sql("LOAD DATA LOCAL INPATH 'examples/src/main/resources/peoplescores.txt' INTO TABLE peoplescores")


		val resultDF = hiveContext.sql("SELECT pi.name,pi.age,ps.score FROM people pi JOIN peoplescores ps ON pi.name=ps.name where ps.score > 90")


		hiveContext.sql("DROP TABLE IF EXISTS peopleinformationresult") //删除同名的Table
		resultDF.saveAsTable("peopleinformationresult")

		val dataFromHive = hiveContext.table("peopleinformationresult")
		dataFromHive.show()
	}
}
