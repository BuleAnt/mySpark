package spark_mllib.demo.itemcf

import java.net.URLDecoder

import scala.io.Source
import scala.util.matching.Regex

/**
  * Created by noah on 17-9-4.
  */
object Regular {
  def decode(value: String): String = URLDecoder.decode(value, "UTF8")

  def main(args: Array[String]): Unit = {
    /*val string = "Hello World 123"
    //"""原生表达
    val regex =
      """([0-9]+)([a-z]+)""".r
    val numPattern = "[0-9]+".r
    val numberPattern ="""\s+[0-9]+\s+""".r
    val m = numPattern.findAllIn(string)
    m.foreach(println)*/
    //10.143.136.24 [2017-08-19 09:12:41]
    // "messag "
    // "160161001||%E9%97%A8%E6%A5%A3%E5%B9%BF%E5%91%8A||%E5%B9%BF%E5%91%8A||_||||||011||110||20170819091241||5.0||Mozilla/5.0%20(Windows%20NT%206.1;%20WOW64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/60.0.3112.78%20Safari/537.36||||||||||||10.10.34.45||N2"
    // "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.78 Safari/537.36"
    // ""
    val file = Source.fromFile("/home/noah/workspaces/spark/messag_2017-08-19_sec.log")
    val line = file.getLines().next()
    val split = line.split("\"messag \"")
    val ip_date = split(0)
    val dateP1 = new Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}) \[(\d{4}-\d{2}-\d{2} \d{2}\:\d{2}\:\d{2})\]""", "ip","date")
    val m = dateP1.findAllMatchIn(ip_date)
    if(m.hasNext){
      val group = m.next()
      group.group("date")
    }
    file.close
    /*val ip_date = "10.143.136.24 [2017-08-19 09:12:41]"


    println(m.next().group("date"))*/
  }
}
