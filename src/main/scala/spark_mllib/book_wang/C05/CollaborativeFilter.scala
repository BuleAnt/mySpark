package spark_mllib.book_wang.C05

import org.apache.spark._
import org.apache.spark.mllib.recommendation.{ALS, Rating}

/**
  * 5-2 ALS
  * MLLib中ALS算法有固定的数据格式,Rating是固定的ALS输入格式,
  * 他要求是一个与元组的数据类型,其中数值分别为[Int,Int,Double]
  * 因此在数据集建立时,用户名和物品名分别用数值代替,二最后的评分没有变化
  * 第二部就是建立als数据模型,ALS数据模型是根据数据集训练tran获得,
  * train有若干个采纳数构成,
  * numBlocks; 并行计算的block数(-1为自动配置);
  * rank: 模型中隐藏因子数
  * iterations:算法迭代次数
  * lambda:ALS中的正则化参数
  * implicitPref:使用显示反馈ALS变量或隐式反馈
  * alpha:ALS隐式反馈变化率用于控制每次拟合修正的幅度
  */
object CollaborativeFilter {
  def main(args: Array[String]) {
    //设置环境变量
    val conf = new SparkConf().setMaster("local").setAppName("CollaborativeFilter ")
    //实例化环境
    val sc = new SparkContext(conf)
    //设置数据集
    val data = sc.textFile("data/u1.txt")
    //处理数据
    val ratings = data.map(_.split(' ') match {
      case Array(user, item, rate) => //将数据集转化
        Rating(user.toInt, item.toInt, rate.toDouble) //将数据集转化为专用Rating
    })
    //设置隐藏因子
    val rank = 2
    //设置迭代次数
    val numIterations = 2
    //进行模型训练
    val model = ALS.train(ratings, rank, numIterations, 0.01)
    var rs = model.recommendProducts(2, 1) //为用户2推荐一个商品
    rs.foreach(println) //打印结果
  }
}
