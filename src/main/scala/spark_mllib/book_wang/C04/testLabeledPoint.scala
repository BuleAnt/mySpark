import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * 4-2 标签的使用
  * 向量标签对于MLlib中机器学习算法的不同值做标记.
  * 例如分类问题中,可将不同的数据集分成若干份,以整型数0,1,2...进行标记.
  * 即程序的编写者可以根据自己的需要对数据进行标记.
  *
  * LabeledPoint是建立向量标签的静态类,主要有两个方法,
  * Features用于显示打印标记点所代表的数据内容,而Label用于显示标记数

  */
object testLabeledPoint {
  def main(args: Array[String]) {
    //建立密集向量
    val vd: Vector = Vectors.dense(2, 0, 6)
    val pos = LabeledPoint(1, vd) //对密集向量建立标记点
    println(pos.features) //打印标记点内容数据
    //打印既定标记
    println(pos.label)
    //建立稀疏向量
    val vs: Vector = Vectors.sparse(4, Array(0, 1, 2, 3), Array(9, 5, 2, 7))
    val neg = LabeledPoint(2, vs) //对密集向量建立标记点
    println(neg.features) //打印标记点内容数据
    println(neg.label) //打印既定标记
  }
}
