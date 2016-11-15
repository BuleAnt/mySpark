package spark_streaming.demo;

import com.google.common.base.Optional;
import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.sql.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * 在线处理广告点击流:
 * 广告点击的基本数据格式:timestamp,ip,userID,adID,province,city
 */
public class AdClickedStreamingStats {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf();
		conf.setAppName("addClickedStreamingStats").setMaster("local[4]");

		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(10));

		Map<String, String> kafkaParameters = new HashMap<>();
		//可以添加多个broker
		kafkaParameters.put("metadata.broker.list", "hadoop:9092");
		Set<String> topics = new HashSet<>();
		//可以关注多个topic,企业级别一般是可配置的,不能代码硬写
		topics.add("SparkStreamingDirected");
		JavaPairDStream<String, String> adClickedStreaming = KafkaUtils
				.createDirectStream(jssc,
						String.class, String.class,
						StringDecoder.class, StringDecoder.class,
						kafkaParameters, topics);


		/**
		 * 因为对黑名单进行在线过滤,而数据是在RDD中,所以使用transform这个函数
		 * 但在这里我们使用transformToPair,原因是读进来的Kafka的数据是Pair<String,String>类型的
		 * 另一个原因是过滤后的数据要进行进一步处理,所以必须是读进来的Kafka数据的原始类型DStream<String,String>
		 * 在此:再次说明每个Batch Duration中实际上讲,输入的数据是被一个且仅仅被一个RDD封装的,
		 * 你可以有多个InputDStream,但是其实在产生Job的时候,这些不同的InputDStream在Batch Duration中
		 * 相当于Spark基于HDFS数据操作的不同文件来源而已
		 */
		final JavaPairDStream<String, String> filteredADClickedStreaming = adClickedStreaming.transformToPair(new Function<JavaPairRDD<String, String>, JavaPairRDD<String, String>>() {


			@Override
			public JavaPairRDD<String, String> call(JavaPairRDD<String, String> rdd) throws Exception {
				/**
				 * 在线黑名单过滤思路步骤:
				 * 1.从数据库的黑名单表blacklisttable中获取黑名单转换成RDD,即新的RDD实例封装黑名单数据;
				 * 2.然后把代表黑名单的RDD的实例和Batch Duration产生的RDD进行join操作
				 *  准确的说是进行leftOuterJoin操作,也就是使用Batch Duration产生的RDD和黑名单的RDD的实例进行leftOuterJoin
				 *  如果两者都有,true,否则为false
				 *  我们要留下的是leftOuterJoin操作结果为false的数据
				 */
				JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
				final List<String> blackListNames = new ArrayList<>();
				jdbcWrapper.doQuery("SELECT * FROM blacklisttable", null, new ExecuteCallBack() {

					@Override
					public void resultCallBack(ResultSet result) throws SQLException {

						while (result.next()) {
							blackListNames.add(result.getString(1));
						}
					}
				});
				List<Tuple2<String, Boolean>> blackListFromDB = new ArrayList<>();
				for (String name : blackListNames) {
					blackListFromDB.add(new Tuple2<>(name, true));
				}

				JavaSparkContext jsc = new JavaSparkContext(rdd.context());
				/**
				 * 黑名单表中只有userID,但如果进行join操作的化,就必须是kv,
				 * 所以在这里需要基于数据表中的数据产生K-v类型的数据集合
				 */
				JavaPairRDD<String, Boolean> blackListRDD = jsc.parallelizePairs(blackListFromDB);
				/**
				 * 进行操作的时候肯定是基于userID进行join的,
				 * 所以必须把传入rdd进行mapToPair操作,转化成为符合格式的rdd
				 * 这里的rdd即从kafka消费过来的记录
				 */
				JavaPairRDD<String, Tuple2<String, String>> rdd2Pair = rdd.mapToPair(new PairFunction<Tuple2<String, String>, String, Tuple2<String, String>>() {
					@Override
					public Tuple2<String, Tuple2<String, String>> call(Tuple2<String, String> t) throws Exception {
						String userID = t._2.split("\t")[2];//timestamp,ip,userID,adID,province,city
						return new Tuple2<>(userID, t);
					}
				});

				//join-->(userID,(kafkaRecord,true/false))
				JavaPairRDD<String, Tuple2<Tuple2<String, String>, Optional<Boolean>>> joined = rdd2Pair.leftOuterJoin(blackListRDD);

				//filter-->mapToPair-->kafkaRecord
				JavaPairRDD<String, String> result = joined.filter(new Function<Tuple2<String, Tuple2<Tuple2<String, String>, Optional<Boolean>>>, Boolean>() {
					@Override
					public Boolean call(Tuple2<String, Tuple2<Tuple2<String, String>, Optional<Boolean>>> v1) throws Exception {

						Optional<Boolean> optional = v1._2._2;
						if (optional.isPresent() && optional.get()) {
							return false;//join后为true的,在filter中设置为false,即过滤掉在backList存在的数据
						}
						return true;
					}
				}).mapToPair(new PairFunction<Tuple2<String, Tuple2<Tuple2<String, String>, Optional<Boolean>>>, String, String>() {
					@Override
					public Tuple2<String, String> call(Tuple2<String, Tuple2<Tuple2<String, String>, Optional<Boolean>>> t) throws Exception {

						return t._2._1;
					}
				});
				return result;
			}
		});

		/**
		 * 经过初步过滤后的filteredADClickedStreaming,将其每一条记录转换成pairs(string,1),为后续做准备
		 */
		JavaPairDStream<String, Long> pairs = filteredADClickedStreaming.mapToPair(new PairFunction<Tuple2<String, String>, String, Long>() {
			@Override
			public Tuple2<String, Long> call(Tuple2<String, String> t) throws Exception {
				String[] splited = t._2.split("\t");
				String timestamp = splited[0];//yyyy-MM-dd
				String ip = splited[1];
				String userID = splited[2];
				String adID = splited[3];
				String province = splited[4];
				String city = splited[5];

				String clickedRecord = timestamp + "_" + ip + "_" + userID + "_" + adID + "_"
						+ province + "_" + city;

				return new Tuple2<>(clickedRecord, 1L);
			}
		});

		/**
		 * 计算每个Batch Duration中每个User的广告点击量
		 */
		JavaPairDStream<String, Long> adClickedUsers = pairs.reduceByKey(new Function2<Long, Long, Long>() {
			@Override
			public Long call(Long v1, Long v2) throws Exception {
				return v1 + v2;
			}
		});

		/**
		 * 在线上计算并判断什么是有效点击
		 * 1.复杂化的一般都是采用机器学习训练好模型(建模)直接在线进行过滤
		 * 2.简单的,可以通过一个Batch Duration中的点击次数来判断是不是非法广告点击
		 * 但是实际上讲,非法广告点击程序会尽可能模拟真实的广告点击行为,所以通过Batch来判断是不完整的,
		 * 我们需要对例如一天(也可以是每1小时)的数据进行判断
		 * 3.比在线机器学习退而求其次的做法如下:
		 *  例如:一段时间内,同一个IP(MAC地址)有多个用户的账号访问
		 *  例如:可以统计一天内一个用户点击广告的次数,如果一天点击同样的广告超过50次的话,列入黑名单
		 * 黑名单有一个重要的特征:动态生成!!所以每次每一个Batch Duration都要考虑是否有新的黑名单加入
		 * 此时黑名单需要存储起来,具体存储在什么地方呢?存储在DB/Redis中即可
		 *
		 * 例如邮件系统中的"黑名单"可以采用SparkStreaming不断的监控每个用户的操作,如果用户发送邮件频率超过了设定的值
		 * 可以暂时把用户列入到黑名单,从而组织用户过度频繁的发送邮件
		 */
		JavaPairDStream<String, Long> filterClickInBatch = adClickedUsers.filter(new Function<Tuple2<String, Long>, Boolean>() {
			@Override
			public Boolean call(Tuple2<String, Long> v1) throws Exception {
				//每10s内,点击次数超过1次的过滤掉
				if (1 < v1._2) {
					// TODO 更新一下黑名单的数据表
					return false;
				} else {
					return true;
				}
			}
		});

		// 将中间数据保存到DB表广告点击事件adclicked中,用于获取累计黑名单
		filterClickInBatch.foreachRDD(new Function<JavaPairRDD<String, Long>, Void>() {
			@Override
			public Void call(JavaPairRDD<String, Long> rdd) throws Exception {

				// partition级别
				rdd.foreachPartition(new VoidFunction<Iterator<Tuple2<String, Long>>>() {
					@Override
					public void call(Iterator<Tuple2<String, Long>> partition) throws Exception {
						/**
						 * 在这里我们使用数据库连接池的高效读写数据库的方式把数据写入数据库MySql的表adclicked
						 * 由于传入的参数是一个Iterator类型的集合,所以为了更加高效的操作我们需要批量处理
						 * 例如说:一次性加入1k条Record,使用insertBatch或者updateBatch类型操作
						 * 插入的用户信息可以只包含:(timestamp,ip),userID,adID,clickedCount,time
						 *
						 * 这里有个问题:可能出现两条记录的key是一样的,此时就需要更新update累加操作
						 */
						// TODO jdbc update/insert
						List<UserAdClicked> userAdClickedList = new ArrayList<>();
						while (partition.hasNext()) {
							Tuple2<String, Long> record = partition.next();
							String[] splited = record._1.split("_");
							UserAdClicked userAdClicked = new UserAdClicked();
							userAdClicked.setTimestamp(splited[0]);
							userAdClicked.setIp(splited[1]);
							userAdClicked.setUserID(splited[2]);
							userAdClicked.setAdID(splited[3]);
							userAdClicked.setProvince(splited[4]);
							userAdClicked.setCity(splited[5]);
							userAdClickedList.add(userAdClicked);
						}
						final List<UserAdClicked> inserting = new ArrayList<>();
						final List<UserAdClicked> updating = new ArrayList<>();

						JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
						// 判断DB中是否含有该数据:字段:timeStamp,ip,userID,adID,province,city,clickedCount
						for (final UserAdClicked clicked : userAdClickedList) {

							jdbcWrapper.doQuery("SELECT count(1) FROM adclicked WHERE " +
											"timestamp = ? AND userID = ? AND adID = ?",
									new Object[]{clicked.getTimestamp(), clicked.getUserID(), clicked.getAdID()},
									new ExecuteCallBack() {
										@Override
										public void resultCallBack(ResultSet result) throws SQLException {
											if (result.next()) {
												long count = result.getLong(1);//获取result的第一个字段的值
												clicked.setClickedCount(count);
												updating.add(clicked);
											} else {
												//clicked.setClickedCount(1L);
												inserting.add(clicked);
											}
										}
									});
						}

						ArrayList<Object[]> inserParametersList = new ArrayList<>();
						for (UserAdClicked insertRecord : inserting) {
							inserParametersList.add(new Object[]{
									insertRecord.getTimestamp(),
									insertRecord.getIp(),
									insertRecord.getUserID(),
									insertRecord.getAdID(),
									insertRecord.getProvince(),
									insertRecord.getCity(),
									insertRecord.getClickedCount()
							});
						}
						jdbcWrapper.doBatch("INSERT INTO adclicked VALUES(?,?,?,?,?,?,?)", inserParametersList);


						ArrayList<Object[]> updateParametersList = new ArrayList<>();
						for (UserAdClicked updateRecord : updating) {
							updateParametersList.add(new Object[]{
									updateRecord.getClickedCount(),
									updateRecord.getTimestamp(),
									updateRecord.getUserID(),
									updateRecord.getAdID(),
									updateRecord.getProvince(),
									updateRecord.getCity()
							});
						}
						//timeStamp,ip,userID,adID,province,city,clickedCount
						jdbcWrapper.doBatch("UPDATE adclicked SET clickedCount = ? WHERE" +
								"timestamp =? AND userID = ? AND adID = ? AND province = ? AND city = ? ", updateParametersList);
					}
				});
				return null;
			}
		});

		/**
		 * 从暂存的DB表adclicked中获取以天为级别的黑名单,用于更新黑名单
		 */
		JavaPairDStream<String, Long> blackBasedOnListHistory = filterClickInBatch.filter(new Function<Tuple2<String, Long>, Boolean>() {
			@Override
			public Boolean call(Tuple2<String, Long> v1) throws Exception {
				String[] splited = v1._1.split("_");//timestamp_ip_userID_adID_province_city

				String data = splited[0];
				String userID = splited[2];
				String adID = splited[3];

				/**
				 * 接下来要根据date,userID,adID等条件去查询用户点击广告的数据表
				 * 获得当天总得点击次数,这个时候基于点击次数判断是否属于黑名单点击
				 */
				//TODO jdbc
				JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
				final int[] clickedCountTotalToday = {0};
				jdbcWrapper.doQuery("SELECT clickedCount FROM adclicked WHERE data = ? AND userID = ? AND adID = ? ", new Object[]{data, userID, adID}, new ExecuteCallBack() {
					@Override
					public void resultCallBack(ResultSet result) throws SQLException {
						clickedCountTotalToday[0] = result.getInt(1);
					}
				});
				if (clickedCountTotalToday[0] > 50) {
					return true;
				}
				return false;
			}
		});

		/**
		 * 对黑名单整个RDD进行去重操作
		 */
		JavaDStream<String> blackListUserID = blackBasedOnListHistory.map(new Function<Tuple2<String, Long>, String>() {
			@Override
			public String call(Tuple2<String, Long> v1) throws Exception {
				return v1._1.split("_")[2];
			}
		});

		blackListUserID.transform(new Function<JavaRDD<String>, JavaRDD<String>>() {
			@Override
			public JavaRDD<String> call(JavaRDD<String> rdd) throws Exception {
				return rdd.distinct();
			}
		});

		// 写入黑名单数据表blacklisttable中
		blackListUserID.foreachRDD(new Function<JavaRDD<String>, Void>() {
			@Override
			public Void call(JavaRDD<String> rdd) throws Exception {

				rdd.foreachPartition(new VoidFunction<Iterator<String>>() {
					@Override
					public void call(Iterator<String> t) throws Exception {

						/**
						 * 此时对blackListUserId直接插入黑名单数据表即可
						 */
						//TODO 这里只是功能性的实现,企业级别要进行javaBean设计
						List<Object[]> blackList = new ArrayList<>();
						while (t.hasNext()) {
							blackList.add(new Object[]{t.next()});
						}
						JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
						//blacklisttable字段:userID
						jdbcWrapper.doBatch("INSERT INTO blacklisttable  VALUES (?)", blackList);
					}
				});
				return null;
			}
		});
		/**
		 * 广告点击累计动态更新
		 * 每个updateStateByKey都会在Bach Duration的时间间隔的基础上进行广告点击次数的更新
		 * 更新之后,我们一般都会持久化的外部存储设备上,在这里我们存储到MySQL数据库中
		 */
		JavaPairDStream<String, Long> updateStateByKeyDStream = filteredADClickedStreaming.mapToPair(new PairFunction<Tuple2<String, String>, String, Long>() {
			@Override
			public Tuple2<String, Long> call(Tuple2<String, String> t) throws Exception {
				String[] splited = t._2.split("\t");
				String timestamp = splited[0];
				String ip = splited[1];
				String userID = splited[2];
				String adID = splited[3];
				String province = splited[4];
				String city = splited[5];

				String clickedRecord = timestamp + "_" + adID + "_"
						+ province + "_" + city;
				return new Tuple2<>(clickedRecord, 1L);
			}
		}).updateStateByKey(new Function2<List<Long>, Optional<Long>, Optional<Long>>() {
			/**
			 * updateStateByKey是根据当前Batch Duration新增的数据按key进行更新以前所有的累计数据
			 *
			 * @param v1 代表的是当前的key在当前的Batch Duration中出现的次数的集合,例如{1,1,1,...}
			 * @param v2 代表当前key在以前的Batch Duration中积累下来的结果
			 * @return 返回v1中的value加和到v2后的结果
			 */
			@Override
			public Optional<Long> call(List<Long> v1, Optional<Long> v2) throws Exception {
				Long clickedTotalHistory = 0L;
				if (v2.isPresent()) {
					clickedTotalHistory = v2.get();
				}
				for (Long one : v1) {
					clickedTotalHistory += one;
				}
				return Optional.of(clickedTotalHistory);
			}
		});

		updateStateByKeyDStream.foreachRDD(new VoidFunction<JavaPairRDD<String, Long>>() {
			@Override
			public void call(JavaPairRDD<String, Long> rdd) throws Exception {

				rdd.foreachPartition(new VoidFunction<Iterator<Tuple2<String, Long>>>() {
					@Override
					public void call(Iterator<Tuple2<String, Long>> partition) throws Exception {
						List<AdClicked> userAdClickedList = new ArrayList<>();
						while (partition.hasNext()) {
							Tuple2<String, Long> record = partition.next();//
							String[] splited = record._1.split("\t");
							AdClicked adClicked = new AdClicked();

							//timestamp_adID_province_city;
							adClicked.setTimestamp(splited[0]);
							adClicked.setAdID(splited[1]);
							adClicked.setProvince(splited[2]);
							adClicked.setCity(splited[3]);
							adClicked.setClickedCount(record._2);


							final List<AdClicked> inserting = new ArrayList<>();
							final List<AdClicked> updating = new ArrayList<>();

							JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
							// 判断DB中是否含有该数据:字段:timeStamp,ip,userID,adID,province,city,clickedCount
							for (final AdClicked clicked : userAdClickedList) {

								jdbcWrapper.doQuery("SELECT clickedCount FROM adclickedcount WHERE " +
												"timestamp = ? AND adID = ? AND privince = ? AND city = ? ",
										new Object[]{clicked.getTimestamp(), clicked.getAdID(), clicked.getProvince(), clicked.getCity()},
										new ExecuteCallBack() {
											@Override
											public void resultCallBack(ResultSet result) throws SQLException {
												if (result.next()) {
													long count = result.getLong(1);//获取result的第一个字段的值
													clicked.setClickedCount(count);
													updating.add(clicked);
												} else {
													//clicked.setClickedCount(1L);
													inserting.add(clicked);
												}
											}
										});
							}

							ArrayList<Object[]> inserParametersList = new ArrayList<>();
							for (AdClicked insertRecord : inserting) {
								inserParametersList.add(new Object[]{
										insertRecord.getTimestamp(),
										insertRecord.getAdID(),
										insertRecord.getProvince(),
										insertRecord.getCity(),
										insertRecord.getClickedCount()
								});
							}
							jdbcWrapper.doBatch("INSERT INTO clickedcount VALUES(?,?,?,?,?)", inserParametersList);


							ArrayList<Object[]> updateParametersList = new ArrayList<>();
							for (AdClicked updateRecord : updating) {
								updateParametersList.add(new Object[]{
										updateRecord.getClickedCount(),
										updateRecord.getTimestamp(),
										updateRecord.getAdID(),
										updateRecord.getCity(),
										updateRecord.getClickedCount()
								});
							}
							jdbcWrapper.doBatch("UPDATE clickedcount SET clickedCount = ? WHERE" +
									"timestamp =? AND adID = ? AND privince = ? AND city = ? ", updateParametersList);
						}
					}
				});
			}
		});

		/**
		 * 对广告点击进行top 5计算,计算出每天每个省份的Top5的排名广告
		 * 因为直接对RDD进行操作,所以使用了transform算子,
		 * 先对rdd进行reduceByKey然后转换DataFrame操作,使用hiveSQL的窗口函数,获取top5
		 * 将top5在转为RDD,并保存在数据库DB中
		 */
		updateStateByKeyDStream.transform(new Function<JavaPairRDD<String, Long>, JavaRDD<Row>>() {
			@Override
			public JavaRDD<Row> call(JavaPairRDD<String, Long> rdd) throws Exception {

				// pairRDD(k,v)-->mapToPair(对tuple2操作:只保留time,adID,province,count)
				// -->pairRDD(k,v)-->reduceByKey-->map-->RowRDD
				JavaRDD<Row> rowRDD = rdd.mapToPair(new PairFunction<Tuple2<String, Long>, String, Long>() {
					@Override
					public Tuple2<String, Long> call(Tuple2<String, Long> t) throws Exception {
						String[] splited = t._1.split("_");
						String timestamp = splited[0];
						String adID = splited[1];
						String province = splited[2];

						String clickedRecord = timestamp + "_" + adID + "_" + province;

						return new Tuple2<>(clickedRecord, t._2);
					}
				}).reduceByKey(new Function2<Long, Long, Long>() {//根据timestamp_adID_province进行reduce
					@Override
					public Long call(Long v1, Long v2) throws Exception {
						return v1 + v2;
					}
				}).map(new Function<Tuple2<String, Long>, Row>() {//pairRDD-->rowRDD
					@Override
					public Row call(Tuple2<String, Long> v1) throws Exception {
						String[] splited = v1._1.split("_");
						String timestamp = splited[0];
						String adID = splited[1];
						String province = splited[2];

						//使用rowRDD的工厂方法,参数是object
						return RowFactory.create(timestamp, adID, province, v1._2);
					}
				});

				StructType structType = DataTypes.createStructType(Arrays.asList(
						DataTypes.createStructField("timestamp", DataTypes.StringType, true),
						DataTypes.createStructField("adID", DataTypes.StringType, true),
						DataTypes.createStructField("province", DataTypes.StringType, true),
						DataTypes.createStructField("clickedCount", DataTypes.StringType, true)
				));
				HiveContext hiveContext = new HiveContext(rdd.context());
				DataFrame df = hiveContext.createDataFrame(rowRDD, structType);

				df.registerTempTable("topNTableSource");
				DataFrame result = hiveContext.sql("SELECT timestamp,adID,province,clickedCount FROM " +
						"(SELECT timestamp,adID,province,clickedCount,ROW_NUMBER() " +
						"OVER(PARTITION BY province ORDER BY clickedCount DESC) rank " +
						"FROM topNTableSource) subquery " +
						"WHERE rank <= 5");
				return result.toJavaRDD();
			}
		}).foreachRDD(new VoidFunction<JavaRDD<Row>>() {
			@Override
			public void call(JavaRDD<Row> rowJavaRDD) throws Exception {
				rowJavaRDD.foreachPartition(new VoidFunction<Iterator<Row>>() {
					@Override
					public void call(Iterator<Row> rowIterator) throws Exception {

						List<AdProvinceTopN> adProvinceTopN = new ArrayList<>();
						while (rowIterator.hasNext()) {
							Row row = rowIterator.next();
							AdProvinceTopN item = new AdProvinceTopN();
							item.setTimestamp(row.getString(0));
							item.setAdID(row.getString(1));
							item.setProvince(row.getString(2));
							item.setClickedCount(row.getLong(3));
							adProvinceTopN.add(item);
						}

						Set<String> set = new HashSet<>();

						//去重操作
						for (AdProvinceTopN item : adProvinceTopN) {
							set.add(item.getTimestamp() + "_" + item.getProvince());
						}
						//按天,分省份的top5,先删除
						ArrayList<Object[]> deleteParametersList = new ArrayList<>();
						for (String deleteRecord : set) {
							String[] splited = deleteRecord.split("_");
							deleteParametersList.add(splited);
						}
						String sqlDelete = "DELETE FROM adprovincetopn WHERE timestamp = ? AND province = ? ";
						JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
						jdbcWrapper.doBatch(sqlDelete, deleteParametersList);

						//update: timestamp,adID,province,clickedCount
						ArrayList<Object[]> insertParametersList = new ArrayList<>();
						for (AdProvinceTopN insertRecord : adProvinceTopN) {
							insertParametersList.add(new Object[]{
									insertRecord.getTimestamp(),
									insertRecord.getAdID(),
									insertRecord.getProvince(),
									insertRecord.getClickedCount()
							});
						}
						jdbcWrapper.doBatch("INSERT INTO adprovincetopn VALUES(?,?,?,?) ", insertParametersList);
					}
				});
			}
		});


		/**
		 * 计算过去半个小时内广告点击的趋势:
		 * 用户广告点击信息:timestamp,ip,userID,adID,province,city
		 * 使用mapToPair对filteredADClickedStreaming中kafka的key部分过滤掉,并将value转化为data,hour,minute,adId,clickCount
		 */
		filteredADClickedStreaming.mapToPair(new PairFunction<Tuple2<String, String>, String, Long>() {

			@Override
			public Tuple2<String, Long> call(Tuple2<String, String> t) throws Exception {
				String[] splited = t._1.split("\t");
				//TODO 重构代码实现时间戳和分钟的转换提取,此处需要提取出该广告的点击分钟单位
				String time = splited[0];//以分钟为单位,10s的若干倍,便于聚合
				String adID = splited[3];

				return new Tuple2<>(time + "_" + adID, 1L);
			}
		}).reduceByKeyAndWindow(new Function2<Long, Long, Long>() { //reduceByKeyAndWindow的高效实现统计过去30min
			@Override
			public Long call(Long v1, Long v2) throws Exception {
				return v1 + v2;
			}
		}, new Function2<Long, Long, Long>() {
			@Override
			public Long call(Long v1, Long v2) throws Exception {
				return v1 - v2;
			}
		}, Durations.minutes(30), Durations.minutes(5) //30分钟Duration窗口,5分钟滑动
		).foreachRDD(new VoidFunction<JavaPairRDD<String, Long>>() {//foreachRDD将数据持久化
			@Override
			public void call(JavaPairRDD<String, Long> rdd) throws Exception {

				// partition级别操作
				rdd.foreachPartition(new VoidFunction<Iterator<Tuple2<String, Long>>>() {
					@Override
					public void call(Iterator<Tuple2<String, Long>> partition) throws Exception {

						// 针对3个关键维度(时间(年月日,时,分),广告id,点击次数)构建javaBean
						List<AdTrendStat> adTrendList = new ArrayList<>();
						while (partition.hasNext()) {
							Tuple2<String, Long> record = partition.next();
							String[] splited = record._1.split("_");
							String time = splited[0];
							String adID = splited[1];
							Long clickedCount = record._2;
							/**
							 * 在插入数据到数据库的时候具体需要字段:time,adID,clickedCount
							 * 而我们通过j2ee技术进行趋势绘图的时候,肯定是需要年/月/日/时/分,这几个维度的
							 * 所以我们在这里需要年,月,日,小时,分钟这些时间维度;
							 */
							AdTrendStat adTrendStat = new AdTrendStat();
							adTrendStat.setAdID(adID);
							adTrendStat.setClickedCount(clickedCount);

							//TODO 获取年月日data,小时hour,分钟minute
							adTrendStat.set_data(time);//格式化的年月日
							adTrendStat.set_hour(time);
							adTrendStat.set_minute(time);

							adTrendList.add(adTrendStat);
						}

						// update/insert


						JDBCWrapper jdbcWrapper = JDBCWrapper.getJDBCInstance();
						// 判断DB中是否已经存在该广告的点击记录:data,hour,minute,adID,clickedCount
						/**
						 * 先判断DB的adclickedtrend是否存在该Ad的点击记录,如果有使用update,如果没有使用insert
						 */

						final List<AdTrendStat> inserting = new ArrayList<>();
						final List<AdTrendStat> updating = new ArrayList<>();
						for (final AdTrendStat clicked : adTrendList) {
							final AdTrendCountHistory adTrendCountHistory = new AdTrendCountHistory();

							jdbcWrapper.doQuery("SELECT clickedCount FROM adclickedtrend WHERE " +
											"data = ? AND hour = ? AND minute = ? AND adID = ? ",
									new Object[]{clicked.get_data(), clicked.get_hour(), clicked.get_minute(), clicked.getAdID()},
									new ExecuteCallBack() {
										@Override
										public void resultCallBack(ResultSet result) throws SQLException {
											if (result.next()) {
												long count = result.getLong(1);//获取result的第一个字段的值
												adTrendCountHistory.setClickedCountHistory(count);
												updating.add(clicked);
											} else {
												//clicked.setClickedCount(1L);
												inserting.add(clicked);
											}
										}
									});
						}

						ArrayList<Object[]> inserParametersList = new ArrayList<>();
						for (AdTrendStat insertRecord : inserting) {
							inserParametersList.add(new Object[]{
									insertRecord.get_data(),
									insertRecord.get_hour(),
									insertRecord.get_minute(),
									insertRecord.getAdID(),
									insertRecord.getClickedCount()
							});
						}
						//data,hour,minute,adID,clickedCount
						jdbcWrapper.doBatch("INSERT INTO adclickedtrend VALUES(?,?,?,?,?)", inserParametersList);


						ArrayList<Object[]> updateParametersList = new ArrayList<>();
						for (AdTrendStat updateRecord : updating) {
							updateParametersList.add(new Object[]{
									updateRecord.getClickedCount(),
									updateRecord.get_data(),
									updateRecord.get_hour(),
									updateRecord.get_minute(),
									updateRecord.getAdID()
							});
						}
						jdbcWrapper.doBatch("UPDATE adclickedtrend SET clickedCount = ? WHERE" +
								"data =? AND hour = ? AND minute = ? AND adID = ? ", updateParametersList);


					}
				});
			}
		});


		jssc.start();
		jssc.awaitTermination();
		jssc.close();
	}


}

