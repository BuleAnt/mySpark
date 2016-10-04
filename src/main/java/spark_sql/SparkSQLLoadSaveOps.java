package spark_sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;

/**
 * load/save DataFrame的javaDemo
 */
public class SparkSQLLoadSaveOps {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("SparkSQLLoadSaveOps");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);

        // load,其中sqlContext.read()返回DataFrameReader,format指定文件格式json
        DataFrame peopleDF = sqlContext.read().format("json").load("src/main/resources/people.json");
        DataFrame usersDF = sqlContext.read().format("parquet").load("src/main/resources/users.parquet");

        // DataFrame.write()返回DataFrameWriter,该类可以通过mode指定写入模式
        // SaveMode默认ErrorIfExists,其他Append/Overwrite/Ignore
        // format指定写入格式,其他方法如save,saveAsTable,json,parquet,orc
        peopleDF.select("name").write().mode(SaveMode.ErrorIfExists)
                .format("json")
                .save("target/out/peopleNames.json");
        usersDF.select("name").write().mode(SaveMode.ErrorIfExists)
                .format("parquet")
                .save("target/out/usersNames.parquet");
    }
}
