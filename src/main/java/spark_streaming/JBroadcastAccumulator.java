package spark_streaming;

import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

/**
 * 联合使用Spark Streaming、Broadcast、Accumulator计数器实现在线黑名单过滤和计数
 */
public class JBroadcastAccumulator {
	private static volatile Broadcast<List<String>> broadcastList = null;
	private static volatile Accumulator<Integer> accumulator = null;

	public void main(String[] args) throws InterruptedException {

		SparkConf conf = new SparkConf().setMaster("local[2]").
				setAppName("SparkStreamingBroadcastAccumulator");


		JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(15));

		//实例化我们的broadcast,使用Broadcast广播黑名单到每个Executor中
		broadcastList = jsc.sparkContext().broadcast(Arrays.asList("Hadoop", "Mahout", "Hive"));
		/**
		 * 全局计数器，用于统计在线过滤多少黑名单
		 */
		accumulator = jsc.sparkContext().accumulator(0, "OnlineBlacklistCount");

		JavaReceiverInputDStream<String> lines = jsc.socketTextStream("Master", 9999);

		JavaPairDStream<String, Integer> pairs = lines.mapToPair(new PairFunction<String, String, Integer>() {

			public Tuple2<String, Integer> call(String word) throws Exception {
				return new Tuple2<>(word, 1);
			}
		});

		JavaPairDStream<String, Integer> wordsCount = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
			public Integer call(Integer v1, Integer v2) throws Exception {
				return v1 + v2;
			}
		});

        /*过滤黑明单我们一般把内容写在foreach中*/

		wordsCount.foreachRDD(new Function2<JavaPairRDD<String, Integer>, Time, Void>() {

			public Void call(JavaPairRDD<String, Integer> rdd, Time time) throws Exception {
				rdd.filter(new Function<Tuple2<String, Integer>, Boolean>() {
					public Boolean call(Tuple2<String, Integer> wordPair) throws Exception {
						if (broadcastList.value().contains(wordPair._1)) {
							accumulator.add(wordPair._2);
							return false;

						} else {
							return true;
						}
					}


				}).collect();

//                System.out.println(broadcastList.value().toString() + ":" + accumulator.value());
				System.out.println("BlackList append : " + ":" + accumulator.value() + "times");
				return null;
			}
		});


		jsc.start();

		jsc.awaitTermination();

		jsc.close();
	}
}
