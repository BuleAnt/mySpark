package spark_mllib.book_wang.C10

import scala.collection.mutable
import scala.io.Source

/**
  * 10-1 Apriori算法 scala实现单机版
  */
object Apriori {

  def main(args: Array[String]) {
    //设置最小支持度
    val minSup = 2
    //设置可变列表
    val list = new mutable.LinkedHashSet[String]()
    //读取数据集并存储
    Source.fromFile("data/apriori.txt").getLines()
      //将数据存储
      .foreach(str => list.add(str))
    //设置map进行计数
    var map = mutable.Map[String, Int]()
    //计算开始
    list.foreach(strss => {
      //分割数据
      val strs = strss.split("、")
      //开始计算程序
      strs.foreach(str => {
        //判断是否存在
        if (map.contains(str)) {
          //对已有数据+1
          map.update(str, map(str) + 1)
          //将未存储的数据加入
        } else map += (str -> 1)
      })
    })
    //判断最小支持度
    val tmpMap = map.filter(_._2 > minSup)
    //提取清单内容
    val mapKeys = tmpMap.keySet
    //创先辅助List
    val tempList = new mutable.LinkedHashSet[String]()
    //创建连接List
    val conList = new mutable.LinkedHashSet[String]()
    //进行连接准备
    mapKeys.foreach(str => tempList.add(str))
    //开始连接
    tempList.foreach(str => {
      //读取辅助List
      tempList.foreach(str2 => {
        //判断
        if (str != str2) {
          //创建连接字符
          val result = str + "、" + str2
          //添加
          conList.add(result)
        }
      })
    })
    //开始对原始列表进行比对
    conList.foreach(strss => {
      //切分数据
      val strs = strss.split("、")
      //开始计数
      strs.foreach(str => {
        //判断是否包含
        if (map.contains(str)) {
          //对已有数据+1
          map.update(str, map(str) + 1)
          //将未存储的数据加入
        } else map += (str -> 1)
      })
    })
  }
}
