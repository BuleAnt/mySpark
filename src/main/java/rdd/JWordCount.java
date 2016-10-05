package rdd;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

public final class JWordCount {
	private static final Pattern SPACE = Pattern.compile(" ");

	public static void main(String[] args) throws Exception {

		testLocal();
		//run(args);
	}

	public static void run(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: JWordCount <file>");
			System.exit(1);
		}

		SparkConf sparkConf = new SparkConf().setAppName("JWordCount");
		JavaSparkContext ctx = new JavaSparkContext(sparkConf);

		JavaRDD<String> lines = ctx.textFile(args[0], 1);

		JavaRDD<String> words = lines
				.flatMap(new FlatMapFunction<String, String>() {
					public Iterable<String> call(String s) {
						//String-->String[]-->List
						//这里将分隔符使用Pattern分割
						return Arrays.asList(SPACE.split(s));
					}
				});

		JavaPairRDD<String, Integer> ones = words
				.mapToPair(new PairFunction<String, String, Integer>() {
					public Tuple2<String, Integer> call(String s) {
						return new Tuple2<String, Integer>(s, 1);
					}
				});

		JavaPairRDD<String, Integer> counts = ones
				.reduceByKey(new Function2<Integer, Integer, Integer>() {
					public Integer call(Integer i1, Integer i2) {
						return i1 + i2;
					}
				});


		List<Tuple2<String, Integer>> output = counts.collect();
		for (Tuple2<String, Integer> tuple : output) {
			System.out.println(tuple._1() + ": " + tuple._2());
		}
		ctx.stop();
	}

	/**
	 * 使用java 方式开发进行本地测试spark的wordCount程序
	 */
	public static void testLocal() {

		SparkConf conf = new SparkConf()
				.setAppName("Spark WordCount written by Java")
				.setMaster("local");
		//一些Log的设置可以不加
		Logger.getLogger("org.apache.spark").setLevel(Level.WARN);
		Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.ERROR);
		conf.set("log4j.rootCategory", "WARN");

		// SparkContext类在Java开发中为JavaSparkContext
		JavaSparkContext sc = new JavaSparkContext(conf);

		//TextFile-->RDD
		JavaRDD<String> lines = sc.textFile("src/main/resources/wordcount.sh");

		//RDD.map(_.split(" "))
		JavaRDD<String> words = lines
				.flatMap(new FlatMapFunction<String, String>() {
					@Override
					public Iterable<String> call(String line) throws Exception {
						return Arrays.asList(line.split(" "));
					}
				});

		//RDD.mapToPair-->PairRDD
		JavaPairRDD<String, Integer> pairs = words
				.mapToPair(new PairFunction<String, String, Integer>() {

					@Override
					public Tuple2<String, Integer> call(String word)
							throws Exception {
						return new Tuple2<String, Integer>(word, 1);
					}
				});

		//PairRDD.reduceByKey(_+_)
		JavaPairRDD<String, Integer> wordsCount = pairs
				.reduceByKey(new Function2<Integer, Integer, Integer>() {

					@Override
					public Integer call(Integer v1, Integer v2)
							throws Exception {
						return v1 + v2;
					}
				});

		//PairRDD-->List<Tuple2>-->foreach(println)
		wordsCount.foreach(new VoidFunction<Tuple2<String, Integer>>() {

			@Override
			public void call(Tuple2<String, Integer> pairs) throws Exception {
				System.out.println(pairs._1 + " : " + pairs._2);
			}
		});

		sc.close();
	}
}