package spark_streaming;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * KafkaProducerDataManually
 * 论坛数据自动生成代码,该生成的数据会作为Producer的方式发送给Kafka,
 * 然后SparkStreaming程序会从Kafka中在线Pull到论坛或者网站的用户在线行为信息,
 * 进而进行多维度的在线分析
 * 格式:
 * data:日期
 * timestamp:时间戳
 * userID:用户ID
 * pageID:页面ID
 * channelID:板块ID
 * action:点击和注册
 */
public class KafkaProducerDataManually extends Thread {

	//具体的论坛频道
	private static String[] channelNames = new String[]{
			"Spark", "Scala", "Kafka", "Flink", "Hadoop", "Storm",
			"Hive", "Impala", "HBase", "ML"
	};
	//用户的两种行为模式
	private static String[] actionNames = new String[]{"View", "Register"};

	private String topic; //发送给Kafka的数据的类别
	private Producer<Integer, String> producerForKafka;//他这个是索引加内容

	private static String dateToday;
	private static Random random;


	public static void main(String[] args) {

		new KafkaProducerDataManually("UserLogs").start();

	}


	public KafkaProducerDataManually(String topic) {
		//日期格式化
		dateToday = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		this.topic = topic;
		random = new Random();

		//设置参数
		Properties conf = new Properties();
		conf.put("metadata.broker.list", "hadoop:9092");
		//Kafka在生成的时候有个key,key的序列化器，这是默认的方式
		conf.put("serializer.class", "kafka.serializer.StringEncoder");
		//基于这些内容，构造器创建Kafka的Produce
		producerForKafka = new Producer<>(new ProducerConfig(conf));
	}

	@Override
	public void run() {
		int counter = 0;
		while (true) {
			counter++;
			String userLog = userlogs();
			System.out.println("product:" + userLog);

			// 通过topic和userLog构建KeyedMessage,Producer将该消息发送出去
			producerForKafka.send(new KeyedMessage<Integer, String>(topic, userLog));

			if (0 == counter % 500) {
				counter = 0;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


	//动态生成一条日志
	private static String userlogs() {

		StringBuffer userLogBuffer = new StringBuffer("");
		int[] unregisteredUsers = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
		long timestamp = new Date().getTime();
		Long userID = 0L;
		long pageID = 0L;

		//随机生成的用户ID 8分之1 没注册的
		if (unregisteredUsers[random.nextInt(8)] == 1) {
			userID = null;
		} else {
			userID = (long) random.nextInt((int) 2000);
		}


		//随机生成的页面ID 随机生成0-2000
		pageID = random.nextInt(2000);

		//随机生成Channel
		String channel = channelNames[random.nextInt(10)];

		//随机生成action行为
		String action = actionNames[random.nextInt(2)];

		userLogBuffer.append(dateToday).append("\t")
				.append(timestamp).append("\t")
				.append(userID).append("\t")
				.append(pageID).append("\t")
				.append(channel).append("\t")
				.append(action);
		//.append("\n");

		return userLogBuffer.toString();

	}

}
