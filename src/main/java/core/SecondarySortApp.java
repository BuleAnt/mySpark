package core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

/**
 * 具体的实现步骤: 第一步:按照Orderd和Serializable接口实现自定义排序的key
 * 第二步:将要进行二次排序的文件加载进来<Key,Value>类型的RDD 第三步:使用sortByKey基于自定义的Key进行二次排序
 * 第四步:去除掉排序的Key,只保留排序的结果
 */
public class SecondarySortApp {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("SecondarySort").setMaster(
				"local");
		@SuppressWarnings("resource")
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<String> lines = sc.textFile("file:///home/hadoop/test/data/txt/a");
		JavaPairRDD<SecondarySortKeyJava, String> pairs = lines
				.mapToPair(new PairFunction<String, SecondarySortKeyJava, String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<SecondarySortKeyJava, String> call(String line)
							throws Exception {
						String[] splited = line.split("\t");
						SecondarySortKeyJava key = new SecondarySortKeyJava(Integer
								.valueOf(splited[0]), Integer
								.valueOf(splited[1]));
						return new Tuple2<SecondarySortKeyJava, String>(key, line);
					}
				});
		JavaPairRDD<SecondarySortKeyJava, String> sorted = pairs.sortByKey();//完成二次排序
		
		// 过滤掉排序后的Key,保留排序的结果Tuple2<SecondarySortKeyJava, String>(key, line)=>line:String
		JavaRDD<String> secondarySorted = sorted
				.map(new Function<Tuple2<SecondarySortKeyJava, String>, String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String call(
							Tuple2<SecondarySortKeyJava, String> sortedContent)
							throws Exception {
						return sortedContent._2;
					}
				});

		secondarySorted.foreach(new VoidFunction<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void call(String sorted) throws Exception {
				System.out.println(sorted);
			}
		});
	}
}
