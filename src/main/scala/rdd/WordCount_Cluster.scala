package rdd

import org.apache.spark.{SparkConf, SparkContext}

/**
	* 使用Scala开发集群运行的Spark WordCount程序
	*/
object WordCount_Cluster {
	def main(args: Array[String]) {

		/**
			* 第一步:创建配置对象SparkConf,设置Spark程序寻星时的配置信息
			* 例如说:通过setMaster来设置程序要链接的Spark程序的Master的URL,
			* 如果设置为local,则代表Spark程序运行在本地模式,特别适合机器比较差的初学者,1gRAM
			*/
		val conf = new SparkConf() //创建SparkConf对象
		conf.setAppName("My First Spark App!") //设置应用程序的名称
		conf.setMaster("spark://hadoop:7077") //此时,程序在Spark集群

		/**
			* 第二步:创建sparkcontext对象,sparkcontext对象是spark所有对象的唯一入口
			* 无论是采用scala,java,python,R等,都必须要有一个sparkcontext
			* sparkcontext核心作用:初始化spark应用程序运行所需要的核心组件,包括DAGScheduler,TaskScheduler,还有SchedulerBackend
			* 同时还会负责Spark程序往master注册程序等
			* Sparkcontext是整个Spark应用程序中最为至关重要的一个对象
			*/
		val sc = new SparkContext(conf) //创建sparkcontext对象,通过传入sparkconf实例来定制SPark运行

		/**
			* 第三步:根据具体的数据来源(HDFS,Hbase,LocalFS,DB,S3等)通过sparkcontext来创建rdd
			* RDD的创建基本有三种方式:根据外部数据来源(例如HDFS),根据Scala集合,由其他的RDD操作产生
			* 数据会被RDD划分成为一系列的Partitions,分配到每个Partition数据属于一个Task的处理范畴
			*/
		//读取本地文件,并设置为一个partition
		val lines = sc.textFile("/home/hadoop/Documents/workspaces/IdeaProjects/mySpark/src/main/resources/text.txt")
		//val lines = sc.textFile("/user/spark/wc/input/data") //读取HDFS文件,并切分成不同的partition

		/**
			* 第四步:对初始的rdd进行Transformation级别的处理,例如map,filter等高阶函数的编程,来进行具体的数据计算
			*/
		//4.1:将每一行的字符串拆分成单个单词
		val words = lines.flatMap { line => line.split(" ") } //对每一行的字符串进行单词拆分并把所有行的拆分结果通过flat合并成为一个大的单词集合
		//4.2:在单词产分的基础上,将每个单词实例计数为1,也就是word=>(word,1)
		val pairs = words.map { word => (word, 1) }
		//4.3:在每个单词实例计数为1基础上,统计每个单词的文件出现的总次数
		val wordCounts = pairs.reduceByKey(_ + _) //对相同的key进行value的累加(包括local和reducer级别同时Reducer)
		wordCounts.collect.foreach(wordNumberPair => println(wordNumberPair._1 + " : " + wordNumberPair._2))
		sc.stop()
	}
}
