package spark_mllib.book_wang.C04

import org.apache.spark.{SparkConf, SparkContext}

/**
  * 4-13 分层抽样
  * 分层抽样是数据提起算法,先将总体的单位按某种特征分为若干次级总体(层),
  * 然后再从每一层内进行单纯随机抽样,组成一个样本的统计学计算方法.
  * 这种方法以前常用语数据量比较大,计算处理非常不方便进行的情况下
  *
  * 一般,抽样讲总体分成互不交叉的层,然后按一定比例从各层独立抽取一定数量的个体,
  * 将各层去除的个体个在一起作为样本,这种抽样方法是一种分层抽样.
  *
  * 在MLlib中使用Map作为分层抽样的数据标记,一般,Map的构成是[k,v]格式,k为数据组,v为数据标签进行处理
  *
  */
object testStratifiedSampling2 {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("testSingleCorrect2 ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val data = sc.textFile("data/sample.txt") //读取数据
      .map(row => {
      //开始处理
      if (row.length == 3) //判断字符数
        (row, 1) //建立对应map
      else (row, 2) //建立对应map
    })
    val fractions: Map[String, Double] = Map("aa" -> 2)
    //设定抽样格式
    val approxSample = data.sampleByKey(withReplacement = false, fractions, 0) //计算抽样样本
    approxSample.foreach(println) //打印结果
  }
}
