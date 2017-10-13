package spark_mllib.book_wang.C10

import org.apache.spark.mllib.fpm.FPGrowth
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 10-2 FP-growth 算法
  *
  */
object FPTree {

  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setMaster("local") //
      .setAppName("FPTree ")
    val sc = new SparkContext(conf)
    //读取数据
    val data = sc.textFile("data/fp.txt")
    //创建FP数实例并设置最小支持度
    val fpg = new FPGrowth().setMinSupport(0.3)
    //val model = fpg.run(data) //创建模型
    val model = fpg.run(data.map(_.split("、"))) //创建模型


  }
}
