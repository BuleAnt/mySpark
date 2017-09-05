package spark_mllib.demo.movie_recommend

import java.util.Random
import java.util.logging.{Level, Logger}

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.recommendation._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.Map
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Administrator on 2017/9/5 0005.
  */
object countForPush {

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARNING)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)
    val conf = new SparkConf().setAppName("Recommender").setMaster("local").setExecutorEnv("spark.executor.memory", "3072m").setExecutorEnv("SPARK_DRIVER_MEMORY", "3072m")
    val sc = new SparkContext(conf)
    val base = "F:/datafile/lisence/"
    val rawUserArtistData = sc.textFile(base + "user_artist_data.txt")
    //用户产品点击表加载
    val rawArtistData = sc.textFile(base + "artist_data.txt")
    //产品数据表加载
    val rawArtistAlias = sc.textFile(base + "artist_alias.txt") //误差名字转换表加载
    evaluate(sc, rawUserArtistData, rawArtistData, rawArtistAlias)
    sc.stop()
  }

  //把产品中不规范的数据建立映射
  def buildArtistAlias(rawArtistAlias: RDD[String]): collection.Map[Int, Int] =
    rawArtistAlias.flatMap { line =>
      val tokens = line.split('\t')
      if (tokens(0).isEmpty) {
        None
      } else {
        Some((tokens(0).toInt, tokens(1).toInt))
      }
    }.collectAsMap()

  def evaluate(
                sc: SparkContext,
                rawUserArtistData: RDD[String],
                rawArtistData: RDD[String],
                rawArtistAlias: RDD[String]): Unit = {
    val bArtistAlias = sc.broadcast(buildArtistAlias(rawArtistAlias))
    //Broadcast（广播）是相对较为常用方法功能，通常使用方式，包括共享配置文件，map数据集，树形数据结构等，为能够更好更快速为TASK任务使用相关变量。
    val allData = buildRatings(rawUserArtistData, bArtistAlias)
    //转换格式不正确数据，并构建Rating
    val Array(trainData, cvData) = allData.randomSplit(Array(0.9, 0.1))
    //数据按比率随机分块，90%用户训练，10%真实数据验证
    trainData.cache()
    //缓存训练数据
    cvData.cache()
    //缓存真实数据

    val allItemIDs = allData.map(_.product).distinct().collect()
    val bAllItemIDs = sc.broadcast(allItemIDs)
    //把用户中的产品所有的数据集合存起来

    var bestauc = Double.MinValue
    var bestModel: Option[MatrixFactorizationModel] = None
    //最好模型
    for (rank <- Array(10, 50);
         lambda <- Array(1.0, 0.0001);
         alpha <- Array(1.0, 40.0)) {
      val model = ALS.trainImplicit(trainData, rank, 10, lambda, alpha)
      //模型训练
      val auc = areaUnderCurve(cvData, bAllItemIDs, model)
      //求auc
      unpersist(model)
      if (bestauc < auc) {
        bestauc = auc
        bestModel = Some(model)
      }
    }

    trainData.unpersist()
    cvData.unpersist()

    val artistByID = buildArtistByID(rawArtistData)

    val someUsers = allData.map(_.user).distinct().take(5)
    //这里取5个用户
    val someRecommendations = someUsers.map(userID => bestModel.get.recommendProducts(userID, 5)) //推荐5个产品给用户

    someRecommendations.map(
      recs => recs.head.user + " -> " + artistByID.filter { case (id, name) => recs.map(_.product).contains(id) }.values.collect().mkString(", ")
    ).foreach(println)
    allData.unpersist()
    unpersist(bestModel.get)
  }

  def areaUnderCurve(
                      positiveData: RDD[Rating],
                      bAllItemIDs: Broadcast[Array[Int]],
                      model: MatrixFactorizationModel) = {

    val positiveUserProducts = positiveData.map(r => (r.user, r.product))
    val positivePredictions = model.predict(positiveUserProducts).groupBy(_.user)
    val negativeUserProducts = positiveUserProducts.groupByKey().mapPartitions {
      userIDAndPosItemIDs => {
        val random = new Random()
        val allItemIDs = bAllItemIDs.value
        userIDAndPosItemIDs.map { case (userID, posItemIDs) =>
          val posItemIDSet = posItemIDs.toSet
          //此为一个产品序列
          val negative = new ArrayBuffer[Int]()
          var i = 0
          while (i < allItemIDs.size && negative.size < posItemIDSet.size) {
            //按每个产品序列长度遍历
            val itemID = allItemIDs(random.nextInt(allItemIDs.size))
            //随机一个用户列表里面的产品
            if (!posItemIDSet.contains(itemID)) {
              negative += itemID
              //向数组中加入与原来不一样的产品
            }
            i += 1
          }
          negative.map(itemID => (userID, itemID))
        }
      }
    }.flatMap(t => t)
    val negativePredictions = model.predict(negativeUserProducts).groupBy(_.user)
    //对用户和产品进行点击量预测
    positivePredictions.join(negativePredictions).values.map {
      case (positiveRatings, negativeRatings) =>
        var correct = 0L
        var total = 0L
        for (positive <- positiveRatings;
             negative <- negativeRatings) {
          if (positive.rating > negative.rating) {
            correct += 1
          }
          total += 1
        }
        correct.toDouble / total
    }.mean()
    //AUC计算
  }

  //去除缓存
  def unpersist(model: MatrixFactorizationModel): Unit = {
    model.userFeatures.unpersist()
    model.productFeatures.unpersist()
  }

  //构建Rating
  def buildRatings(
                    rawUserArtistData: RDD[String], //未转换用户产品信息表
                    bArtistAlias: Broadcast[Map[Int, Int]]) = {
    rawUserArtistData.map { line =>
      val Array(userID, artistID, count) = line.split(' ').map(_.toInt)
      val finalArtistID = bArtistAlias.value.getOrElse(artistID, artistID)
      //如果匹配返回artistID匹配到的对象，没匹配到返回artistID（原来值）
      Rating(userID, finalArtistID, count)
    }
  }

  //产品数据
  def buildArtistByID(rawArtistData: RDD[String]) =
    rawArtistData.flatMap { line =>
      val (id, name) = line.span(_ != '\t')
      if (name.isEmpty) {
        None
      } else {
        try {
          Some((id.toInt, name.trim))
        } catch {
          case e: NumberFormatException => None
        }
      }
    }


}