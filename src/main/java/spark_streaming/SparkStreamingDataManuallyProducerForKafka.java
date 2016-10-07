package spark_streaming;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * SparkStreamingDataManuallyProducerForKafka
 */
public class SparkStreamingDataManuallyProducerForKafka extends Thread {

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

	public SparkStreamingDataManuallyProducerForKafka(String topic) {
//      格式 化器
		dateToday = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		this.topic = topic;
		random = new Random();
//        设置参数
		Properties conf = new Properties();
		conf.put("metadata.broker.list", "Master:9092,Worker1:9092,Worker2:9092");
//        Kafka在生成的时候有个key,key的序列化器，这是默认的方式
		conf.put("serializer.class", "kafka.serializer.StringEncoder");
//        基于这些内容，创建构造器Produce
		producerForKafka = new Producer<Integer, String>(new ProducerConfig(conf));
	}

	//对于线程而言得有run方法，我们得复写这个run方法
	@Override
	public void run() {
		int counter = 0;
		while (true) {
			counter++;
			String userLog = userlogs();
			System.out.println("product:" + userLog);
//          topic ,就是这个消息属于哪个topic，这个时候就一直发消息
			producerForKafka.send(new KeyedMessage<Integer, String>(topic, userLog));

			if (0 == counter % 500) {
				counter = 0;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	public static void main(String[] args) {

		new SparkStreamingDataManuallyProducerForKafka("UserLogs").start();

	}


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


		userLogBuffer.append(dateToday)
				.append("\t")
				.append(timestamp)
				.append("\t")
				.append(userID)
				.append("\t")
				.append(pageID)
				.append("\t")
				.append(channel)
				.append("\t")
				.append(action)
				.append("\n");


		return userLogBuffer.toString();

	}

}
