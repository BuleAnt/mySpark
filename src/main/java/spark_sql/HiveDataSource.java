package spark_sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.hive.HiveContext;

public class HiveDataSource {
	public static void main(String[] args) {

		SparkConf conf = new SparkConf().setMaster("");
		JavaSparkContext sc = new JavaSparkContext(conf);
		HiveContext hiveContext = new HiveContext(sc);

		hiveContext.sql("drop table if exits students_infos");
		hiveContext.sql("create table if not exits student_infos(name string, age Int)");
		hiveContext.sql("load data local inpath'/user/hadoop/..");
		hiveContext.sql("create table if not exists students_scores(name string,score int)");
		hiveContext.sql("load data local inpath'/user/hadoop/..");
		DataFrame goodStudentsDF = hiveContext.sql("select from student_infos si.name ,ss.score from students_info si join students_scores ss ON si.name = ss.name");
		hiveContext.sql("drop table if exists good_students_infos");
		goodStudentsDF.saveAsTable("good_student_infos");
	}
}
