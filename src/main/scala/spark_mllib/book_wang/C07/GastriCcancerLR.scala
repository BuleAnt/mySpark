package spark_mllib.book_wang.C07

import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkContext, SparkConf}

/**
  * 7-4 胃癌的转移判断
  * 某研究人员咋探讨肾细胞癌转移的有关临床病理因素研究中,手机了一批行根治性肾切除手术患者的肾癌标本资料,
  * 现从中抽取26例资料作为示例进行logistic回归分析
  * 数据说明: y x1 x2 x3 x4 x5
  * y: 肾细胞癌转移情况(有转移 y=1;无转移 y=0);
  * x1: 确诊时患者的年龄(岁);
  * x2: 肾细胞癌血管内皮生长因子(VEGF),其阳性表述由低到高共3个等级;
  * x3: 肾细胞核租住学分级,由低到高4级;
  * x5: 肾细胞癌分期,由低到高共4期
  * 将数据建立在
  */
object GastriCcancerLR {
  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("LogisticRegression4")
    //设定名称
    val sc = new SparkContext(conf) //创建环境变量实例

    val data = MLUtils.loadLibSVMFile(sc, "data/wa.txt")
    //获取数据集
    val splits = data.randomSplit(Array(0.7, 0.3), seed = 11L)
    //对数据集切分
    val parsedData = splits(0)
    //分割训练数据
    val parseTtest = splits(1)
    //分割测试数据
    val model = LogisticRegressionWithSGD.train(parsedData, 50) //训练模型

    val predictionAndLabels = parseTtest.map {
      //计算测试值
      case LabeledPoint(label, features) => //计算测试值
        val prediction = model.predict(features) //计算测试值
        (prediction, label) //存储测试和预测值
    }

    val metrics = new MulticlassMetrics(predictionAndLabels)
    //创建验证类
    val precision = metrics.precision //计算验证值
    println("Precision = " + precision) //打印验证值

    val patient = Vectors.dense(Array(70, 3, 180.0, 4, 3)) //计算患者可能性
    if (patient == 1) println("患者的胃癌有几率转移。") //做出判断
    else println("患者的胃癌没有几率转移。") //做出判断
  }
}
