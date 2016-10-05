package rdd

import org.apache.spark.SparkContext

/**
	* text->rdd-->filter(word=3)-->wc-->sort-->save
	*/

object SparkYarn {
	/**
		* data:{a a b...}
		* -->{(a,1),(a,1),(b,1)}
		* -->{(a,2),(b,1)}
		* -->{(2->a),(1->b)}
		* -->{(a->2),(b->1)}-->a 2 /n b 1
		*
		* @param args inputPath outputPath
		*/
	def main(args: Array[String]) {
		val sc = new SparkContext
		val data = sc.textFile(args(0))

		data.filter(_.split(' ').length == 3)
			.map(_.split(' ')(1) -> 1)
			.reduceByKey(_ + _)
			.map(x => x._2 -> x._1)
			.sortByKey(false)
			.map(y => y._2 -> y._1)
			.saveAsTextFile(args(1))
	}
}
