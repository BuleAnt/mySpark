package core

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-8-24.
  */
class AccumulatorDemo {
  val sc = new SparkContext(new SparkConf().setAppName("testAccumulator"))
  val file = sc textFile("file.txt")
  val blankLines = sc.accumulator(0)
  val callSigns = file.flatMap(line => {
    if (line == ""){
      blankLines += 1
    }
    line.split(" ")
  })

  callSigns.saveAsTextFile("output.txt")
  println("Blank lines: "+blankLines.value)
}
