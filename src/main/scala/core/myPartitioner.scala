package core

import org.apache.spark.{Partitioner, SparkConf, SparkContext}

/**
  * 有时自己的业务需要自己实现spark的分区函数
  * 实现的功能是根据key值的最后一位数字，写到不同的文件
  */
//自定义分区类，需继承Partitioner类
class myPartitioner(numParts: Int) extends Partitioner {
  //覆盖分区数
  override def numPartitions: Int = numParts

  //覆盖分区号获取函数
  override def getPartition(key: Any): Int = {
    key.toString.toInt % 10
  }
}

object Test {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("testPartition").setMaster("local[4]")
    val sc = new SparkContext(conf)
    //模拟5个分区的数据
    val data = sc.parallelize(1 to 10, 5)

    //根据尾号转变为10个分区，分写到10个文件
    data.map((_, 1)).partitionBy(new myPartitioner(10)).saveAsTextFile("/home/noah/templates/spark")
  }
}