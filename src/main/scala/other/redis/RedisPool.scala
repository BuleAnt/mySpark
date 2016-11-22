package other.redis

import redis.clients.jedis.Client

/**
  * Created by hadoop on 16-11-16.
  */
class RedisPool {
	def main(args: Array[String]) {
		val jedis = RedisClient.pool.getResource
		jedis.select(1)
	}
}
