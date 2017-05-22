package rdd
import org.apache.spark.{SparkContext, SparkConf}


/**
  * Created by noah on 17-5-22.
  */
object TestReduce2 {
  def main(args: Array[String]) {
    val conf = new SparkConf()                                       //创建环境变量
      .setMaster("local")                                               //设置本地化处理
      .setAppName("testRDDMethod ")                                    	  //设定名称
    val sc = new SparkContext(conf)						       //创建环境变量实例
    var str = sc.parallelize(Array("one","two","three","four","five"))          //创建数据集
    val result = str.reduce(myFun)								  //进行数据拟合
    result.foreach(print)                                              //打印结果
  }

  def myFun(str1:String,str2:String):String = {                          //创建方法
  var str = str1                                                   //设置确定方法
    if(str2.size >= str.size){                                          //比较长度
      str = str2                                                    //替换
    }
    return str                                                     //返回最长的那个字符串
  }
}