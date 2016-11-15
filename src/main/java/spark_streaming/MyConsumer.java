package spark_streaming;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MyConsumer {
	public static void main(String[] args) {
		String topic = "lxw1234";
		ConsumerConnector consumer = Consumer.createJavaConsumerConnector(createConsumerConfig());
		Map<String, Integer> topicCountMap = new HashMap<>();
		topicCountMap.put(topic, new Integer(1));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		KafkaStream<byte[], byte[]> stream = consumerMap.get(topic).get(0);
		ConsumerIterator<byte[], byte[]> it = stream.iterator();
		while (it.hasNext()) {
			MessageAndMetadata<byte[], byte[]> mam = it.next();
			String msg = new String(mam.message());
			String cookieid = msg.split("\\|")[1];
			int testPartition = Math.abs(cookieid.hashCode()) % 4;
			System.out.println("consume: Partition [" + mam.partition() + "] testPartition [" + testPartition + "] Message: [" + new String(mam.message()) + "] ..");
		}

	}

	private static ConsumerConfig createConsumerConfig() {
		Properties props = new Properties();
		props.put("group.id", "group_lxw_test");
		props.put("zookeeper.connect", "127.0.0.133:2182");
		props.put("zookeeper.session.timeout.ms", "4000");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");
		props.put("auto.offset.reset", "smallest");
		return new ConsumerConfig(props);
	}
}