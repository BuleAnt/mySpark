package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-2 parallelize
  * parallelize是sparkContext内部方法,将数据并行化
  * 默认第二个参数为1 ,修改testRDDMethod中的parallelize并行度为2
  * 可以看到运行结果
  */
object testRDDMethod2 {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("testRDDMethod2 ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val arr = sc.parallelize(Array(1, 2, 3, 4, 5, 6), 2)
    //输入数组数据集
    val result = arr.aggregate(0)(math.max, _ + _) //使用aggregate方法
    println(result) //打印结果
  }
}
