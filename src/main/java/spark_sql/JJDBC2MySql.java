package spark_sql;

/**
 * 1.Spark SQL可以通过JDBC从传统的关系型数据库中读写数据,读取数据后直接生成的DataFrame,然后在加上借助于Spark内核的丰富的API来进行各种操作
 */

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

public class JJDBC2MySql {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName(
                "JJDBC2MySql");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        /**
         * 1 .通过format("jdbc")的方式说明SparkSQL操作的数据来源
         * 是通过JDBC获得的,JDBC后端一般都是数据库,例如Mysql,Oracle等 2
         * .通过DataFrameReader的option方法把要访问的数据库的信息传递进去 url:数据库的jdbc链接地址
         * dbtable:具体哪一个数据表 3 .JDBC驱动包可以放在Spark的lib目录下,也可以在SparkSubmit中jar添加
         * 具体编译和打包不需要这个JDBC的jar
         */
        DataFrameReader reader = sqlContext.read().format("jdbc");
        reader.option("url", "jdbc:myslq://hadoop:3306/spark");
        reader.option("dbtable", "dtspark");
        reader.option("driver", "com.mysql.jdbc.Driver");
        reader.option("user", "root");
        reader.option("password", "root");
        /**
         * 在实际的企业级开发环境中,如果数据库中数据规模特别大:例如10亿条数据,此时才是传统的DB去处理的话
         * 一般需要对10亿条数据分成多批次处理,例如100批 (首先与单台Server的处理能力)
         * 且实际的处理过程可能会非常复杂,通过传统的Java EE等技术可能很难或者不方便实时处理算法,此时采用Spark
         * SQL获得数据库中的数据并进行分布式处理就可以非常好的解决该问题. 但是由于Spark
         * SQL加载DB中的数据需要时间,所以一般会在Spark SQL和具体要操作的DB之间加上一个缓冲层次.
         * 例如中间使用Redis,可以把Spark处理速度提高甚至45倍
         */
        DataFrame dtSparkDataSourceDFFromMysql = reader.load();// 基于dtspark表创建DataFrame
        reader.option("dbtable", "dthadoop");
        DataFrame dtHadoopDataSourceDFFromMysql = reader.load();// 基于dthadoop表穿件DataFrame

        /**
         * 把DataFrame转换成RDD并且基于RDD进行Join操作
         */
        JavaPairRDD<String, Tuple2<Integer, Integer>> resultRDD = dtSparkDataSourceDFFromMysql
                .javaRDD()
                .mapToPair(new PairFunction<Row, String, Integer>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<String, Integer> call(Row row)
                            throws Exception {
                        return new Tuple2<String, Integer>((String) row
                                .getAs("name"), (int) row.getLong(1));
                    }
                })
                .join(dtHadoopDataSourceDFFromMysql.javaRDD().mapToPair(
                        new PairFunction<Row, String, Integer>() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public Tuple2<String, Integer> call(Row row)
                                    throws Exception {
                                return new Tuple2<String, Integer>((String) row
                                        .getAs("name"), (int) row.getLong(1));
                            }
                        }));
        JavaRDD<Row> reusltRowRDD = resultRDD
                .map(new Function<Tuple2<String, Tuple2<Integer, Integer>>, Row>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Row call(
                            Tuple2<String, Tuple2<Integer, Integer>> tuple)
                            throws Exception {

                        return RowFactory.create(tuple._1, tuple._2._2,
                                tuple._2._1);
                    }
                });

        List<StructField> structFields = new ArrayList<StructField>();
        structFields.add(DataTypes.createStructField("name",
                DataTypes.StringType, true));
        structFields.add(DataTypes.createStructField("age",
                DataTypes.IntegerType, true));
        structFields.add(DataTypes.createStructField("score",
                DataTypes.IntegerType, true));
        StructType structType = DataTypes.createStructType(structFields);
        DataFrame personDF = sqlContext.createDataFrame(reusltRowRDD,
                structType);
        personDF.show();
        /**
         * 1. 当DataFrame要把通过Spark SQL,Core,ML等复杂操作后的数据写入数据库的时候,
         * 首先是权限的问题,必须确保数据库授权了当前操作Spark SQL的用户 2.
         * DataFrame要写数据到DB的时候,一般都不可以直接写进去,而是要转成RDD, 通过RDD写数据到DB中
         */
        personDF.javaRDD().foreachPartition(new VoidFunction<Iterator<Row>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void call(Iterator<Row> t) throws SQLException {
                Connection conn2MySQL = null;
                Statement statement = null;
                String sql = "insert into xxx";
                try {
                    conn2MySQL = DriverManager.getConnection(
                            "jdbc:myslq://hadoop:3306/spark", "root", "root");
                    statement = conn2MySQL.createStatement();
                    statement.execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (conn2MySQL != null) {
                        conn2MySQL.close();
                    }
                }
            }
        });
        // personDF.write().jdbc(url, table, connectionProperties);
    }
}
