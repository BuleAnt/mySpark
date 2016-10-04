package spark_sql;

import java.util.ArrayList;
import java.util.List;

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

public class DataFrameWrite {

    private void mian() {
        SparkConf conf = new SparkConf().setMaster("local").setAppName(
                "DataFrameWrite");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);

        DataFrame execellentScoresDF = sqlContext
                .sql("select name ,score from peopleScores where xx");

        List<String> execellentScoresNameList = execellentScoresDF.javaRDD()
                .map(new Function<Row, String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String call(Row row) throws Exception {

                        return row.getAs("name");
                    }

                }).collect();
        // 动态组拼JSON
        List<String> peopleInformations = new ArrayList<String>();
        peopleInformations.add("{\"name\":\"Michael\",\"age\":20}");
        peopleInformations.add("{\"name\":\"Andy\",\"age\":17}");
        peopleInformations.add("{\"name\":\"Justin\",\"age\":19}");

        // 通过内容为JSON的RDD来构造DataFrame
        JavaRDD<String> peopleInformationsRDD = sc
                .parallelize(peopleInformations);
        DataFrame peopleInformationsDF = sqlContext.read().json(
                peopleInformationsRDD);
        // 注册成临时表
        peopleInformationsDF.registerTempTable("peopleInformations");

        String sqlText = "select name,age from peopleInformations where name in(";
        for (int i = 0; i < execellentScoresNameList.size(); i++) {
            sqlText += "'" + execellentScoresNameList.get(i) + "'";
            if (i < execellentScoresNameList.size() - 1) {
                sqlText += ",";
            }
        }
        sqlText += ")";
        DataFrame execellentNameAgeDF = sqlContext.sql(sqlText);
        JavaPairRDD<String, Tuple2<Integer, Integer>> resultRDD = execellentScoresDF
                .javaRDD()
                .mapToPair(new PairFunction<Row, String, Integer>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<String, Integer> call(Row row)
                            throws Exception {
                        return new Tuple2<String, Integer>((String) row
                                .getAs("name"), (int) row.getLong(1));
                    }
                })
                .join(execellentNameAgeDF.javaRDD().mapToPair(
                        new PairFunction<Row, String, Integer>() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public Tuple2<String, Integer> call(Row row)
                                    throws Exception {
                                return new Tuple2<String, Integer>((String) row
                                        .getAs("name"), (int) row.getLong(1));
                            }
                        }));
        JavaRDD<Row> reusltRowRDD = resultRDD
                .map(new Function<Tuple2<String, Tuple2<Integer, Integer>>, Row>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Row call(
                            Tuple2<String, Tuple2<Integer, Integer>> tuple)
                            throws Exception {

                        return RowFactory.create(tuple._1, tuple._2._2,
                                tuple._2._1);
                    }
                });

        List<StructField> structFields = new ArrayList<StructField>();
        structFields.add(DataTypes.createStructField("name",
                DataTypes.StringType, true));
        structFields.add(DataTypes.createStructField("age",
                DataTypes.IntegerType, true));
        structFields.add(DataTypes.createStructField("score",
                DataTypes.IntegerType, true));
        StructType structType = DataTypes.createStructType(structFields);
        DataFrame personDF = sqlContext.createDataFrame(reusltRowRDD,
                structType);
        personDF.show();
    }

}
