package spark_mllib.book_wang.C03

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 3-4 cache
  * cache讲数据内容计算并保存在计算节点的内存中
  * 在Lazy模式中,数据在编译和未使用时是不进行计算的,而仅仅保存其存储地址.
  * 只有在action方法到来时才正式计算.
  * 这样做的好处在于可以极大地减少存储空间,从而提高利用率,
  * 而有时必须要数据进行计算,此时需要通过cache方法
  */
object CacheTest {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("CacheTest")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val arr = sc.parallelize(Array("abc", "b", "c", "d", "e", "f")) //设定数据集
    println(arr) //打印结果
    println("----------------") //分隔符
    println(arr.cache()) //打印结果
  }
}

