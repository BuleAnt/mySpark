package rdd;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

public class JTopNGroup {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf().setAppName("JTopNGroup").setMaster(
                "local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile("/home/hadoop/test/data/topNGroup.txt");
        /*
		 * JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String,
		 * String>() { private static final long serialVersionUID = 1L;
		 * 
		 * @Override public Iterable<String> call(String t) throws Exception {
		 * return Arrays.asList(t.split(" ")); } });
		 */
        // 把每行数据变成符合要求的K-V方式
        JavaPairRDD<String, Integer> pairs = lines.mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Tuple2<String, Integer> call(String line)
                    throws Exception {
                String[] splitedLine = line.split(" ");
                return new Tuple2<String, Integer>(splitedLine[0],
                        Integer.valueOf(splitedLine[1]));
            }
        });
        JavaPairRDD<String, Iterable<Integer>> groupedPairs = pairs.groupByKey();// 对数据进行分组
        JavaPairRDD<String, Iterable<Integer>> top5 = groupedPairs
                .mapToPair(new PairFunction<Tuple2<String, Iterable<Integer>>, String, Iterable<Integer>>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<String, Iterable<Integer>> call(Tuple2<String, Iterable<Integer>> groupedData)
                            throws Exception {
                        Integer[] top5 = new Integer[5];// 保存Top5的数据本身
                        String groupedKey = groupedData._1;// 获取分组的组分名
                        Iterator<Integer> groupedValue = groupedData._2.iterator();// 获取每组的内容集合
                        while (groupedValue.hasNext()) {// 查看是否有下一个元素,如果有继续循环
                            Integer value = groupedValue.next();// 获取当前循环的元素本身的内容
                            for (int i = 0; i < 5; i++) {//具体实现top 5
                                if (top5[i] == null) {
                                    top5[i] = value;//填充top5[i]数组
                                    break;//填充完成直接跳出
                                } else if (value > top5[i]) {//如果有top5[i]的值,则对value进行比较排序,如果value比top5[i]小,则i++进入下一个for循环
                                    for (int j = 4; j > i; j--) {//从后向前,递归比较
                                        top5[j] = top5[j - 1];//将原来值向后移动
                                    }
                                    top5[i] = value;//将value赋值给比较后最后的位置
                                    break;
                                }
                            }
                        }
                        return new Tuple2<String, Iterable<Integer>>(
                                groupedKey, Arrays.asList(top5));
                    }
                });

        // 打印分组后的Top N
        top5.foreach(new VoidFunction<Tuple2<String, Iterable<Integer>>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void call(Tuple2<String, Iterable<Integer>> topped)
                    throws Exception {
                System.out.println("Group Key: " + topped._1);// 获取group Key
                Iterator<Integer> toppedValue = topped._2.iterator();// 获取group Value
                while (toppedValue.hasNext()) {// 具体打印每组的Top N
                    Integer value = toppedValue.next();
                    System.out.println(value);
                }
                System.out.println("*******************************************");
            }
        });
        sc.close();
    }
}
