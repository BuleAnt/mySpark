package other.redis

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.JedisPool

object RedisClient extends Serializable {
	val redisHost = "hadoop"
	val redisPort = 6379
	val redisTimeout = 30000
	/**
	  * Spark集群环境部署Application后,计算的时候会将作用于RDD数据集上的函数（Functions）发送到集群中Worker上的Executor上(（在Spark Streaming中是作用于DStream的操作）
	  * 这些函数操作所作用的对象（Elements）必须是可序列化的，通过Scala也可以使用lazy引用来解决
	  * 否则这些对象（Elements）在跨节点序列化传输后，无法正确地执行反序列化重构成实际可用的对象
	  */
	lazy val pool = new JedisPool(new GenericObjectPoolConfig(), redisHost, redisPort, redisTimeout)

	lazy val hook = new Thread {
		override def run = {
			println("Execute hook thread: " + this)
			pool.destroy()
		}
	}
	sys.addShutdownHook(hook.run)
}