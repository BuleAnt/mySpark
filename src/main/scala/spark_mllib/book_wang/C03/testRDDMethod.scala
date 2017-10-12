package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-1 aggregate
  * 从源码看 aggregate 定义了几个泛型参数,
  * U是数据类型,可以掺入任意类型的数据.seqOp是给定的计算方法,输出结果要求也是U类型,
  * 而第二个combOp是合并方法,将第一个计算方法得出的结果与源码中zeroValue进行合并.
  * 需要支出的是: zeroValue并不是一个固定的值,二是一个没有实际意义的"空值"
  *
  */
object testRDDMethod {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("testRDDMethod")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val arr = sc.parallelize(Array(1, 2, 3, 4, 5, 6))
    //输入数组数据集
    val result = arr.aggregate(0)(math.max(_, _), _ + _) //使用aggregate方法
    println(result) //打印结果
  }
}
