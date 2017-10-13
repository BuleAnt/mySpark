package spark_mllib.book_wang.C07

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils

/**
  * 7-8 僵尸粉 鉴定
  * 第一步需要对正常用户和虚假用户标记不同的标签,正常用户为1,虚假用户2为0
  * 其次计算向量设置,这里需要选择根据微博使用的特征转化的向量,由于微博用户的使用有一定规律,
  * 例如会经常发微博记录或者大量感兴趣的其他关注,因此可以使用发帖数和注册时间的比值作为第一个参考向量,
  * 同时使用关注用户的数量与注册天数的比值作为第二个参考向量.
  * 同时对于正常的微博用户,还应注册手机作为接受信息使用,因此可以使用是否填写手机作为用户向量的一个判定标准
  * 即可得到一下向量:
  * v = {v1,v2,v3}
  * v1 = 已发微博/注册天数
  * v2 = 好友数量/注册天数
  * v3 = 是否有手机
  * 其中由于v1和v2都是一些列连续的数值,我们可以对其进行人为划分,即:
  * 已发微博/注册天数< 0.05 v1=01
  * 0.05 <= 已发微博/注册天数  < 0.75, v1 = 0
  * 已发微博/注册天数 >= 0.75, v1=1
  * 因此可将v1的向量表示为为(-1,0,1),而v2的向量也可以如此表示,对于v3用-1表示不适用手机,二1表示使用手机
  * 对数据的获取,这里采用人工判定2万个数据作为数据集,经过归并计算,格式如bayes.txt
  * 对于此格式的读取可以采用MLUtils.loadLabeledPoints方法将其判断成LabeledPoint格式今次那个处理,
  * 之后对数据进行处理分割70%的训练数据和30%的测试数据
  * 最后室友通过训练贝叶斯模型计算测试数据,并讲结合真实数据进行比较从而获得模型的验证.
  */
object BayesTest {

  def main(args: Array[String]) {
    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("BayesTest ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val data1 = MLUtils.loadLabeledPoints(sc, "c://data.txt")
    //读取数据集
    val file = sc.textFile("")
    val data = file.map { line => //处理数据
      val parts = line.split(',') //分割数据
      LabeledPoint(parts(0).toDouble, //标签数据转换
        Vectors.dense(parts(1).split(' ').map(_.toDouble))) //向量数据转换
    }

    val splits = data.randomSplit(Array(0.7, 0.3), seed = 11L)
    //对数据进行分配
    val trainingData = splits(0)
    //设置训练数据
    val testData = splits(1)
    //设置测试数据
    val model = NaiveBayes.train(trainingData, lambda = 1.0)
    //训练贝叶斯模型
    val predictionAndLabel = testData.map(p => (model.predict(p.features), p.label))
    //验证模型
    val accuracy = 1.0 * predictionAndLabel.filter(//计算准确度
      label => label._1 == label._2).count() //比较结果
    println(accuracy) //打印准确度
  }
}

