import org.apache.spark.SparkContext._
import org.apache.spark._

/**
  * Created by xiaojun on 2015/3/9.
  */
object HiveCollectSet {
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

		val seqOp = (a: List[String], b: User) => (b.vtm :: a).distinct
		val combOp = (a: List[String], b: List[String]) => {
			(a ::: b).distinct
		}

		println("-----------------------------------------")
		val rdd3 = rdd2.aggregateByKey(List[String]())(seqOp, combOp).mapValues(l => l.mkString(","))
		rdd3.collect.foreach(r => println(r._2))
		println("-----------------------------------------")
		sc.stop()
	}

}
