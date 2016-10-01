import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-8-26.
  */
object Function {
  def main(args: Array[String]) {
    /*   for (i <- (1 to 10))
         print(i)
       val somNumbers = List(-11, 0, 23, 4)
       somNumbers.filter(x => x > 0)
       def factorial_loop(i: BigInt): BigInt = {
         var result = BigInt(1)
         for (j <- 2 to i.intValue)
           result *= j
         result
       }*/
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("test"))
    val list = List((1, 2), (3, 4))
    val rdd = sc.parallelize(list)
    //rdd.foreach(fun)
    rdd.foreach(x => for (i <- x._1 to x._2) println(x._1, x._2, i))
    // val test = list.foreach(fun)
    //val test2 = list.foreach(map(x => x))
    //println(test)

    /* def fun(x: (Int, Int)) = {
      // var j = 0
       for (i <- (x._1 to x._2))
       //println(i)
        // j = i
      println (x._1, x._2, i)
     }
   */
  }
}
