package spark_sql

import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
	* Created by hadoop on 16-10-5.
	*/
object SQLUDFUDAF {

	def main(args: Array[String]) {

		val conf = new SparkConf() //创建SparkConf对象
		conf.setAppName("SparkSQLUDFUDAF")
		//    conf.setMaster("spark://Master:7077") //此时，程序在Spark集群
		conf.setMaster("local[4]")
		val sc = new SparkContext(conf)
		val sqlContext = new SQLContext(sc)

		//模拟实际使用的数据,基于数据创建DataFrame
		val bigData = Array("Spark", "Spark", "Hadoop", "Spark", "Hadoop", "Spark", "Spark", "Hadoop", "Spark", "Hadoop")
		val bigDataRDD = sc.parallelize(bigData)
		val bigDataRDDRow = bigDataRDD.map(item => Row(item))
		val structType = StructType(Array(StructField("word", StringType, true))) //true可以为空
		val bigDataDF = sqlContext.createDataFrame(bigDataRDDRow, structType)
		bigDataDF.registerTempTable("bigDataTable")

		/**
			* 通过SQLContext.udf.register注册UDF
			* 这里注册的udf名称为computeLength,输入参数为input: String,输出该字符串的长度
			*/
		//在Scala 2.10.x版本UDF函数最多可以接受22个输入参数
		sqlContext.udf.register("length", (input: String) => input.length)
		//注册后,可以直接在SQL语句中使用UDF，就像使用SQL自动的内部函数一样
		sqlContext.sql("select word,length(word) as length from bigDataTable").show


		sqlContext.udf.register("wordCount", new MyUDAF)
		sqlContext.sql("select word,wordCount(word) as count,length(word) as length" +
			" from bigDataTable group by word").show()
		//while (true) ()//可以在web UI中查看
	}
}

/**
	* 按照模板实现UDAF
	* 简单的对DataFrame的count功能
	*/
class MyUDAF extends UserDefinedAggregateFunction {

	/**
		* 聚合函数的输入参数的数据类型
		* field指定的名称input,仅仅是为了标识输入参数,确定相应的缓冲区的值
		*/
	override def inputSchema: StructType = StructType(Array(StructField("input", StringType, true)))

	/**
		* 在聚合缓冲区(进行聚合操作的时候处理)的数据类型
		*/
	override def bufferSchema: StructType = StructType(Array(StructField("count", IntegerType, true)))

	/**
		* 指定聚合函数函数计算结果返回值数据类型
		*/
	override def dataType: DataType = IntegerType

	//当且仅当该函数是确定性的,返回true
	override def deterministic: Boolean = true

	/**
		* 在merge聚合(Aggregate)之前,初始化缓冲区,每组(缓冲区)数据置零:如
		* `merge(initialBuffer, initialBuffer)` should equal `initialBuffer`.
		*/
	override def initialize(buffer: MutableAggregationBuffer): Unit = {
		buffer(0) = 0
	}

	/**
		* 在进行聚合时,每当有新的值进来，update聚合缓冲区
		* 即:每来一组输入数据row,就对分组后的数据通过本地的聚合操作进行一次计算更新
		* 相当于mr中的Combiner
		*/
	override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
		buffer(0) = buffer.getAs[Int](0) + 1
	}

	/**
		* merge两组聚合缓冲区,并存储更新过的缓冲区值到第一组AggregationBuffer
		* 每当我们将两组聚合过的数据合并(merge)的时候,将调用此方法
		* 最后在分布式节点进行Local Reduce完成后需要进行全局级别的Merge操作
		*/
	override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
		buffer1(0) = buffer1.getAs[Int](0) + buffer2.getAs[Int](0)
	}

	/**
		* 返回UDAF计算的最终结果
		*/
	override def evaluate(buffer: Row): Any = buffer.getAs[Int](0)
}
