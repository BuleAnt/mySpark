package spark_streaming

import org.apache.spark.SparkConf
import org.apache.spark.examples.streaming.StreamingExamples
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by hadoop on 16-6-30.
  */

object NetworkCount {
  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("Usage: NetworkWorkCount <hostname> <port> <seconds>\n" +
        "In local mode, <master> should be 'local[n]' with n > 1")
      System.exit(1)
    }
    StreamingExamples.setStreamingLogLevels()
    //arg(2)为Stream每x秒产生一个batch
    val ssc = new StreamingContext(new SparkConf, Seconds(args(2).toInt))
    //在目标ip创建套接字,count
    val lines = ssc.socketTextStream(args(0), args(1).toInt, StorageLevel.MEMORY_ONLY_SER)
    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
    wordCounts.print()
    ssc.start()
    ssc.awaitTermination()
  }
}
