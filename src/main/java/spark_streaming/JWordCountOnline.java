package spark_streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * JWordCountOnline
 */
public class JWordCountOnline {
	public static void main(String[] args) {
		/**
		 * 第一步：配置SparkConf：
		 */
		SparkConf conf = new SparkConf();
		conf.setMaster("local[2]").setAppName("JWordCountOnline");
		//conf = new SparkConf().setMaster("spark://Master:7077").setAppName("JWordCountOnline");

		/**
		 * 第二步:创建SparkStreamingContext
		 */
		JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(5));

		/**
		 * 第三步：创建Spark Streaming输入数据来源input stream：ReceiverInputDStream
		 * 我们将数据来源配置为本地端口9999（注意端口要求没有被占用）
		 * 在shell中通过: nc -lk 9999打开socket的client建立链接,输入数据
		 */
		JavaReceiverInputDStream<String> lines = jsc.socketTextStream("hadoop", 9999);

		/**
		 * 第四步：接下来就像对于RDD编程一样基于DStream进行编程！！！
		 * DStream是RDD产生的模板或者说是类。在Spark Streaming
		 */
		JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
			@Override
			public Iterable<String> call(String line) throws Exception {
				return Arrays.asList(line.split(" "));
			}
		});
		JavaPairDStream<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {

			@Override
			public Tuple2<String, Integer> call(String word) throws Exception {
				return new Tuple2<>(word, 1);
			}
		});
		JavaPairDStream wordsCount = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
			@Override
			public Integer call(Integer v1, Integer v2) throws Exception {
				return v1 + v2;
			}
		});

		/**
		 * 此处的print并不会直接触发Job的执行,注意Steaming程序中不许要有输出
		 */
		wordsCount.print();

		/**
		 * start方法使Spark Streaming执行引擎也就是Driver开始运行，
		 * Driver启动的时候是位于一条新的线程中的。当然，其内部有消息循环体用于接收应用程序本身或者Executor中的消息。
		 */
		//开是执行Stream
		jsc.start();
		//等待执行停止,如果在执行过程中发生的任何异常将被抛出这个线程
		jsc.awaitTermination();
		//调用stop,停止Stream的执行
		jsc.close();
	}
}
