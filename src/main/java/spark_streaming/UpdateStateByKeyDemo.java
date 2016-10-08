package spark_streaming;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Optional;
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

/**
 * UpdateStateByKeyDemo
 *
 * 将新的数据value更新key原有的value数据,更新stage,返回更新后的数据
 */
public class UpdateStateByKeyDemo {

	public static void main(String[] args) {


		SparkConf conf = new SparkConf().setMaster("local[2]").
				setAppName("UpdateStateByKeyDemo");
		JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(5));

		/**
		 * updateByKey操作必须开启checkpoint机制
		 * 生产环境下一般放在HDFS中
		 */
		jsc.checkpoint("target/checkpoint");


		JavaReceiverInputDStream<String> lines = jsc.socketTextStream("hadoop", 9999);
		JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() { // 如果是Scala，由于SAM转换，所以可以写成val
			// words
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

		/**
		 * 在这里是通过updateStateByKey来以Batch Interval为单位来对历史状态进行更新，这是功能上的一个非常大的改进，
		 * 否则的话需要完成同样的目的，就可能需要把数据保存在Redis、Tachyon或者HDFS或者HBase或者数据库中来不断的完成同样一个key的State更新，
		 * 如果你对性能有极为苛刻的要求，且数据量特别大的话，可以考虑把数据放在分布式的Redis或者Tachyon内存文件系统中；
		 *
		 * 当然从Spark1.6.x开始可以尝试使用mapWithState，Spark2.X后mapWithState应该非常稳定了。
		 */
		JavaPairDStream<String, Integer> wordsCount = pairs
				.updateStateByKey(new Function2<List<Integer>, Optional<Integer>, Optional<Integer>>() {
					@Override
					// values为新传进来的数据,state为曾经的数据,这里做累加聚合
					public Optional<Integer> call(List<Integer> values, Optional<Integer> state)
							throws Exception {
						Integer updatedValue = 0;
						if (state.isPresent()) {
							updatedValue = state.get();
						}
						for (Integer value : values) {
							updatedValue += value;
						}
						//返回com.google.common.base.Optional类型,泛型为Integer
						return Optional.of(updatedValue);
					}
				});

        /*
         *
         * 此处的print并不会直接出发Job的执行，因为现在的一切都是在Spark Streaming框架的控制之下的，对于Spark
         * Streaming
         * 而言具体是否触发真正的Job运行是基于设置的Duration时间间隔的
         * 诸位一定要注意的是Spark Streaming应用程序要想执行具体的Job，对Dtream就必须有output Stream操作，
         * output
         * Stream有很多类型的函数触发，类print、saveAsTextFile、saveAsHadoopFiles等，最为重要的一个
         * 方法是foraeachRDD,因为Spark
         * Streaming处理的结果一般都会放在Redis、DB、DashBoard等上面，foreachRDD
         * 主要就是用用来完成这些功能的，而且可以随意的自定义具体数据到底放在哪里！！！
         */

		wordsCount.print();


		jsc.start();
		jsc.awaitTermination();
		jsc.close();

	}
}