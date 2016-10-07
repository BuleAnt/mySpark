package rdd

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-8-24.
  */
object AccumulatorDemo {
	def main(args: Array[String]) {

		val sc = new SparkContext(new SparkConf().setAppName("AccumulatorDemo"))
		val file = sc.textFile("src/test/resources/text.txt")
		val blankLines = sc.accumulator(0)
		val callSigns = file.flatMap(line => {
			if (line == "") {
				blankLines += 1
				Console.println("Blank lines: " + blankLines.value)
			}
			line.split(" ")
		})

		callSigns.collect().foreach(println)
		//callSigns.saveAsTextFile("target/out/output.txt")
	}
}
