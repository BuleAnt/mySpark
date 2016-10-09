package spark_streaming

import org.apache.spark.SparkConf
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 使用Spark Streaming+Spark SQL来在线动态计算电商中不同类别中最热门的商品排名,
  * 例如收集类别下最热门的三种手机
  *
  * 技术实现:Spark Streaming+Spark SQL,
  * 之所以Spark Streaming能够使用ML,sql,Graphs等功能是应为有ForeachRDD和Transform等接口
  * 这些接口中其实是基于RDD进行操作,所以以RDD为基石,就可以使用Spark其他所有的功能,就像直接调用API一样简单
  *
  * 假设这里的数据格式:user item category,例如 Rocky Samsung Android
  */
object Top3ItemForEachCategory2DB {
	def main(args: Array[String]) {
		val conf = new SparkConf().setAppName("OnlineForeachRDD2DB").setMaster("spark://hadoop:7077")
		val ssc = new StreamingContext(conf, Seconds(5))
		ssc.checkpoint("/home/hadoop/Documents/workspaces/idea/mySpark/target/checkpoint")

		val userClickLogsDStream = ssc.socketTextStream("hadoop", 9999)

		// (user item category)-->map-->(category_item,1)
		val formattedUserClickLogDStream = userClickLogsDStream.map(clickLog =>
			(clickLog.split(" ")(2) + "_" + clickLog.split(" ")(1), 1))

		// (item_user,1)-->reduceByKeyAndWindow-->60s的窗口间隔,20秒的滑动窗口-->SparkSQL
		val categoryUserClickLogsDStream = formattedUserClickLogDStream.reduceByKeyAndWindow(
			_ + _, _ - _, Seconds(60), Seconds(20))

		// DStream.foreachRDD
		categoryUserClickLogsDStream.foreachRDD { rdd => {

			if (rdd.isEmpty()) {
				println("No data inputted!!!")
			} else {
				// rdd-->RowRDD-->DataFrame
				val categoryItemRow = rdd.map(reducedItem => {
					val category = reducedItem._1.split("_")(0)
					val item = reducedItem._1.split("_")(1)
					val click_count = reducedItem._2
					Row(category, item, click_count)
				})

				//构建 schema: StructType
				val structType = StructType(Array(
					StructField("category", StringType, boolean2Boolean(true)),
					StructField("item", StringType, boolean2Boolean(true)),
					StructField("click_count", IntegerType, boolean2Boolean(true))
				))

				// 这里构建HiveContext时候,rdd含有SparkContext
				val hiveContext = new HiveContext(rdd.context)
				val categoryItemDF = hiveContext.createDataFrame(categoryItemRow, structType)
				categoryItemDF.registerTempTable("categoryItemTable")

				//按类型取前三的SQL: row_number() over( partition by category order by click_count desc) rank
				val resultDataFrame = hiveContext.sql("SELECT category,item,click_count FROM (SELECT category,item,click_count,row_number()" +
						"OVER(PARTITION BY category ORDER BY click_count DESC) rank FROM categoryItemTable) subquery " +
						"WHERE rank <= 3")

				resultDataFrame.show()

				//DataFrame-->RowRDD-->JDBC入库
				val resultRowRDD = resultDataFrame.rdd //DataFrame to RDD
				resultRowRDD.foreachPartition { partitionOfRecords => {



					if (partitionOfRecords.isEmpty) {
						println("this is RDD is not null but partition is null")
					}
					else {
						// ConnectionPool is a static,lazily initialized pool of connections
						val connection = ConnectionPool.getConnection()
						partitionOfRecords.foreach(record => {
							val sql = "insert into streaming.category_top3 values('" + record.getAs("category") + "','" +
							record.getAs("item") + "','" + record.getAs("click_count") + ")"
							val stmt = connection.createStatement
							stmt.executeUpdate(sql)
						})
						ConnectionPool.returnConnection(connection)
					}





				}}
			}
		}}
		ssc.start()
		ssc.awaitTermination()
	}
}