class JDBCWrapper {

	private static JDBCWrapper jdbcInstance = null;
	// 线程安全
	private static LinkedBlockingDeque<Connection> dbConnectionPool = new LinkedBlockingDeque<>();

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static JDBCWrapper getJDBCInstance() {

		// 高效的单例
		if (jdbcInstance == null) {
			synchronized (JDBCWrapper.class) {
				if (jdbcInstance == null) {
					jdbcInstance = new JDBCWrapper();
				}
			}
		}
		return jdbcInstance;
	}

	private JDBCWrapper() {

		for (int i = 0; i < 10; i++) {
			try {
				Connection conn = DriverManager.getConnection("jdbc:mysql://hadoop:3306/streaming", "root", "root");

				dbConnectionPool.put(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// 保证线程只获得一次实例
	public synchronized Connection getConnection() {
		while (dbConnectionPool.size() == 0) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return dbConnectionPool.poll();
	}

	/**
	 * 批量
	 */
	public int[] doBatch(String sqlText, List<Object[]> paramsList) {

		Connection conn = getConnection();
		PreparedStatement preparedStatement = null;
		int[] result = null;
		try {
			conn.setAutoCommit(false);//关闭自动提交
			preparedStatement = conn.prepareStatement(sqlText);
			for (Object[] params : paramsList) {
				for (int i = 0; i < params.length; i++) {
					preparedStatement.setObject(i + 1, params[i]);
				}
				preparedStatement.addBatch();
			}
			result = preparedStatement.executeBatch();

			//callBack.resultCallBack(result);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (conn != null) {
				try {
					dbConnectionPool.put(conn);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
		return result;
	}

	/**
	 * 查询
	 *
	 * @param sqlText  sql语句
	 * @param callBack 定义接口进行回调result,具体使用时候直接回调(模仿java通过回调实现scala的方法)
	 */
	public void doQuery(String sqlText, Object[] paramsArray, ExecuteCallBack callBack) {

		Connection conn = getConnection();
		PreparedStatement preparedStatement = null;
		ResultSet[] resultSets = null;

		ResultSet result = null;
		try {
			conn.setAutoCommit(false);//关闭自动提交
			preparedStatement = conn.prepareStatement(sqlText);
			for (int i = 0; i < paramsArray.length; i++) {
				preparedStatement.setObject(i + 1, paramsArray[i]);
			}
			preparedStatement.addBatch();
			result = preparedStatement.executeQuery();

			/**
			 * 通过传入callBack,讲结果回调
			 */
			callBack.resultCallBack(result);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (conn != null) {
				try {
					dbConnectionPool.put(conn);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}

}

/**
 *
 */
interface ExecuteCallBack {

	public void resultCallBack(ResultSet result) throws SQLException;
}

class UserAdClicked {

	private String timestamp;
	private String ip;
	private String userID;
	private String adID;
	private String province;
	private String city;
	private Long clickedCount;

	public Long getClickedCount() {
		return clickedCount;
	}

	public void setClickedCount(Long clickedCount) {
		this.clickedCount = clickedCount;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getAdID() {
		return adID;
	}

	public void setAdID(String adID) {
		this.adID = adID;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}


}

class AdClicked {

	private String timestamp;
	private String adID;
	private String province;
	private String city;
	private Long clickedCount;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getAdID() {
		return adID;
	}

	public void setAdID(String adID) {
		this.adID = adID;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Long getClickedCount() {
		return clickedCount;
	}

	public void setClickedCount(Long clickedCount) {
		this.clickedCount = clickedCount;
	}
}

class AdProvinceTopN {
	private String timestamp;
	private String adID;
	private String province;
	private Long clickedCount;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getAdID() {
		return adID;
	}

	public void setAdID(String adID) {
		this.adID = adID;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public Long getClickedCount() {
		return clickedCount;
	}

	public void setClickedCount(Long clickedCount) {
		this.clickedCount = clickedCount;
	}
}

class AdTrendStat {

	private String adID;
	private Long clickedCount;
	private String _data;
	private String _hour;
	private String _minute;

	public String getAdID() {
		return adID;
	}

	public void setAdID(String adID) {
		this.adID = adID;
	}

	public Long getClickedCount() {
		return clickedCount;
	}

	public void setClickedCount(Long clickedCount) {
		this.clickedCount = clickedCount;
	}

	public String get_data() {
		return _data;
	}

	public void set_data(String _data) {
		this._data = _data;
	}

	public String get_hour() {
		return _hour;
	}

	public void set_hour(String _hour) {
		this._hour = _hour;
	}

	public String get_minute() {
		return _minute;
	}

	public void set_minute(String _minute) {
		this._minute = _minute;
	}
}
class AdTrendCountHistory{
	private Long clickedCountHistory;

	public Long getClickedCountHistory() {
		return clickedCountHistory;
	}

	public void setClickedCountHistory(Long clickedCountHistory) {
		this.clickedCountHistory = clickedCountHistory;
	}
}