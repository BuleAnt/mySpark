import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.PowerIterationClustering
import org.apache.spark.rdd.RDD

object PIC {
  def main(args: Array[String]) {

    val conf = new SparkConf() //创建环境变量
      .setMaster("local") //设置本地化处理
      .setAppName("PIC ")
    //设定名称
    val sc = new SparkContext(conf)
    //创建环境变量实例
    val data = sc.textFile("c://u2.txt")
    //读取数据
    val pic = new PowerIterationClustering() //创建专用类
      .setK(3) //设定聚类数
      .setMaxIterations(20)
    //设置迭代次数
    val model = pic.run(data.asInstanceOf[RDD[(Long, Long, Double)]]) //创建模型
  }
}
