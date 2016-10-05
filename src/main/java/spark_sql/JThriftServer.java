package spark_sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Java通过JDBC方式访问Thrift Server,进而访问Spark SQL
 * 通过Thrift Server作为桥梁链接,是企业开发中最为常见的方式
 * 连接参考 spark官方doc:
 * http://spark.apache.org/docs/1.6.1/sql-programming-guide.html#jdbc-to-other-databases
 *
 */
public class JThriftServer {
	public static void main(String[] args) {

		String sql ="select name from students where age = ?";
		Connection conn = null;
		ResultSet resultSet = null;
		try {
			//hive server的驱动为org.apache.hadoop.hive.jdbc.HiveDriver
			//hive server2的驱动为org.apache.hive.jdbc.HiveDriver
			Class.forName("org.apache.hive.jdbc.HiveDriver");
			// 通过http方式发现送thrift RPC message;用户名为hadoop集群的superuser,我的是hadoop,密码随意
			conn = DriverManager.getConnection("jdbc:hive2://hadoop:10000/spark?" +
					"hive.server2.transport.mode=http;hive.server2.thrift.http.path=cliservice","hadoop","");
			PreparedStatement preparedStatement = conn.prepareStatement(sql); // prepareStatement防注入

			preparedStatement.setInt(1,30);// 第1个?匹配30
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				System.out.print(resultSet.getString(1));// 此处的数据应该保存到Parquet中等
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
