package spark_sql.logdemo;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * 论坛数据自动生成数据格式:
 * data:日期,格式:yyyy-MM-dd
 * timestamp:时间戳
 * userId:用户ID
 * pageId:页面ID
 * chanelID:板块ID
 * action:点击/注册
 */
public class JSqlData {

	//论坛频道
	private static String[] channelNames = new String[]{
			"Spark", "Scala", "Kafka", "Flink", "Hadoop", "Storm", "Hive",
			"Impala", "HBase", "Java", "PHP", "ML"
	};

	//用户行为
	private static String[] actionNames = new String[]{"View", "Register"};
	private static StringBuffer userLogBuffer = new StringBuffer("");
	private static String yesterdayFormated;

	public static void main(String[] args) {

		long numberItems = 500000;//默认数据量是5000条
		String path = "target/out";//默认的文件输出路径
		if (args.length == 2) {
			numberItems = Integer.valueOf(args[0]);
			path = args[1];
		}
		System.out.println("User log number is:" + numberItems);

		//自动生成数据
		yesterdayFormated = yesterday();
		userlogs(numberItems, path);
	}

	//昨天的时间的生成
	private static String yesterday() {

		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();

		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		Date yesterday = cal.getTime();
		return date.format(yesterday);
	}

	//生成数据
	private static void userlogs(long numberItems, String path) {

		Random random = new Random();

		Long userID;
		int pageID;
		for (int i = 0; i < numberItems; i++) {
			long timestamp = new Date().getTime();//每条数据的timestamp
			if (random.nextInt(10) == 1) {
				userID = null;
			} else {
				userID = (long) random.nextInt((int) numberItems / 10);
			}

			//随机生成用户ID
			pageID = random.nextInt(20);//随机生成页面ID
			String channel = channelNames[random.nextInt(12)];//随机生成channel
			String action = actionNames[random.nextInt(2)];//随机生成行为
			//组装日志
			userLogBuffer.append(yesterdayFormated).append("\t")
					.append(timestamp).append("\t")
					.append(userID).append("\t")
					.append(pageID).append("\t")
					.append(channel).append("\t")
					.append(action).append("\n");
		}
		//System.out.print(userLogBuffer.toString());

		//讲日志写入文件
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path + "/userLog.log")));
			printWriter.write(userLogBuffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (printWriter != null) {
				printWriter.flush();
				printWriter.close();
			}
		}
	}


}
