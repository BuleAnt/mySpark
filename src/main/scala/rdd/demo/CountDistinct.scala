package rdd.demo

import org.apache.spark._

/**
  * Created by xiaojun on 2015/3/9.
  */
object CountDistinct {
	def main(args: Array[String]) {

		case class User(id: String, name: String, vtm: String, url: String)
		//val rowkey = (new RowKey).evaluate(_)
		val HADOOP_USER = "hdfs"
		// 设置访问spark使用的用户名
		System.setProperty("user.name", HADOOP_USER);
		// 设置访问hadoop使用的用户名
		System.setProperty("HADOOP_USER_NAME", HADOOP_USER);

		val conf = new SparkConf().setAppName("wordcount").setMaster("local").setExecutorEnv("HADOOP_USER_NAME", HADOOP_USER)
		val sc = new SparkContext(conf)
		val data = sc.textFile("test.txt")
		val rdd1 = data.map(line => {
			val r = line.split(",")
			User(r(0), r(1), r(2), r(3))
		})
		val rdd2 = rdd1.map(r => ((r.id, r.name), r))

		val seqOp = (a: (Int, List[String]), b: User) => a match {
			case (0, List()) => (1, List(b.url))
			case _ => (a._1 + 1, b.url :: a._2)
		}

		val combOp = (a: (Int, List[String]), b: (Int, List[String])) => {
			(a._1 + b._1, a._2 ::: b._2)
		}

		println("-----------------------------------------")
		val rdd3 = rdd2.aggregateByKey((0, List[String]()))(seqOp, combOp).map(a => {
			(a._1, a._2._1, a._2._2.distinct.length)
		})
		rdd3.collect.foreach(println)
		println("-----------------------------------------")
		sc.stop()
	}

}
