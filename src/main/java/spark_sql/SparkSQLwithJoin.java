package spark_sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;


/**
 * SparkSQLwithJoin
 */
public class SparkSQLwithJoin {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("SparkSQLwithJoin");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);

        //针对json文件数据源来创建DataFrame
        DataFrame peoplesDF = sqlContext.read().json("src/main/resources/my_peoples.json");

        //基于Json构建的DataFrame来注册临时表
        peoplesDF.registerTempTable("peopleScores");

        //查询出score>90的学生
        DataFrame execellentScoresDF = sqlContext.sql("select name,score from peopleScores where score > 90");
        //execellentScoresDF.show();

        //在DataFrame基础上转化为RDD,通过Map操作计算分数大于90的所有人的name
        List<String> execellentScoresList = execellentScoresDF.javaRDD().map(new Function<Row, String>() {
            @Override
            public String call(Row row) throws Exception {
                return row.getAs("name");
            }
        }).collect();


        //动态组拼出Json
        List<String> peopleInformations = new ArrayList<String>();
        peopleInformations.add("{\"name\":\"Michael\",\"age\":20}");
        peopleInformations.add("{\"name\":\"Michael\",\"age\":17}");
        peopleInformations.add("{\"name\":\"Michael\",\"age\":19}");
        //通过内容为Json的RDD来构建DataFrame
        JavaRDD<String> peopleInformationRDD = sc.parallelize(peopleInformations);
        DataFrame peopleInformationDF = sqlContext.read().json(peopleInformationRDD);
        //注册成为临时表,两个临时表不能直接一起查询操作
        peopleInformationDF.registerTempTable("peopleInformations");

        //从动态拼组Json转化的DF表peopleInformations中过滤:
        // where name in ('Michael','Andy')
        String sqlText = "select name,age from peopleInformations where name in (";
        for (int i = 0; i < execellentScoresList.size(); i++) {
            sqlText += " '" + execellentScoresList.get(i) + "'";
            if (i < (execellentScoresList.size() - 1)) {
                sqlText += ",";
            }
        }
        sqlText += ")";
        DataFrame execellentNameAgeDF = sqlContext.sql(sqlText);
        //System.out.println(sqlText);
        //execellentNameAgeDF.show();

        // execellentScoresDF-->RowRDD-->PairRDD(name,score)
        // join
        // execellentNameAgeDF-->RowRDD-->PairRDD(name,age)
        // PairRDD的join以key相等为条件
        JavaPairRDD<String, Tuple2<Integer, Integer>> resultRDD =
                execellentScoresDF.javaRDD().mapToPair(new PairFunction<Row, String, Integer>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<String, Integer> call(Row row) throws Exception {
                        //Integer.valueOf(String.valueOf(row.getAs("score")))
                        return new Tuple2<String, Integer>((String) row.getAs("name"), Integer.valueOf(String.valueOf(row.getAs("score"))));
                    }
                }).join(execellentNameAgeDF.javaRDD().mapToPair(new PairFunction<Row, String, Integer>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<String, Integer> call(Row row) throws Exception {
                        return new Tuple2<String, Integer>((String) row.getAs("name"), Integer.valueOf(String.valueOf(row.getAs("age"))));
                    }
                }));

        // PairRDD-->RDD name,(age,score)
        JavaRDD<Row> resultRowRDD =
                resultRDD.map(new Function<Tuple2<String, Tuple2<Integer, Integer>>, Row>() {

                    @Override
                    public Row call(Tuple2<String, Tuple2<Integer, Integer>> tuple) throws Exception {
                        return RowFactory.create(tuple._1, tuple._2._2, tuple._2._1);
                    }
                });

        // RDD-->DataFrame,先组装RDD的StructField
        List<StructField> structFields = new ArrayList<StructField>();
        structFields.add(DataTypes.createStructField("name", DataTypes.StringType, true));
        structFields.add(DataTypes.createStructField("age", DataTypes.IntegerType, true));
        structFields.add(DataTypes.createStructField("score", DataTypes.IntegerType, true));
        // 在构建StructType，用于最后DataFrame元数据的描述
        StructType structType = DataTypes.createStructType(structFields);

        DataFrame personsDF = sqlContext.createDataFrame(resultRowRDD, structType);

        personsDF.show();
        personsDF.write().format("json").save("target/out/personsResult");
    }
}