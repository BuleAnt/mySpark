package core;
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

import java.util.Arrays;

/**
 * 使用java 方式开发进行本地测试spark的wordCount程序
 */
public class WordCountJava {
    @SuppressWarnings("serial")
    public static void main(String[] args) {
        Logger.getLogger("org.apache.spark").setLevel(Level.WARN);
        Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.ERROR);
        SparkConf conf = new SparkConf().setAppName(
                "Spark WordCountJava written by Java").setMaster("local");
        conf.set("log4j.rootCategory", "WARN");
        // SparkContext类在不同的语言中不同,如果是Java开发,为JavaSparkContext
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc
                .textFile("src/main/java/core/JavaWordCount.java");
        // 如果是scala,由于SAM转换,可以写成:val lines =
        // sc.textFile("/user/spark/wc/input/data")
        JavaRDD<String> words = lines
                .flatMap(new FlatMapFunction<String, String>() {
                    @Override
                    public Iterable<String> call(String line) throws Exception {
                        return Arrays.asList(line.split(" "));
                    }
                });
        JavaPairRDD<String, Integer> pairs = words
                .mapToPair(new PairFunction<String, String, Integer>() {

                    @Override
                    public Tuple2<String, Integer> call(String word)
                            throws Exception {
                        return new Tuple2<String, Integer>(word, 1);
                    }
                });
        JavaPairRDD<String, Integer> wordsCount = pairs
                .reduceByKey(new Function2<Integer, Integer, Integer>() {

                    @Override
                    public Integer call(Integer v1, Integer v2)
                            throws Exception {
                        return v1 + v2;
                    }
                });
        wordsCount.foreach(new VoidFunction<Tuple2<String, Integer>>() {

            @Override
            public void call(Tuple2<String, Integer> pairs) throws Exception {
                System.out.println(pairs._1 + " : " + pairs._2);
            }
        });
        sc.close();
    }
}

