package core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;

import java.util.Arrays;
import java.util.Map;

/**
 * JRDDAction
 */
public class JRDDAction {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Simple Application");
        JavaSparkContext sc = new JavaSparkContext(conf);

        //使用parallelize创建RDD
        JavaRDD<Integer> lines1 = sc.parallelize(Arrays.asList(1, 3, 2, 4));
        lines1.persist(StorageLevel.MEMORY_ONLY());
        System.out.println("222222222Count is:" + lines1.count());

        JavaRDD<String> lines2 = sc.parallelize(Arrays.asList("pandas", "like", "i like pandas"));
        lines2.persist(StorageLevel.MEMORY_ONLY());
        System.out.println("333333333333Count is:" + lines2.count());

        //行动操作
        //reduce, 常见行动操作
        Integer reduceResult = lines1.reduce(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer x, Integer y) throws Exception {
                return x + y;
            }
        });
        System.out.println("44444444444444444reduceResult is:" + reduceResult);
        //fold, 类似reduce，接受初始值，作为每个分区第一次调用时的结果。对这个初始值多次计算不能影响结果，例如+表示0，*表示1
        Integer foldResult = lines1.fold(0, new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer x, Integer y) throws Exception {
                return x + y;
            }
        });
        System.out.println("555555555555foldResult is:" + foldResult);
        //aggregate, 类似fold，但是返回值不一定和输入类型一致
        Function2<AvgCount, Integer, AvgCount> addAndCount = new Function2<AvgCount, Integer, AvgCount>() {
            public AvgCount call(AvgCount a, Integer x) {
                a.total += x;
                a.num += 1;
                return a;
            }
        };
        Function2<AvgCount, AvgCount, AvgCount> combine = new Function2<AvgCount, AvgCount, AvgCount>() {
            public AvgCount call(AvgCount a, AvgCount b) {
                a.total += b.total;
                a.num += b.num;
                ;
                return a;
            }
        };
        AvgCount init = new AvgCount(0, 0);
        AvgCount result = lines1.aggregate(init, addAndCount, combine);
        System.out.println("6666666666 avg  is:" + result.avg());
        //top,从RDD中 返回经排序后的前几个元素，从大到小
        System.out.println("7777777777777 top  is:");
        for (Integer it : lines1.top(4)) {
            System.out.println(it + "   ");
        }

        //take,随机提取几个元素
        System.out.println("88888888888 take  is:");
        for (Integer it : lines1.take(4)) {
            System.out.println(it + "   ");
        }

        //takeSample,数据采样
        System.out.println("9999999999999 takeSample  is:");
        for (Integer it : lines1.takeSample(true, 2, 3)) {
            System.out.println(it + "   ");
        }

        //takeOrdered,按提供顺序返回，经排序，例如从1小到达
        System.out.println("9999999999999 takeOrdered  is:");
        for (Integer it : lines1.takeOrdered(4)) {
            System.out.println(it + "   ");
        }

        //countByValue,获取map对
        Map<Integer, Long> cbv = lines1.countByValue();
        System.out.println("9999999999999 countByValue  is:");
        for (Integer it : cbv.keySet()) {
            System.out.println(it + "   " + cbv.get(it) + " ");
        }

        //foreach,遍历rdd内容
        System.out.println("9999999999999 foreach  is:");
        lines1.foreach(new VoidFunction<Integer>() {
            public void call(Integer t) throws Exception {
                System.out.println(t + " ");
            }
        });


    }
}
