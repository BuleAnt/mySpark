package core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

import java.util.Arrays;

/**
 * JRDDTransform
 */
public class JRDDTransform {


    public static void main(String[] args) {
        // Logger.getLogger("org.apache.spark").setLevel(Level.WARN);
        // Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF);
        SparkConf conf = new SparkConf()
                .setAppName("RDDTestJava")
                .setMaster("local");
        //conf.set("log4j.rootCategory", "WARN");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //ArrayList-->RDD
        JavaRDD<String> lines = sc.parallelize(Arrays.asList("hello world", "hello hadoop", "hello spark", "hello"));

        /**
         * map, 每个元素返回一个对象
         */
        JavaRDD<String> mapRDD = lines.map(new Function<String, String>() {
            @Override
            public String call(String v1) throws Exception {
                String line = Arrays.asList(v1.split(" ")).toString();
                System.out.println(line);
                return line;
            }
        }).persist(StorageLevel.MEMORY_ONLY());//如果不做persist在showRDD中还会重新执行一次map操作
        //StringUtils.join通过一个集合的迭代器,按照指定分隔符讲该其join到一个String中返回
        //System.out.println(StringUtils.join(result.collect(), ","));
        showRDD(mapRDD.setName("MapRDD"));


        /**
         * flat map,所有元素放入一个对象中返回，返回可迭代内容
         */
        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String line) throws Exception {
                System.out.println(Arrays.asList(line.split(" ")).toString());
                return Arrays.asList(line.split(" "));
            }
        }).setName("flatMapRDD").persist(StorageLevel.MEMORY_ONLY());
        System.out.println("words first: " + words.first());//first返回第一个元素
        showRDD(words);

        //distinct,集合去重，不常用，开销大
        JavaRDD<String> distinctRDD = words.distinct()
                .setName("distinctRDD")
                .persist(StorageLevel.MEMORY_ONLY());
        showRDD(distinctRDD);
        //union,合并，包括重复
        showRDD(words.union(distinctRDD).setName("unionRDD"));
        //intersection,返回共有的
        showRDD(words.intersection(distinctRDD).setName("intersectionRDD"));
        //subtract只在第一个中
        showRDD(distinctRDD.subtract(words).setName("subtract"));
        //cartesian,返回笛卡尔积
        showRDD(words.cartesian(distinctRDD));
    }


    private static void showRDD(JavaRDD strRDD) {
        System.out.println(strRDD.name() == null ? " " : strRDD.name() + "count:" + strRDD.count() + strRDD.collect());
    }

    private static void showRDD(JavaPairRDD<String, String> strRDD) {
        System.out.println(strRDD.name() == null ? " " : strRDD.name() + "count:" + strRDD.count());
      /*  for (Tuple2<String,String> it : strRDD.collect()) {
            System.out.println(it._1 + "   " + it._2 + "\n");
        }*/
        strRDD.foreach(new VoidFunction<Tuple2<String, String>>() {
            public void call(Tuple2<String, String> x) throws Exception {
                System.out.print(x);
            }
        });

    }

}
