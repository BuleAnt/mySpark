package spark_streaming

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket
import java.nio.charset.StandardCharsets

import org.apache.spark.storage.StorageLevel
import org.apache.spark.{Logging, SparkConf}
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by hadoop on 16-10-12.
  */
object MyReceiverWC {

	def main(args: Array[String]) {
		val sparkConf = new SparkConf().setAppName("CustomReceiver")
		val ssc = new StreamingContext(sparkConf, Seconds(1))
		val lines = ssc.receiverStream(new CustomReceiver(args(0), args(1).toInt))
		val words = lines.flatMap(_.split(" "))
		val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
		wordCounts.print()
		ssc.start()
		ssc.awaitTermination()
	}
}

class CustomReceiver(host: String, port: Int)
		extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) with Logging {

	def onStart() {
		// Start the thread that receives data over a connection
		new Thread("Socket Receiver") {
			override def run() {
				receive()
			}
		}.start()
	}

	def onStop() {
		// There is nothing much to do as the thread calling receive()
		// is designed to stop by itself isStopped() returns false
	}

	/** Create a socket connection and receive data until receiver is stopped */
	private def receive() {
		var socket: Socket = null
		var userInput: String = null
		try {
			logInfo("Connecting to " + host + ":" + port)
			socket = new Socket(host, port)
			logInfo("Connected to " + host + ":" + port)
			val reader = new BufferedReader(
				new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
			userInput = reader.readLine()
			while (!isStopped && userInput != null) {
				println(userInput)
				store(userInput)
				userInput = reader.readLine()
			}
			reader.close()
			socket.close()
			logInfo("Stopped receiving")
			restart("Trying to connect again")
		} catch {
			case e: java.net.ConnectException =>
				restart("Error connecting to " + host + ":" + port, e)
			case t: Throwable =>
				restart("Error receiving data", t)
		}
	}
}
