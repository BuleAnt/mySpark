package spark_streaming.demo

import java.text.SimpleDateFormat
import java.util.Date

import kafka.serializer.StringDecoder
import net.sf.json.JSONObject
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.SQLContext
import redis.clients.jedis.JedisPool

object IPLoginAnalytics {

	def main(args: Array[String]): Unit = {
		val sdf = new SimpleDateFormat("yyyyMMdd")
		var masterUrl = "local[2]"
		if (args.length > 0) {
			masterUrl = args(0)
		}

		// Create a StreamingContext with the given master URL
		val conf = new SparkConf().setMaster(masterUrl).setAppName("IPLoginCountStat")
		val ssc = new StreamingContext(conf, Seconds(5))

		// Kafka configurations
		val topics = Set("test_data3")
		val brokers = "dn1:9092,dn2:9092,dn3:9092"
		val kafkaParams = Map[String, String](
			"metadata.broker.list" -> brokers, "serializer.class" -> "kafka.serializer.StringEncoder")

		val ipLoginHashKey = "mf::ip::login::" + sdf.format(new Date())

		// Create a direct stream
		val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

		val events = kafkaStream.flatMap(line => {
			val data = JSONObject.fromObject(line._2)
			Some(data)
		})

		def func(iter: Iterator[(String, String)]): Unit = {
			while (iter.hasNext) {
				val item = iter.next()
				println(item._1 + "," + item._2)
			}
		}

		events.foreachRDD { rdd =>
			// Get the singleton instance of SQLContext
			val sqlContext = SQLContextSingleton.getInstance(rdd.sparkContext)
			import sqlContext.implicits._
			// Convert RDD[String] to DataFrame
			val wordsDataFrame = rdd.map(f => Record(f.getString("_plat"), f.getString("_uid"), f.getString("_tm"), f.getString("country"), f.getString("location"))).toDF()

			// Register as table
			wordsDataFrame.registerTempTable("events")
			// Do word count on table using SQL and print it
			val wordCountsDataFrame = sqlContext.sql("select location,count(distinct plat,uid) as value from events where from_unixtime(tm,'yyyyMMdd') = '" + sdf.format(new Date()) + "' group by location")
			var results = wordCountsDataFrame.collect().iterator

			/**
			  * Internal Redis client for managing Redis connection {@link Jedis} based on {@link RedisPool}
			  */
			object InternalRedisClient extends Serializable {

				@transient private var pool: JedisPool = null

				def makePool(redisHost: String, redisPort: Int, redisTimeout: Int,
				             maxTotal: Int, maxIdle: Int, minIdle: Int): Unit = {
					makePool(redisHost, redisPort, redisTimeout, maxTotal, maxIdle, minIdle, true, false, 10000)
				}

				def makePool(redisHost: String, redisPort: Int, redisTimeout: Int,
				             maxTotal: Int, maxIdle: Int, minIdle: Int, testOnBorrow: Boolean,
				             testOnReturn: Boolean, maxWaitMillis: Long): Unit = {
					if (pool == null) {
						val poolConfig = new GenericObjectPoolConfig()
						poolConfig.setMaxTotal(maxTotal)
						poolConfig.setMaxIdle(maxIdle)
						poolConfig.setMinIdle(minIdle)
						poolConfig.setTestOnBorrow(testOnBorrow)
						poolConfig.setTestOnReturn(testOnReturn)
						poolConfig.setMaxWaitMillis(maxWaitMillis)
						pool = new JedisPool(poolConfig, redisHost, redisPort, redisTimeout)

						val hook = new Thread {
							override def run = pool.destroy()
						}
						sys.addShutdownHook(hook.run)
					}
				}

				def getPool: JedisPool = {
					assert(pool != null)
					pool
				}
			}

			// Redis configurations
			val maxTotal = 10
			val maxIdle = 10
			val minIdle = 1
			val redisHost = "dn1"
			val redisPort = 6379
			val redisTimeout = 30000
			InternalRedisClient.makePool(redisHost, redisPort, redisTimeout, maxTotal, maxIdle, minIdle)
			val jedis = InternalRedisClient.getPool.getResource
			while (results.hasNext) {
				var item = results.next()
				var key = item.getString(0)
				var value = item.getLong(1)
				jedis.hincrBy(ipLoginHashKey, key, value)
			}
		}

		ssc.start()
		ssc.awaitTermination()

	}
}

/** Case class for converting RDD to DataFrame */
case class Record(plat: String, uid: String, tm: String, country: String, location: String)

/** Lazily instantiated singleton instance of SQLContext */
object SQLContextSingleton {

	@transient private var instance: SQLContext = _

	def getInstance(sparkContext: SparkContext): SQLContext = {
		if (instance == null) {
			instance = new SQLContext(sparkContext)
		}
		instance
	}
}