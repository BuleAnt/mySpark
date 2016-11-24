package other.hbase

import org.apache.hadoop.hbase.{HBaseConfiguration, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-11-16.
  */
class HbaseTest {

	def main(args: Array[String]) {
		val config = new SparkConf().setAppName("HbaseTest").setMaster("local")
		val sc = new SparkContext(config)

		val conf = HBaseConfiguration.create
		var tablename = "scores"
		// Other options for configuring scan behavior are available. More information available at
		// http://hbase.apache.org/apidocs/org/apache/Hadoop/hbase/mapreduce/TableInputFormat.html
		conf.set(TableInputFormat.INPUT_TABLE, tablename)


		// Initialize hBase table if necessary
		val admin = new HBaseAdmin(conf)
		if (!admin.isTableAvailable(tablename)) {
			//val tableDesc = new HTableDescriptor(TableName.valueOf(args(0)))

			//admin.createTable(tableDesc)
		}
		println("start ")

		val hBaseRDD = sc.newAPIHadoopRDD(conf, classOf[TableInputFormat],
			classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
			classOf[org.apache.hadoop.hbase.client.Result])



		println(tablename + "表的总行数为 " + hBaseRDD.count())


		//## 获取键值对信息
		val keyValue = hBaseRDD.map(x => x._2).map(_.list)


		//outPut is a RDD[String], in which each line represents a record in HBase
	/*	val outPut = keyValue.flatMap(x => x.asScala.map(cell =>
			"columnFamily=%s,qualifier=%s,timestamp=%s,type=%s,value=%s".format(
				Bytes.toStringBinary(CellUtil.cloneFamily(cell)),
				Bytes.toStringBinary(CellUtil.cloneQualifier(cell)),
				cell.getTimestamp.toString,
				Type.codeToType(cell.getTypeByte),
				Bytes.toStringBinary(CellUtil.cloneValue(cell))
			)
		)
		)*/


		//outPut.foreach(println)
	}
}
