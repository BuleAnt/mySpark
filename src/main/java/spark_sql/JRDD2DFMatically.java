package spark_sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;


/**
 * 使用Java编程RDD和DataFrame的动态转换
 */
public class JRDD2DFMatically {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setMaster("local").setAppName("RDD2DataFrameByProgrammatically");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		JavaRDD<String> lines = sc.textFile("src/main/resources/persons.txt");
		/**
		 * 第一步:在RDD的基础上创建Row的RDD
		 */
		JavaRDD<Row> personsRDD = lines.map(new Function<String, Row>() {
			@Override
			public Row call(String line) throws Exception {
				String[] splited = line.split(",");
				// 使用工厂方法生成避免手工实现,lines读取的是文件数据,顺序id,name,age不变
				return RowFactory.create(Integer.valueOf(splited[0]), splited[1], Integer.valueOf(splited[2]));
			}
		});

		/**
		 * 第二步:动态构造DataFrame的元数据,
		 * 一般而言,有多少列以及每列的具体内容可能来自于Json的文件,也可能来自于数据库DB
		 */
		List<StructField> structFields = new ArrayList<StructField>();
		//用循环的方式进行添加。最佳实践，数据库的列不要超过32列。当然，Spark对此没有限制
		structFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, true));
		structFields.add(DataTypes.createStructField("name", DataTypes.StringType, true));
		structFields.add(DataTypes.createStructField("age", DataTypes.IntegerType, true));
		//构建StructType，用于最后DataFrame元数据的描述
		StructType structType = DataTypes.createStructType(structFields);
		/**
		 * 第三步:基于以后的MetaData以及RDD<ROW>来构造DataFrame
		 * RDD(Spark核心),DataFrame(主要围绕SparkSQL,部分ML使用)
		 * DataSet(所有计算框架基于DataSet计算,底层钨丝计划,那么也就是所有的计算都可以使用钨丝计划)
		 */

		// sqlContext是传统代码方式,正常使用hiveContext官方也推荐
		// 这里在此说明hiveContext不是在hive的基础上使用,是由于hiveContext功能比sqlContext更强大
		// createDataFrame()通过rowRDD以及structType动态构建DataFrame
		DataFrame personsDF = sqlContext.createDataFrame(personsRDD, structType);

		/**
		 * 第四步:注册成为临时表以供后续的SQL查询操作
		 */
		personsDF.registerTempTable("persons");//背后发生的不是现在考虑的

		/**
		 * 第五步:进行数据多维度分析
		 */
		DataFrame result = sqlContext.sql("select * from persons where age > 7");

		List<Row> listRow = result.javaRDD().collect();
		for (Row row : listRow) {
			System.out.println(row);
		}
	}
}
