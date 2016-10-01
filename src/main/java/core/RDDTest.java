package core;


import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;

/**
 * Created by hadoop on 16-8-24.
 */
public class RDDTest {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName(
                "RDDTestJava").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<Integer> rdd = sc.parallelize(Arrays.asList(1, 2, 3, 4));
        JavaRDD<Integer> result = rdd.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer x) throws Exception {
                return x * x;
            }
        });
        System.out.println(StringUtils.join(result.collect(), ","));

        JavaRDD<String> lines = sc.parallelize(Arrays.asList("hello world","hi"));
        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String line) throws Exception {
                return Arrays.asList(line.split(" "));
            }
        });
        words.first();
    }

}
