package spark_sql

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
	* SQL2Hive
	*/
class SQL2Hive {
}
object SQL2Hive {
	def main(args: Array[String]): Unit = {
		val conf = new SparkConf()
		conf.setAppName("SparkSQL2Hive") //设置应用程序的名称，在程序运行的监控界面可以看到名称
		conf.setMaster("spark://hadoop:7077")
		//此Demo只能在spark-shell或集群环境spark-submit运行
		val sc = new SparkContext(conf)
		val hiveContext = new HiveContext(sc)

		hiveContext.sql("CREATE DATABASE IF NOT EXISTS spark")
		hiveContext.sql("use spark") //前面创建好了这个数据库
		//第一张学生姓名年龄表stu_age
		hiveContext.sql("DROP TABLE IF EXISTS stu_age") //删除同名的Table
		hiveContext.sql("CREATE TABLE IF NOT EXISTS stu_age(name STRING,age INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\t' LINES TERMINATED BY '\\n'") //创建自定义的表
		//把本地数据加载到Hive数据仓库中，发生了数据的拷贝。也可以通过LOAD DATA INPATH去获得HDFS等上面的数据到Hive，发生了数据的移动。
		hiveContext.sql("LOAD DATA LOCAL INPATH '/home/hadoop/Documents/workspaces/IdeaProjects/mySpark/src/main/resources/stu_age.txt' INTO TABLE stu_age") //导入数据
		//第二张学生姓名成绩表stu_score
		hiveContext.sql("DROP TABLE IF EXISTS stu_score")
		hiveContext.sql("CREATE TABLE IF NOT EXISTS stu_score(name STRING,score INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\t' LINES TERMINATED BY '\\n'")
		hiveContext.sql("LOAD DATA LOCAL INPATH '/home/hadoop/Documents/workspaces/IdeaProjects/mySpark/src/main/resources/stu_score.txt' INTO TABLE stu_score")

		//join
		val stuDF = hiveContext.sql("SELECT a.name,a.age,s.score FROM spark.stu_age a JOIN spark.stu_score s ON a.name=s.name where s.score > 90")

		//当删除该表时，数据也会一起被删除（磁盘上的数据不再存在）
		hiveContext.sql("DROP TABLE IF EXISTS students") //删除同名的Table
		stuDF.saveAsTable("students")

		//使用HivewContext的Table方法可以直接去读Hive中的Table并生成DaraFrame
		//读取的数据就可以进行机器学习、图计算、各种复杂ETL等操作
		val dataFromHive = hiveContext.table("students")
		dataFromHive.show()
	}
}
