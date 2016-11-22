package other.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.util.Base64;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 */
public class HbaseJava {
	public static void main(String[] args) {
		JavaSparkContext sc = new JavaSparkContext("local", "hbaseTest",
				System.getenv("SPARK_HOME"), System.getenv("JARS"));

		Configuration conf = HBaseConfiguration.create();
		Scan scan = new Scan();
		scan.addFamily(Bytes.toBytes("cf"));
		scan.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("airName"));

		try {
			String tableName = "flight_wap_order_log";
			conf.set(TableInputFormat.INPUT_TABLE, tableName);
			/*ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
			String ScanToString = Base64.encodeBytes(proto.toByteArray());
			conf.set(TableInputFormat.SCAN, ScanToString);

			JavaPairRDD<ImmutableBytesWritable, Result> myRDD =
					sc.newAPIHadoopRDD(conf, TableInputFormat.class,
							ImmutableBytesWritable.class, Result.class);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
