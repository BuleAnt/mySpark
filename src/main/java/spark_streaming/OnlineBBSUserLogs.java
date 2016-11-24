package spark_streaming;

import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.*;

/**
 * OnlineBBSUserLogs
 * 格式:
 * data:日期
 * timestamp:时间戳
 * userID:用户ID
 * pageID:页面ID
 * channelID:板块ID
 * action:点击和注册
 */
public class OnlineBBSUserLogs {

	public static void main(String[] args) {

		SparkConf conf = new SparkConf().setMaster("local[4]").
				setAppName("OnlineBBSUserLogs");
		JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(5));

		Map<String, String> kafkaParameters = new HashMap<String, String>();
		kafkaParameters.put("metadata.broker.list", "hadoop:9092");

		// topics不能重复所以用HashSet
		Set<String> topics = new HashSet<String>();
		topics.add("UserLogs");

		// KafkaUtils.createDirectStream方式创建DStream
		// 此处导这个包import kafka.serializer.StringDecoder;
		// Producer是StringEncoder消费者是StringDecoder
		JavaPairInputDStream<String, String> lines = KafkaUtils.createDirectStream(jsc,
				String.class, String.class,
				StringDecoder.class, StringDecoder.class,
				kafkaParameters, topics);

		// 在线PV计算
		onLinePagePV(lines);
		// 在线UV计算
		//onLineUV(lines);
		// 在线注册人数
		//onLineRegistered(lines);
		// 在线计算跳出率
		//onLineJumped(lines);
		// 在线计算不同模块的pv
		//onLineChannelPV(lines);

		jsc.start();
		jsc.awaitTermination();
		jsc.close();

	}


	private static void onLinePagePV(JavaPairInputDStream<String, String> lines) {
		//filter出action为View的记录
		JavaPairDStream<String, String> logsDStream = lines.filter(new Function<Tuple2<String, String>, Boolean>() {
			@Override
			public Boolean call(Tuple2<String, String> v1) throws Exception {
				// 分割之后生成一个数组
				String[] logs = v1._2.split("\t");

				String action = logs[5];
				return "View".equals(action.trim());
			}
		});

		// (meta,message)-->map-->(page,1)
		JavaPairDStream<Long, Long> pairs = logsDStream.mapToPair(new PairFunction<Tuple2<String, String>, Long, Long>() {
			@Override
			public Tuple2<Long, Long> call(Tuple2<String, String> t) throws Exception {
				//    key 是Kafka给的我们不要
				String[] logs = t._2.split("\t");

				Long pageId = Long.valueOf(logs[3]);
				//每次计数为1
				return new Tuple2<Long, Long>(pageId, 1L);
			}
		});

		//页面的ID和点击次数
		JavaPairDStream wordsCount = pairs.reduceByKey(new Function2<Long, Long, Long>() {
			@Override
			public Long call(Long v1, Long v2) throws Exception {

				return v1 + v2;
			}
		});

		/**
		 * 在企业生产环境下,一般把计算的数据放入Redis或DB中,采用J2EE等技术进行趋势的绘制等,
		 * 这就像动态更新的股票交易一样,来实现在想的监控等
		 */
		wordsCount.print();
	}

	/**
	 * 因为要计算UV,所以需要获得同样的Page的不同User,这个时候就需要去重操作,
	 * DStream目前没有distinct,此时我们就需要求助于DStream的Transform
	 * 在该方法内部直接对RDD进行distinct操作,这样就是实现了用户UserID去重,进而可以计算出UV
	 */
	private static void onLineUV(JavaPairInputDStream<String, String> lines) {

		JavaPairDStream<String, String> logDStream = lines.filter(new Function<Tuple2<String, String>, Boolean>() {
			@Override
			public Boolean call(Tuple2<String, String> v1) throws Exception {
				String[] logs = v1._2.split("\t");
				String action = logs[5];

				return "View".equals(action.trim());
			}
		});

		logDStream.map(new Function<Tuple2<String, String>, String>() {
			@Override
			public String call(Tuple2<String, String> v1) throws Exception {
				String[] logs = v1._2.split("\t");
				Long userID = Long.valueOf(null == logs[2] ? "-1" : logs[2]);
				Long pageID = Long.valueOf(logs[3]);

				return pageID + "_" + userID;
			}
		}).transform(new Function<JavaRDD<String>, JavaRDD<String>>() {
			@Override
			public JavaRDD<String> call(JavaRDD<String> v1) throws Exception {
				v1.distinct();
				return v1;
			}
		}).mapToPair(new PairFunction<String, Long, Long>() {
			@Override
			public Tuple2<Long, Long> call(String s) throws Exception {
				String[] logs = s.split("_");
				Long pageID = Long.valueOf(logs[0]);
				return new Tuple2<Long, Long>(pageID, 1L);
			}
		}).print();
	}


	private static void onLineRegistered(JavaPairInputDStream<String, String> lines) {
		lines.filter(new Function<Tuple2<String, String>, Boolean>() {
			@Override
			public Boolean call(Tuple2<String, String> v1) throws Exception {
				String[] logs = v1._2.split("\t");
				System.out.println(Arrays.asList(logs));
				String action = logs[5];

				return "Register".equals(action.trim());
			}
		}).count().print();
	}


	private static void onLineJumped(JavaPairInputDStream<String, String> lines) {
		lines.filter(new Function<Tuple2<String, String>, Boolean>() {
			@Override
			public Boolean call(Tuple2<String, String> v1) throws Exception {
				String[] logs = v1._2.split("\t");
				String action = logs[5];

				return "View".equals(action.trim());
			}
		}).mapToPair(new PairFunction<Tuple2<String, String>, Long, Long>() {
			@Override
			public Tuple2<Long, Long> call(Tuple2<String, String> t) throws Exception {
				String[] logs = t._2.split("\t");

				Long pageId = Long.valueOf(logs[3]);
				//每次计数为1
				return new Tuple2<Long, Long>(pageId, 1L);
			}
		}).reduceByKey(new Function2<Long, Long, Long>() {
			@Override
			public Long call(Long v1, Long v2) throws Exception {

				return v1 + v2;
			}
		}).filter(new Function<Tuple2<Long, Long>, Boolean>() {
			@Override
			public Boolean call(Tuple2<Long, Long> v1) throws Exception {

				return 1 == v1._2;
			}
		}).count().print();
	}

	private static void onLineChannelPV(JavaPairInputDStream<String, String> lines) {
		//filter出action为View的记录
		lines.mapToPair(new PairFunction<Tuple2<String, String>, String, Long>() {
			@Override
			public Tuple2<String, Long> call(Tuple2<String, String> t) throws Exception {
				String[] logs = t._2.split("\t");

				return new Tuple2<String, Long>(String.valueOf(logs[4]), 1L);//(channelID,1)
			}
		}).reduceByKey(new Function2<Long, Long, Long>() {
			@Override
			public Long call(Long v1, Long v2) throws Exception {

				return v1 + v2;
			}
		}).print();
	}

}
