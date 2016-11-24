package spark_streaming.demo;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.*;

/**
 * MockAdClickedStats
 */
public class MockAdClickedStats {
	public static void main(String[] args) {

		final Random random = new Random();
		final String[] provinces = new String[]{"Guangdong", "Zhejiang", "Jiangsu", "Fujian"};
		final Map<String, String[]> cities = new HashMap<String, String[]>();
		cities.put("Guangdong", new String[]{"Guangzhou", "Shenzhen", "DongGuan"});
		cities.put("Zhejiang", new String[]{"Hangzhou", "Wenzhou", "Ningbo"});
		cities.put("Jiangsu", new String[]{"Nanjing", "Suzhou", "Wuxi"});
		cities.put("Fujian", new String[]{"Fuzhou", "Xiamen", "Sanming"});


		/*String[] ips = new String[]{
				"192.168.0.3", "192.168.0.4", "192.168.0.5", "192.168.0.6", "192.168.0.7", "192.168.0.8",
				"192.168.0.9", "192.168.0.10", "192.168.0.11", "192.168.0.12", "192.168.0.13", "192.168.0.14",
		};*/

		/**
		 * Kafka相关基本配置信息
		 */
		Properties kafkaConf = new Properties();
		kafkaConf.put("serializer.class", "kafka.serializer.StringEncoder");
		kafkaConf.put("metadata.broker.list", "hadoop:9092");
		ProducerConfig producerConfig = new ProducerConfig(kafkaConf);

		//key为id的索引这里类型Integer,value为实际数据
		final Producer<Integer, String> producer = new Producer<Integer, String>(producerConfig);
		//producer发消息
		new Thread(new Runnable() {
			@Override
			public void run() {

				while (true) {

					//格式:timestamp,ip,userID,adID,province,city
					long timestamp = new Date().getTime();
					String ip = random.nextInt(254) + "." + random.nextInt(254) + "." + random.nextInt(254) + "." + random.nextInt(254);//可以采用网络上免费提供的ip库
					int userID = random.nextInt(10000);
					int adID = random.nextInt(100);
					String province = provinces[random.nextInt(3)];
					String city = cities.get(province)[random.nextInt(2)];

					String clickedAd = timestamp + "\t" + ip + "\t" + userID + "\t" + adID + "\t" + province + "\t" + city;

					producer.send(new KeyedMessage<Integer, String>("adClicked", clickedAd));

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

	}
}
