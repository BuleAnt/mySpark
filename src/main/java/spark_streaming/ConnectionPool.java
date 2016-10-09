package spark_streaming;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;

/**
 * 使用Java编写一个最基本数据库连接池类
 */
public class ConnectionPool {
	public static LinkedList<Connection> connectionQueue;

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public synchronized static Connection getConnection() {
		try {
			if (connectionQueue == null) {
				connectionQueue = new LinkedList<>();
				for (int i = 0; i < 5; i++) {
					Connection conn = DriverManager.getConnection(
							"jdbc:mysql://hadoop:3306/streaming", "root", "root");
					connectionQueue.push(conn);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connectionQueue.poll();

	}

	public synchronized static void returnConnection(Connection conn) {
		connectionQueue.push(conn);
	}
}
