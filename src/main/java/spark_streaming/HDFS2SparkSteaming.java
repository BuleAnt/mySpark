package spark_streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.api.java.JavaStreamingContextFactory;
import scala.Tuple2;

import java.util.Arrays;

/**
 * HDFS2SparkSteaming wordCount
 * checkpoint相关编程,模型如下:
 * new StreamingContextFactory(path,conf){
 * new ssc;
 * ssc.checkpoint(path)
 * return ssc;
 * }
 * ssc = StreamingContext.getOrCreate(checkpointDirectory,StreamingContextFactory)
 * textFileStream-->DStream-->wordCount-->print
 */
public class HDFS2SparkSteaming {
	public static void main(String[] args) {
		final SparkConf conf = new SparkConf().setAppName("WordCountOnline");
		conf.setMaster("local[2]");
		//conf.setMaster("spark://hadoop:7077");
		//JavaStreamingContext jsc = new JavaStreamingContext(conf,Durations.seconds(5));
		final String checkpointDirectory = "hdfs://hadoop:9000/user/spark/streaming/checkpoint";

		//实现一个JavaStreamingContextFactory;create方法可以创建JavaStreamingContext
		JavaStreamingContextFactory factory = new JavaStreamingContextFactory() {

			@Override
			public JavaStreamingContext create() {
				return createContext(checkpointDirectory, conf);
			}
		};

		/**
		 * getOrCreate可以在失败中回复Driver，不过还需要指定Driver这个程序运行在Cluster，
		 * 并且提交应用程序的时候制定--supervise
		 * getOrCreate能够从checkpoint中恢复StreamingContext,
		 * 如果提供的checkpoint的path存在,StreamingContext将从该path的数据重启,
		 * 然后通过提供的之前创建SparkSteaming的factory创建SparkSteaming
		 */
		JavaStreamingContext jsc = JavaStreamingContext.getOrCreate(checkpointDirectory, factory);

		// 此处没有Receiver，SparkStreaming应用程序只是按照时间间隔监控目录下每个Batch新增的内容
		// （把新增的）作为RDD的数据来源生产原始RDD
		JavaDStream<String> lines = jsc.textFileStream("hdfs://hadoop:9000/user/spark/input");

		JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
			@Override
			public Iterable<String> call(String line) throws Exception {
				return Arrays.asList(line.split(" "));
			}
		});

		// DStream进行Transformation级别的处理
		// word.map()-->(word,1)
		JavaPairDStream<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
			@Override
			public Tuple2<String, Integer> call(String word) throws Exception {
				return new Tuple2<>(word, 1);
			}
		});

		JavaPairDStream<String, Integer> wordsCount = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
			// 对相同的key,进行Value的累计（包括Local和Reduce级别同时Reduce）
			@Override
			public Integer call(Integer v1, Integer v2) throws Exception {
				return v1 + v2;
			}
		});

		wordsCount.print();
		/**
		 * start后开启一个线程启动Driver，当然其内部有消息循环体，用于接收应用程序本身或者Executor中的消息
		 */
		jsc.start();
		jsc.awaitTermination();
		jsc.close();
	}

	private static JavaStreamingContext createContext(String checkpointDirectory, SparkConf conf) {
		System.out.println("Creating new context");
		JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(5));

		// 框架默认会对DSteam进行checkpoint
		// 一方面保持容错，一方面还保持状态，在开始和结束的时候每个batch都会进行checkpoint
		ssc.checkpoint(checkpointDirectory);// 通过checkpoint可以指定一个checkpoint的路径
		return ssc;

	}
}
