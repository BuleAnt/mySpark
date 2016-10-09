package spark_streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 使用Scala并发集群运行的Spark来实现在线热搜词
  *
  * 背景描述：
  * 在社交网络（例如微博），电子商务（例如京东），热搜词（例如百度）等
  * 人们核心关注的内容之一就是我所关注的内容中大家正在最关注什么或者说当前的热点是什么，
  * 这在市级企业级应用中是非常有价值，
  * 例如我们关心过去30分钟大家正在热搜什么，并且每5分钟更新一次，这就使得热点内容是动态更新的，当然更有价值。
  * Yahoo（是Hadoop的最大用户）被收购，因为没做到实时在线处理
  *
  * 实现技术：
  * Spark Streaming（在线批处理）提供了滑动窗口的技术来支撑实现上述业务背景，
  * 我们您可以使用reduceByKeyAndWindow操作来做具体实现
  * Storm也可以,但是非常麻烦,因为Storm是一条条数据进行处理
  */
object HottestItems {

	def main(args: Array[String]) {

		val conf = new SparkConf()
				.setAppName("OnlineHottestItems")
				.setMaster("local[4]")

		/**
		  * 此处设置 Batch Interval 是在spark Streaming 中生成基本Job的单位，
		  * 窗口和滑动时间间隔一定是该batch Interval的整数倍
		  */
		val ssc = new StreamingContext(conf, Seconds(5))
		// 如果使用window复用方式,必须做checkpoint
		ssc.checkpoint("target/checkpoint")
		val hottestStream = ssc.socketTextStream("hadoop", 9999)

		/**
		  * 用户搜索的格式简化为 name item,
		  * 在这里我们由于要计算热点内容，所以只需要提取item即可;
		  * 提取出的item通过map转化为（item,1）形式
		  * (name,item)-->map-->(item,1)
		  */
		val searchPair = hottestStream.map(_.split(" ")(1)).map(item => (item, 1)) //热搜词

		/**
		  * 方式一:
		  * reduceByKeyAndWindow(fuc(),seconds,seconds)
		  * 这里每隔20秒更新过去60秒间隔,进行简单的叠加操作;
		  * reduceFunc: (V, V) => V,sum操作
		  * windowDuration: Duration,窗口60秒
		  * slideDuration: Duration,滑动20秒
		  */
		//val hottestDStream = searchPair.reduceByKeyAndWindow(_ + _, Seconds(60), Seconds(20))


		/**
		  * 方式二:
		  * reduceFunc associative reduce function:该函数负责对上个window中加上newRDDs
		  * invReduceFunc inverse reduce function:该函数负责对上个window中减去oldRDDs
		  * width of the window;窗口间隔宽度
		  * sliding interval of the window:窗口的滑动间隔
		  * 这里的reduceByKeyAndWindow复用了上个window重复间隔,必须做checkpoint
		  * 可以看核心源码: ReducedWindowedDStream.compute()
		  */
		val hottestDStream = searchPair.reduceByKeyAndWindow(_ + _, _ - _, Seconds(60), Seconds(20))

		/**
		  * DStream.transform转换为对RDD操作,
		  * 排序并获取3条
		  */
		hottestDStream.transform(hottestItemRDD => {
			val top3 = hottestItemRDD
					.map(pair => (pair._2, pair._1))
					.sortByKey(boolean2Boolean(false))
					.map(pair => (pair._2, pair._1))
					.take(3)

			//打印出排序后的前三
			println(top3.mkString)

			/*	for (item <- top3) {
					println(item)
				}*/
			// 返回的是不做处理的RDD
			hottestItemRDD
		}).print()

		ssc.start()
		ssc.awaitTermination()

	}
}