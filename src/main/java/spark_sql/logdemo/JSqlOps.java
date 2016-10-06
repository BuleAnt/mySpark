package spark_sql.logdemo;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.hive.HiveContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *  Spark SQL基于网站Log综合代码和实际运行测试
 */
public class JSqlOps {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf();
		conf.setMaster("spark://hadoop:7077").setAppName("userLog");
		JavaSparkContext sc = new JavaSparkContext(conf);
		HiveContext hiveContext = new HiveContext(sc.sc());

		String yesterday = getYesterday();
		String twodaysago = getTwodaysago();
		pvStatistic(hiveContext, twodaysago);//PV
		uvStatistic(hiveContext, yesterday);//UV
		hotChannel(hiveContext, yesterday);//热门板块
		jumpOutStatistic(hiveContext, yesterday);//页面跳出率
		newUserRegisterPercentStatistic(hiveContext, yesterday);//新用户注册的比例

	}

	private static void newUserRegisterPercentStatistic(HiveContext hiveContext, String yesterday) {
		hiveContext.sql("use spark");
		String newUserSQL = " select count(1) from user_logs where action= ‘View’ and date = '" + yesterday + "'and user_id is NULL ";
		String yesterdayRegisteredSQL = "select count(1) from user_logs where action= ‘Register’ and date = '" + yesterday + "'";

		Object newUser = hiveContext.sql(newUserSQL).collect()[0].get(0);
		Object yesterdayRegistered = hiveContext.sql(yesterdayRegisteredSQL).collect()[0].get(0);
		Double result = Double.valueOf(yesterdayRegistered.toString()) / Double.valueOf(newUser.toString());
		System.out.print("新用户注册的比例为：" + result);
	}

	private static void pvStatistic(HiveContext hiveContext, String twodaysago) {
		hiveContext.sql("use spark");
		String sqlText = "select date,page_id,count(1) pv " +
				"from user_logs where action = 'View' and date = ‘" + twodaysago + "’ " +
				"group by date,page_id order by pv desc limit 10";
		hiveContext.sql(sqlText).show();
		//执行结果数据放到数据库中或者hive中
		hiveContext.sql(sqlText).saveAsTable("pv");
		//hiveContext.
	}

	private static void uvStatistic(HiveContext hiveContext, String yesterday) {
		hiveContext.sql("use spark");
		String sqlText = "select date,channel,uv " +
				"from (select date,channel,page_id,count(1) uv " +
				"from (select date,channel,page_id,user_id from user_logs where action = 'View' and date = '" + yesterday +
				"' group by date,channel,page_id,user_id) subquery  group by date,channel,page_id) result" +
				"order by uv desc";
		hiveContext.sql(sqlText).show();
	}

	private static void jumpOutStatistic(HiveContext hiveContext, String yesterday) {
		hiveContext.sql("use spark");
		String totalPvSQL = " select count(1) from user_logs where action= ‘View’ and date = '" + yesterday + "' ";
		String pvToOneSQL = "select count(1) from (select count(1) totalNumber from user_logs where action = ‘View’" +
				" and date = '" + yesterday + "' group by user_id having totalNumber = 1) targetTable ";

		Object totalPv = hiveContext.sql(totalPvSQL).collect()[0].get(0);
		Object pvToOne = hiveContext.sql(pvToOneSQL).collect()[0].get(0);
		Double result = Double.valueOf(totalPv.toString()) / Double.valueOf(pvToOne.toString());
		System.out.print("跳出率为：" + result);
	}

	private static void hotChannel(HiveContext hiveContext, String yesterday) {
		hiveContext.sql("use spark");
		String sqlText = "select date,channel,channelpv  " +
				"from (select date,channel,count(1) channelpv from user_logs where action = 'View' and date = '" + yesterday +
				"'  group by date,channel) subquery " +
				"order by channelpv desc ";
		hiveContext.sql(sqlText).show();
	}


	private static String getYesterday() {
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		Date yesterday = cal.getTime();
		return date.format(yesterday);
	}


	private static String getTwodaysago() {
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -2);
		Date twodaysago = cal.getTime();
		return date.format(twodaysago);
	}
}
