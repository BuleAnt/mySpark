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
import org.apache.spark.streaming.flume.FlumeUtils;
import org.apache.spark.streaming.flume.SparkFlumeEvent;
import scala.Tuple2;

import java.util.Arrays;

/**
 * Flume2SparkStreaming-->wordCount
 * ReceiverInputDStream dStream = FlumeUtils.createStream
 * -->new FlumeReceiver()-->thread.start()-->new NettyServer().start
 */
public class Flume2SparkStreaming {

	public static void main(String[] args) {

		SparkConf conf = new SparkConf().setMaster("local[4]").setAppName("FlumePushDate2SparkStreaming");
		JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(30));


		//从Flume中接收数据有两种方式
		JavaReceiverInputDStream<SparkFlumeEvent> lines;
		/**
		 * 第一种方式:
		 * Flume 设置avro sink,push数据到spark streaming
		 * FlumeUtils.createStream最终会返回一个FlumeReceiver[SparkFlumeEvent]
		 * 该类继承自ReceiverInputDStream,用于来接收Flume事件,该类启new NettyServer()来启动一个线程
		 */
		// lines = FlumeUtils.createStream(jsc, "hadoop", 9999);

		/**
		 * 第二种方式:
		 * FLume 设置sink类型为:org.apache.spark.streaming.flume.sink.SparkSink
		 * 依照官网添加jar包或添加pom依赖
		 *
		 */
		lines = FlumeUtils.createPollingStream(jsc, "hadoop", 9999);


		//TransForm
		JavaDStream<String> words = lines.flatMap(new FlatMapFunction<SparkFlumeEvent, String>() {

			@Override
			public Iterable<String> call(SparkFlumeEvent event) throws Exception {
				String line = new String(event.event().getBody().array());
				return Arrays.asList(line.split(" "));
			}
		});

		JavaPairDStream<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {

			@Override
			public Tuple2<String, Integer> call(String word) throws Exception {
				return new Tuple2<>(word, 1);
			}
		});
		JavaPairDStream<String, Integer> wordsCount = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {

			@Override
			public Integer call(Integer v1, Integer v2) throws Exception {
				return v1 + v2;
			}
		});

		wordsCount.print();
		jsc.start();
		jsc.awaitTermination();
		jsc.close();
	}


}
