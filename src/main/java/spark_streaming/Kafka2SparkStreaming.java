package spark_streaming;

import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.*;

/**
 * Kafka2SparkStreaming
 * ReceiverInputDStream dSteam = KafkaUtils.createStream
 */
public class Kafka2SparkStreaming {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setMaster("local[4]").setAppName("Kafka2SparkStreaming");
		JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(10));


		//**********************************************************************************//
		//*************通过KafkaReceiver或ReliableKafkaReceiver pulls messages***************//
		//**********************************************************************************//
		Map<String, Integer> topicConsumerConcurrency = new HashMap<>();
		topicConsumerConcurrency.put("HelloKafka", 1);//这里2个的话是指2个接受的线程
		/**
		 * KafkaUtils.createStream通过zk获取offset来从Kafka中获取消息.具体参数含义：
		 *
		 * jssc: JavaStreamingContext,是StreamingContext实例，
		 * zkQuorum: String,是zookeeper集群信息（接受Kafka数据的时候会从zookeeper中获取Offset等元数据信息）
		 * groupId: String,是Consumer Group
		 * topics: JMap[String, JInt],是消费的Topic以及并发读取Topic中Partition的线程数,每个partition在在对应的线程中消费
		 *
		 * 返回值JavaPairReceiverInputDStream[String, String]:其中DStream中k/v为Kafka message的key和value
		 */
		JavaPairReceiverInputDStream<String, String> lines = KafkaUtils.createStream(jsc,
				"hadoop:2181", "MyFirstConsumerGrou", topicConsumerConcurrency);

		//**********************************************************************************//
		//****通过DirectKafkaInputDStream directly pulls messages exactly once **************//
		//**********************************************************************************//
		Map<String, String> kafkaParameters = new HashMap<>();
		//这里的kafka
		kafkaParameters.put("metadata.broker.list", "hadoop:9092");
		Set<String> topics = new HashSet<>();
		topics.add("HelloKafka");
		/**
		 *  KafkaUtils.createDirectStream是直接通过broker.list获取Kafka消息,参数如下:
		 *
		 *  jssc: JavaStreamingContext,是StreamingContext实例，
		 *  keyClass: Class[K],valueClass: Class[V],为Kafka message的key和value
		 *  keyDecoderClass: Class[KD],valueDecoderClass: Class[VD],为Kafka messaged的编码解码器
		 *  kafkaParams: JMap[String, String],是kafka的metadata.broker.list及kafka server的host和ip
		 *  topics: JSet[String],是Topic及其并发Partition的线程数
		 *
		 *  返回值为JavaPairInputDStream,而不是JavaPairReceiverInputDStream
		 */
		JavaPairInputDStream<String, String> directly = KafkaUtils.createDirectStream(jsc,
						String.class, String.class,
						StringDecoder.class, StringDecoder.class,
						kafkaParameters, topics);
		//**********************************************************************************//

		// 对初始的DTStream进行Transformation级别处理
		// 这里FlatMapFunction函数中输入类型为Tuple2<String, String>,我们需要获取的是kafka消息的value
		JavaDStream<String> words = directly.flatMap(new FlatMapFunction<Tuple2<String, String>, String>() {

			@Override
			public Iterable<String> call(Tuple2<String, String> tuple) throws Exception {
				return Arrays.asList(tuple._2.split(" "));
			}
		});

		JavaPairDStream<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {

			@Override
			public Tuple2<String, Integer> call(String word) throws Exception {
				return new Tuple2<>(word, 1);
			}
		});

		JavaPairDStream<String, Integer> wordsCount = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {

			private static final long serialVersionUID = 1L;

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