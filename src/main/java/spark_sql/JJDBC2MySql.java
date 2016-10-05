package spark_sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import scala.Tuple2;

/**
 * 1.Spark SQL可以通过JDBC从传统的关系型数据库中读写数据,
 * 读取数据后直接生成的DataFrame,
 * 然后在加上借助于Spark内核的丰富的API来进行各种操作
 */

public class JJDBC2MySql {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setMaster("local").setAppName("JDBC2MySql");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);

		/**
		 * 1.通过format("jdbc")的方式说明SparkSQL操作的数据来源
		 *      是通过JDBC获得的,JDBC后端一般都是数据库,例如Mysql,Oracle等
		 * 2.通过DataFrameReader的option方法把要访问的数据库的信息传递进去
		 *      url:数据库的jdbc链接地址
		 *      dbtable:具体哪一个数据表
		 * 3.Driver:JDBC驱动包可以放在Spark的lib目录下,
		 * 也可以在SparkSubmit中jar添加,具体编译和打包不需要这个JDBC的jar
		 */
		DataFrameReader reader = sqlContext.read().format("jdbc");
		reader.option("url", "jdbc:mysql://hadoop:3306/spark");
		reader.option("dbtable", "stu_age");
		reader.option("driver", "com.mysql.jdbc.Driver");
		reader.option("user", "root");
		reader.option("password", "root");

		// 这个时候并不真正的执行，lazy级别的:基于student表创建DataFrame
		DataFrame dtStuAgeDF = reader.load();
		dtStuAgeDF.show();
		reader.option("dbtable", "stu_score");//数据表名person
		DataFrame dtStuScoreDF = reader.load();// 基于person表创建DataFrame
		dtStuScoreDF.show();
		/**
		 * 在实际的企业级开发环境中,如果数据库中数据规模特别大:
		 * 例如10亿条数据,此时才是传统的DB去处理的话,一般需要对10亿条数据分成多批次处理,例如100批 (首先与单台Server的处理能力)
		 * 且实际的处理过程可能会非常复杂,通过传统的Java EE等技术可能很难或者不方便实时处理算法,
		 *
		 * 此时采用Spark SQL获得数据库中的数据并进行分布式处理就可以非常好的解决该问题.
		 * 但是由于Spark SQL加载DB中的数据需要时间,所以一般会在Spark SQL和具体要操作的DB之间加上一个缓冲层次.
		 * 例如中间使用Redis,可以把Spark处理速度提高甚至45倍
		 */
		//把DataFrame转换成RDD并且基于RDD进行Join操作(name 1)join(name 1),实际项目将会有特别复杂的操作
		JavaPairRDD<String, Tuple2<Integer, Integer>> stuRDD = dtStuAgeDF
				.javaRDD().mapToPair(new PairFunction<Row, String, Integer>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, Integer> call(Row row)
							throws Exception {
						return new Tuple2<>((String) row.getAs("name"), Integer.valueOf(String.valueOf(row.getAs("age"))));
					}
				})
				.join(dtStuScoreDF.javaRDD().mapToPair(
						new PairFunction<Row, String, Integer>() {
							private static final long serialVersionUID = 1L;

							@Override
							public Tuple2<String, Integer> call(Row row) throws Exception {
								return new Tuple2<>((String) row.getAs("name"), Integer.valueOf(String.valueOf(row.getAs("score"))));
							}
						}));
		//通过map将PairRDD转换为rowRDD
		JavaRDD<Row> stuRowRDD = stuRDD.map(new Function<Tuple2<String, Tuple2<Integer, Integer>>, Row>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Row call(
					Tuple2<String, Tuple2<Integer, Integer>> tuple) throws Exception {
				return RowFactory.create(tuple._1, tuple._2._2, tuple._2._1);
			}
		});

		//构建structType,通过createDataFrame将RDD转换为DataFrame
		List<StructField> structFields = new ArrayList<>();
		structFields.add(DataTypes.createStructField("name", DataTypes.StringType, true));
		structFields.add(DataTypes.createStructField("age", DataTypes.IntegerType, true));
		structFields.add(DataTypes.createStructField("score", DataTypes.IntegerType, true));
		StructType structType = DataTypes.createStructType(structFields);

		DataFrame stuDF = sqlContext.createDataFrame(stuRowRDD, structType);
		stuDF.show();
		/**
		 * 1. 当DataFrame要把通过Spark SQL,Core,ML等复杂操作后的数据写入数据库的时候,
		 *      首先是权限的问题,必须确保数据库授权了当前操作Spark SQL的用户
		 * 2.DataFrame要写数据到DB的时候,一般都不可以直接写进去,而是要转成RDD, 通过RDD写数据到DB中
		 */
		//foreachPartition对每个rdd的Partition操作
		stuDF.javaRDD().foreachPartition(new VoidFunction<Iterator<Row>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public void call(Iterator<Row> t) throws SQLException {
				Connection conn2MySQL = null;
				Statement statement = null;
				try {
					conn2MySQL = DriverManager.getConnection("jdbc:mysql://hadoop:3306/spark", "root", "root");
					statement = conn2MySQL.createStatement();
					String sql = "create table if not exists students(name varchar(20),age tinyint,score int)";
					statement.execute(sql);
					System.out.println(sql);
					while (t.hasNext()) {
						Row row = t.next();
						sql = "delete from students where name ='" + row.getAs("name") + "'";
						statement.execute(sql);
						System.out.println(sql);

						sql = "insert into students values (";
						sql += "'" + row.getAs("name") + "'," + row.getAs("age") + "," + row.getAs("score") + ")";
						//通过这个log就可发现该操作的顺序
						statement.execute(sql);
						System.out.println(sql);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					if (conn2MySQL != null) {
						try {
							conn2MySQL.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		// personDF.write().jdbc(url, table, connectionProperties);
	}
}
