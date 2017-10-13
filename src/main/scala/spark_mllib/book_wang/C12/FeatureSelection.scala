package spark_mllib.book_wang.C12

import org.apache.spark.mllib.feature.ChiSqSelector
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 12-3 基于卡方检验的特征选择
  */
object FeatureSelection {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local").setAppName("FeatureSelection ")
    val sc = new SparkContext(conf)
    val data = MLUtils.loadLibSVMFile(sc, "data/fs.txt")
    //创建数据处理空间
    val discretizedData = data.map { lp =>
      LabeledPoint(lp.label, Vectors.dense(lp.features.toArray.map { x => x / 2 }))
    }
    //创建选择2个特性的卡方检验实例
    val selector = new ChiSqSelector(2)
    //创建训练模型
    val transformer = selector.fit(discretizedData)
    //过滤前2个特性
    val filteredData = discretizedData.map { lp =>
      LabeledPoint(lp.label, transformer.transform(lp.features))
    }

    filteredData.foreach(println)
  }
}


