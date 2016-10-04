package rdd;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;

import java.util.Arrays;


public class JRDDCreate {

    public static SparkConf conf = new SparkConf()
            .setAppName("RDDTestJava")
            .setMaster("local");
    public static JavaSparkContext sc = new JavaSparkContext(conf);

    public static void main(String[] args) {
        Logger.getLogger("org.apache.spark").setLevel(Level.WARN);
        Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF);
        testParallelize();
    }

    public static void testParallelize() {
        //使用parallelize创建RDD
        JavaRDD<Integer> intRDD = sc.parallelize(Arrays.asList(1, 2, 3, 4));

        //持久化RDD，方便后面多次动作，无需重复计算
        //不能与JavaRDD<String> logData = sc.textFile(logFile).cache();同用，因为cache已指定内存持久化
        intRDD.persist(StorageLevel.MEMORY_ONLY());
        System.out.println("intRDD count is:" + intRDD.count());

        JavaRDD<String> strRDD = sc.parallelize(Arrays.asList("pandas", "like", "i like pandas"));
        strRDD.persist(StorageLevel.MEMORY_ONLY());
        System.out.println("strRDD count is:" + strRDD.count());
    }


}
