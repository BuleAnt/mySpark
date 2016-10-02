package spark_sql;

import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

//使用反射将RDD转换成DataFrame
public class RDD2DataFrameByReflectionJava {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setMaster("local")
			.setAppName("RDD2DataFrameByReflectionJava")
			.setMaster("local")
			.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);

		JavaRDD<String> lines = sc
			.textFile("src/main/resources/persons.txt");
		JavaRDD<Person> persons = lines.map(new Function<String, Person>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Person call(String line) throws Exception {
				String[] splited = line.split(",");
				Person p = new Person();
				p.setId(Integer.valueOf(splited[0].trim()));
				p.setName(splited[1]);
				p.setAge(Integer.valueOf(splited[2].trim()));
				return p;
			}
		});

		// 在底层通过反射方式获得Person的所有fields,结合RDD本身生成DataFrame
		//(ScalaReflection.schemaFor[A].dataType.asInstanceOf[StructType])
		DataFrame df = sqlContext.createDataFrame(persons, Person.class);
		// 将DataFrame转化成一张表
		df.registerTempTable("persons");

		DataFrame bigDatas = sqlContext
			.sql("select * from persons where age >= 6");

		// 将一个DataFrame转化为rdd
		JavaRDD<Row> bigDataRDD = bigDatas.javaRDD();

		JavaRDD<Person> result = bigDataRDD.map(new Function<Row, Person>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Person call(Row row) throws Exception {
				Person p = new Person();
				// 注意这里df中的位置以属性名称字典顺序进行排序:age,id,name
				p.setId(row.getInt(1));
				p.setName(row.getString(2));
				p.setAge(row.getInt(0));
				return p;
			}
		});

		// 返回RDD中所有元素
		List<Person> personList = result.collect();
		for (Person person : personList) {
			System.out.println(person.toString());
		}
	}
}

