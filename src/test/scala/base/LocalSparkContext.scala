package base

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest._

trait LocalSparkContext extends BeforeAndAfterAll {
  self: Suite =>

  @transient var sc: SparkContext = _

  override def beforeAll() {
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("test")
    sc = new SparkContext(conf)
  }

  override def afterAll() {
    if (sc != null) {
      sc.stop()
    }
  }
}