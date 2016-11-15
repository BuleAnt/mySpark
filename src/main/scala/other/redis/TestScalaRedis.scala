package other.redis

import redis.clients.jedis.Jedis

/**
  * Created by hadoop on 16-11-16.
  */
object TestScalaRedis {
	def main(args: Array[String]) {
		val jr:Jedis = null;
		try{
			val jr = new Jedis("192.168.110.127", 6379);
			//            jr.auth("test123");
			System.out.println(jr.ping());
			System.out.println(jr.isConnected() && jr.ping().equals("PONG"));
			val key = "bjsxtgaga";

			jr.set(key,"hello redis!");

			val v = jr.get(key);

			val k2 = "count";

			jr.incr(k2);

			jr.incr(k2);

			jr.lpush("gaga", "gaga");
			System.out.println(v);

			System.out.println(jr.get(k2));

		}catch {
			case t => // todo: handle error
				t.printStackTrace()
		}finally {
			if (jr!=null) {
				jr.disconnect()
			}
		}



	}
}
