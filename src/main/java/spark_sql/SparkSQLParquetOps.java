package spark_sql;

import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

public class SparkSQLParquetOps {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName(
                "SparkSQLParquetOps");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);

        // 读取parquet格式文件生成DataFrame
        DataFrame userDF = sqlContext.read().parquet("src/main/resources/users.parquet");
        // 注册为一张表
        userDF.registerTempTable("users");

        // sqlContext进行sql查询
        DataFrame result = sqlContext.sql("select name from users");

        // 这里可以将DataFrame转换为RDD,也可以直接输出
        result.javaRDD().map(new Function<Row, String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String call(Row row) throws Exception {
                return "The name is : " + row.getAs("name");
            }
        });

        // 遍历输出
        List<Row> ListRow = result.javaRDD().collect();
        for (Row person : ListRow) {
            System.out.println(person.toString());
        }
    }
}
