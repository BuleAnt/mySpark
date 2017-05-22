package rdd
import org.apache.spark.{SparkContext, SparkConf}
/**
  * Created by noah on 17-5-22.
  */
object CountByValue{
  def main(args: Array[String]) {
    val conf = new SparkConf()                                       //创建环境变量
      .setMaster("local")                                               //设置本地化处理
      .setAppName("countByValue ")                                    	  //设定名称
    val sc = new SparkContext(conf)						       //创建环境变量实例
    val arr = sc.parallelize(Array(1,2,3,4,5,6))						  //创建数据集
    val result = arr.countByValue()								  //调用方法计算个数
    result.foreach(print)                                              //打印结果
  }
}