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

		DataFrame userDF = sqlContext.read().parquet("/opt/single/spark-compiled/examples/src/main/resources/users.parquet");
		userDF.registerTempTable("users");
		DataFrame result = sqlContext.sql("select name from users");
		result.javaRDD().map(new Function<Row, String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String call(Row row) throws Exception {
				return "The name is : " + row.getAs("name");
			}
		});
		List<Row> ListRow = result.javaRDD().collect();
		for (Row person : ListRow) {
			System.out.println(person.toString());
		}
	}
}
