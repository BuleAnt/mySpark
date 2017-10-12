package spark_mllib.book_wang.C06

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 6-3 线性回归实战
  * 数据lr.txt 某商品的需求量(y,吨),价格(x1,元/千克)和消费者收入(x2,元)观测值
  * 要求: 建立需求函数:y = ax1+bx2
  */
object LinearRegression2 {
  val conf = new SparkConf() //创建环境变量
    .setMaster("local") //设置本地化处理
    .setAppName("LinearRegression2 ")
  //设定名称
  val sc = new SparkContext(conf) //创建环境变量实例

  def main(args: Array[String]) {
    val data = sc.textFile("data/lr.txt")
    //获取数据集路径
    val parsedData = data.map { line => //开始对数据集处理
      val parts = line.split('|') //根据逗号进行分区
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(',').map(_.toDouble)))
    }.cache()
    //转化数据格式
    val model = LinearRegressionWithSGD.train(parsedData, 2, 0.1)
    //建立模型
    val result = model.predict(Vectors.dense(2)) //通过模型预测模型
    println(result) //打印预测结果
  }

}

