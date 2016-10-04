package core

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.log4j.{Level, Logger}

/**
  * rdd Ops Demo
  */
object RDDDemo {


  def main(args: Array[String]) {
    //Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    //Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)
    val sc = new SparkContext(new SparkConf()
      .setAppName("RDDDemo")
      .setMaster("local"))
    //.set("log4j.rootCategory", "WARN"))

    //parallelize从普通数组创建RDD
    val listRDD = sc.parallelize(List(1, 2, 3, 4))
    listRDD.collect.foreach(println)
    //textFile指定path读取数据创建RDD
    val textRDD = sc.textFile("src/main/resources/text.txt")
    textRDD.collect.foreach(println)

    println("-------------------map->RDD-------------------------")
    //map对RDD中的每个元素都执行一个指定的函数来产生一个新的RDD
    //任何原RDD中的元素在新RDD中都有且只有一个元素与之对应。
    val mapRDD = listRDD.map(x => x * x)
    println(mapRDD.collect.mkString(",")) //toString

    println("-------------------mapPartition->RDD-------------------------")
    //mapPartitions的输入函数是应用于每个分区，把每个分区中的内容作为整体来处理的
    val mapPartitionRDD = listRDD.mapPartitions(toNewList)
    println(mapPartitionRDD.collect.mkString(","))

    println("-------------------mapValues->RDD-------------------------")
    //mapValues的输入函数应用于RDD中Kev-Value的Value，原RDD中的Key保持不变，与新的Value一起组成新的RDD中的元素。
    val partitionRDD = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", " eagle"), 2)
    val mapValRDD = partitionRDD.map(x => (x.length, x)).mapValues("x" + _ + "x")
    println(mapValRDD.collect().mkString)

    println("-------------------mapWith->RDD-------------------------")
    //mapWith是map的另外一个变种,有两个输入函数(constructA: Int => A, preservesPartitioning: Boolean = false)
    //第一个函数constructA是把RDD的partition index（index从0开始）作为输入，输出为新类型A；
    //第二个函数f是把二元组(T, A)作为输入（其中T为原RDD中的元素，A为第一个函数的输出），输出类型为U。
    val mapWithRDD = listRDD.mapWith(a => a * 10)((a, b) => b + 2)
    println(mapWithRDD.collect().mkString)


    println("-------------------flatMap->RDD-------------------------")
    //flatMap处理后生成多个元素构建的新RDD
    val flatMapRDD = sc.parallelize(1 to 4, 2).flatMap(x => 1 to x)
    println(flatMapRDD.collect().mkString)

    val lines = sc.parallelize(List("Hello world ", "hi"))
    val words = lines.flatMap(line => line.split(" "))
    println(words.first())
    //flatMapWith,flatMapValues与map同理


    println("-------------------reduce-------------------------")
    // reduce将RDD中元素两两传递给输入函数，同时产生一个新的值，
    // 新产生的值与RDD中下一个元素再被传递给输入函数直到最后只有一个值为止。
    val reduceInt = listRDD.reduce((x, y) => x + y)
    println(reduceInt)

    println("-------------------reduceByKey->RDD-------------------------")
    // reduceByKey是对元素为KV对的RDD中Key相同的元素的Value进行reduce
    // 然后与原RDD中的Key组成一个新的KV对
    val pairRDD = sc.parallelize(List((1, 2), (3, 4), (3, 6)))
    val reduceByKeyRDD = pairRDD.reduceByKey((x, y) => x + y)
    println(reduceByKeyRDD.collect().mkString)

    println("-------------------mapValRDD-------------------------")
    println("-------------------mapValRDD-------------------------")


  }

  //通过list/iterator迭代创建一个新的list返回其iterator/list
  def toNewList[T](it: Iterator[T]): Iterator[(T, T)] = {
    var res = List[(T, T)]()
    var pre = it.next
    while (it.hasNext) {
      val cur = it.next
      res ::=(pre, cur) //::操作向list的头部添加元素
      pre = cur
    }
    res.iterator
  }
}
