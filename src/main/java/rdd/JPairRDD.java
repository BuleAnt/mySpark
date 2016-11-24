package rdd;

import com.google.common.base.Optional;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Spark PairRDD
 */
public class JPairRDD {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local");
		JavaSparkContext sc = new JavaSparkContext(conf);

		// convert from other RDD
		JavaRDD<String> rdd = sc.parallelize(Arrays.asList("1 aa", "2 bb", "4 cc", "3 dd"));
		/**
		 * JCreate PairRDD:
		 * mapToPair将一个rdd转化为pairRDD
		 * parallelizePairs将一个List转化为PairRDD
		 */
		//
		JavaPairRDD<String, String> pairRDD = rdd.mapToPair(new PairFunction<String, String, String>() {
			public Tuple2<String, String> call(String x) throws Exception {
				return new Tuple2<String, String>(x.split(" ")[0], x);
			}
		});
		System.out.println("mapToPair:");
		pairRDD.foreach(new VoidFunction<Tuple2<String, String>>() {
			public void call(Tuple2<String, String> x) throws Exception {
				System.out.println(x);
			}
		});

		// parallelizePairs
		List<Tuple2<Integer, Integer>> tuple2List = Arrays.asList(new Tuple2<Integer, Integer>(1, 2), new Tuple2<Integer, Integer>(3, 4), new Tuple2<Integer, Integer>(3, 6));
		JavaPairRDD<Integer, Integer> line2 = sc.parallelizePairs(tuple2List)
				.persist(StorageLevel.MEMORY_ONLY());

		List<Tuple2<Integer, Integer>> tuple2List1 = Arrays.asList(new Tuple2<Integer, Integer>(3, 9));
		JavaPairRDD<Integer, Integer> tuple2RDD = sc.parallelizePairs(tuple2List1)
				.persist(StorageLevel.MEMORY_ONLY());

		/**
		 * JTransformation:
		 * subtractByKey
		 * join / rightOuterJoin / leftOuterJoin
		 * cogroup
		 * combineByKey
		 * sortByKey
		 */
		// subtractByKey
		JavaPairRDD<Integer, Integer> line4 = line2.subtractByKey(tuple2RDD);
		System.out.println("subtractByKey:");
		line4.foreach(new VoidFunction<Tuple2<Integer, Integer>>() {
			public void call(Tuple2<Integer, Integer> x) throws Exception {
				System.out.println(x);
			}
		});

		// join
		JavaPairRDD<Integer, Tuple2<Integer, Integer>> line5 = line2.join(tuple2RDD);
		System.out.println("join:");
		line5.foreach(new VoidFunction<Tuple2<Integer, Tuple2<Integer, Integer>>>() {
			public void call(Tuple2<Integer, Tuple2<Integer, Integer>> x) throws Exception {
				System.out.println(x);
			}
		});

		// rightOuterJoin
		JavaPairRDD<Integer, Tuple2<Optional<Integer>, Integer>> line6 = line2.rightOuterJoin(tuple2RDD);
		System.out.println("rightOuterJoin:");
		line6.foreach(new VoidFunction<Tuple2<Integer, Tuple2<Optional<Integer>, Integer>>>() {
			public void call(Tuple2<Integer, Tuple2<Optional<Integer>, Integer>> x) throws Exception {
				System.out.println(x);
			}
		});

		// leftOuterJoin
		JavaPairRDD<Integer, Tuple2<Integer, Optional<Integer>>> line7 = line2.leftOuterJoin(tuple2RDD);
		System.out.println("leftOuterJoin:");
		line7.foreach(new VoidFunction<Tuple2<Integer, Tuple2<Integer, Optional<Integer>>>>() {
			public void call(Tuple2<Integer, Tuple2<Integer, Optional<Integer>>> x) throws Exception {
				System.out.println(x);
			}
		});

		// cogroup
		JavaPairRDD<Integer, Tuple2<Iterable<Integer>, Iterable<Integer>>> line8 = line2.cogroup(tuple2RDD);
		System.out.println("cogroup:");
		line8.foreach(new VoidFunction<Tuple2<Integer, Tuple2<Iterable<Integer>, Iterable<Integer>>>>() {
			public void call(Tuple2<Integer, Tuple2<Iterable<Integer>, Iterable<Integer>>> x) throws Exception {
				System.out.println(x);
			}
		});

		// combineByKey,聚合
		// 1. createCombiner, if key already exists, then do mergeValue.
		// a[1]:(2,1), a[3]:(4,1)
		Function<Integer, AvgCount> ca = new Function<Integer, AvgCount>() {
			public AvgCount call(Integer x) throws Exception {
				return new AvgCount(x, 1);
			}
		};
		// 2. mergeValue
		// a[3]:(a[3],6) => (10,2)
		Function2<AvgCount, Integer, AvgCount> addAndCount = new Function2<AvgCount, Integer, AvgCount>() {
			public AvgCount call(AvgCount x, Integer y) throws Exception {
				x.total += y;
				x.num += 1;
				return x;
			}
		};
		// 3.mergeCombiners in different partitions
		// if (4,1) and (6,1) are different partitions , then =>(10,2)
		Function2<AvgCount, AvgCount, AvgCount> combine = new Function2<AvgCount, AvgCount, AvgCount>() {
			public AvgCount call(AvgCount x, AvgCount y) throws Exception {
				x.total += y.total;
				x.num += y.num;
				return x;
			}
		};
		JavaPairRDD<Integer, AvgCount> avgCounts = line2.combineByKey(ca, addAndCount, combine);
		System.out.println("combineByKey:");
		avgCounts.foreach(new VoidFunction<Tuple2<Integer, AvgCount>>() {
			public void call(Tuple2<Integer, AvgCount> x) throws Exception {
				System.out.println(x._1 + " " + x._2.avg());
			}
		});

		// sortByKey
		JavaPairRDD<Integer, Integer> sortRDD = line2.sortByKey(new MyComparator());
		System.out.println("sortByKey:");
		sortRDD.foreach(new VoidFunction<Tuple2<Integer, Integer>>() {
			public void call(Tuple2<Integer, Integer> x) throws Exception {
				System.out.println(x);
			}
		});
	}

	/**
	 * Action:
	 * countByKey() 对key进行count
	 */

}

class MyComparator implements Comparator<Integer>, Serializable {
	@Override
	public int compare(Integer o1, Integer o2) {
		return o1 - o2;
	}
}

class AvgCount implements Serializable {
	public int total;
	public int num;

	public double avg() {
		return total / (double) num;
	}

	public AvgCount(int total, int num) {
		this.total = total;
		this.num = num;
	}

}