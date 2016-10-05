package rdd;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;

import java.util.Arrays;
import java.util.Map;

/**
 * RDD Action
 * reduce(func) 例如:reduce((_+_))
 * collect() :转换成一个集合
 * count() / first() /top(n)
 * take(n) / takeSample(withReplacement, num, [seed])
 * takeOrdered(n, [ordering])
 * countByKey() 对key进行count:See:JPairRDD.java
 * foreach(func) 遍历
 * 其中save操作有:
 * saveAsTextFile(path) 文本文件保存
 * saveAsSequenceFile(path)(Java and Scala)
 * saveAsObjectFile(path)(Java and Scala)
 */
public class JAction {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local");
		JavaSparkContext sc = new JavaSparkContext(conf);

		//创建RDD
		JavaRDD<Integer> lines1 = sc.parallelize(Arrays.asList(1, 3, 2, 4));
		lines1.persist(StorageLevel.MEMORY_ONLY());
		JavaRDD<String> lines2 = sc.parallelize(Arrays.asList("pandas", "like", "i like pandas"));
		lines2.persist(StorageLevel.MEMORY_ONLY());


		//reduce, 常见行动操作:将RDD中元素两两传递给输入函数，同时产生一个新的值，
		// 新产生的值与RDD中下一个元素再被传递给输入函数直到最后只有一个值为止。
		Integer reduceResult = lines1.reduce(new Function2<Integer, Integer, Integer>() {
			public Integer call(Integer x, Integer y) throws Exception {
				return x + y;
			}
		});
		System.out.println("reduceResult is:" + reduceResult);

		// fold, 类似reduce，接受初始值，作为每个分区第一次调用时的结果。
		// 对这个初始值多次计算不能影响结果，例如+表示0，*表示1
		Integer foldResult = lines1.fold(0, new Function2<Integer, Integer, Integer>() {
			public Integer call(Integer x, Integer y) throws Exception {
				return x + y;
			}
		});
		System.out.println("foldResult is:" + foldResult);

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
				return a;
			}
		};
		AvgCount init = new AvgCount(0, 0);
		AvgCount result = lines1.aggregate(init, addAndCount, combine);
		System.out.println("avg  is:" + result.avg());


		//top,从RDD中返回经排序后的前几个元素，从大到小
		System.out.println("top  is:");
		for (Integer it : lines1.top(4)) {
			System.out.println(it + "   ");
		}

		//take,随机提取几个元素
		System.out.println("take  is:");
		for (Integer it : lines1.take(4)) {
			System.out.println(it + "   ");
		}

		//takeSample,数据采样
		System.out.println("takeSample  is:");
		for (Integer it : lines1.takeSample(true, 2, 3)) {
			System.out.println(it + "   ");
		}

		//takeOrdered,按提供顺序返回，经排序，例如从1小到达
		System.out.println("takeOrdered  is:");
		for (Integer it : lines1.takeOrdered(4)) {
			System.out.println(it + "   ");
		}

		//countByValue,获取map对
		Map<Integer, Long> cbv = lines1.countByValue();
		System.out.println("countByValue  is:");
		for (Integer it : cbv.keySet()) {
			System.out.println(it + "   " + cbv.get(it) + " ");
		}

		//foreach,遍历rdd内容
		System.out.println("foreach  is:");
		lines1.foreach(new VoidFunction<Integer>() {
			public void call(Integer t) throws Exception {
				System.out.println(t + " ");
			}
		});


	}
}
