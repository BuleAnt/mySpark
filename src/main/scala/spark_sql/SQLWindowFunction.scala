package spark_sql

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
	* GroupTopN in Spark SQL Window Function:
	* row_number() OVER (PARTITION BY name ORDER BY score DESC
	*/
object SQLWindowFunction {
	def main(args: Array[String]): Unit = {

		val conf = new SparkConf()
		conf.setAppName("SparkSQLWindowFuntionOps")
		conf.setMaster("spark://hadoop:7077")

		val sc = new SparkContext(conf)
		val hiveContext = new HiveContext(sc)

		hiveContext.sql("use spark") //使用名称为spark的数据库，我们接下来所有的表的操作都位于这个库中

		//如果要创建的表存在的话就删除，然后创建我们要导入数据的表
		hiveContext.sql("DROP TABLE IF EXISTS scores")
		hiveContext.sql("CREATE TABLE IF NOT EXISTS scores(name STRING,score INT) "
			+"ROW FORMAT DELIMITED FIELDS TERMINATED BY ' ' LINES TERMINATED BY '\\n'")
		//把要处理的出数据导入到Hive的表中
		hiveContext.sql("LOAD DATA LOCAL INPATH '/home/hadoop/Documents/workspaces/IdeaProjects/mySpark/src/main/resources/groupTop.txt' INTO TABLE scores")

		/**
			* 使用子查询的方式完成目标数据的提取，在目标数据内幕使用窗口函数row_number来进行分组排序：
			* PARTITION BY :指定窗口函数分组的Key；
			* ORDER BY：分组后进行排序；
			*/
		val result = hiveContext.sql("SELECT name,score "
			+ "FROM ("
			+ "SELECT "
			+ "name,score,"
			//row_number 窗口分析函数
			+ "row_number() OVER (PARTITION BY name ORDER BY score DESC) rank"
			+" FROM scores "
			+ ") sub_scores "
			+ "WHERE rank <=4")
		result.show(); //在Driver的控制台上打印出结果内容

		//把数据保存在Hive数据仓库中
		hiveContext.sql("DROP TABLE IF EXISTS sortedResultScores")
		result.saveAsTable("sortedResultScores")
	}
}
