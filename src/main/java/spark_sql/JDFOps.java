package spark_sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

/**
 * 使用Java方式开发实战对DataFrame的操作
 */
public class JDFOps {
	public static void main(String[] args) {
		// 创建SparkConf用于读取系统配置信息并设置当前应用程序的名字
		SparkConf conf = new SparkConf().setAppName("DataFrame").setMaster("local");
		// 创建JavaSparkContext对象实例作为整个Driver的核心基石
		JavaSparkContext sc = new JavaSparkContext(conf);

		// 创建SQLContext上下文对象用于SQL的分析,
		// HiveContext继承自SQLContext,通过Ctrl+H查看继承结构,Spark官方文档建议任何时候用HiveContext，
		// HiveContext不仅有SQLContext的功能，而且可以操作Hive数据仓库。没有必要使用SQLContext
		SQLContext sqlContext = new SQLContext(sc);

		// DataFrame基于数据来源构建，例如说Json数据读取。
		// DataFrame可以理解为一张表,SQLContext可以设置其方言。
		DataFrame df = sqlContext.read().json("src/main/resources/people.json");

		df.show();//select * from table;

		df.printSchema();//desc table

		df.select("name").show();//select name from table

		df.select(df.col("name"), df.col("age").plus(10)).show();//select name,age+10 from table

		df.filter(df.col("age").gt(20)).show();//select age from table where age>20

		df.groupBy(df.col("age")).count().show();//select age,count(1) from table group by age
	}
}
