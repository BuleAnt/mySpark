package spark_mll.book_huang

import org.apache.log4j.{Level, Logger}
import breeze.linalg._
import breeze.numerics._
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by noah on 17-8-4.
  */
object bressze_test01 {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("breeze_test01")
    val sc = new SparkContext(conf)
    Logger.getRootLogger.setLevel(Level.WARN)

    // 3.11 breeze 创建函数
    val m1 = DenseMatrix.zeros[Double](2, 3) // 2行3列0矩阵
    val v1 = DenseVector.zeros[Double](3) // 长度为3的0向量
    val v2 = DenseVector.ones(3) // 长度为3,元素为1的向量
    val v3 = DenseVector.fill(3) { // 长度3的元素全部为5.0的向量
      5.0
    }
    val v4 = DenseVector.range(1, 10, 2) // 向量赋值range从1到10,步进为2
    val m2 = DenseMatrix.eye[Double](3) // 3x3单位矩阵
    val v6 = diag(DenseVector(1.0, 2.0, 3.0)) // 对角元素
    val m3 = DenseMatrix((1.0, 2.0),(3.0, 4.0)) // 按行向量赋值
    val v8 = DenseVector(1, 2, 3, 4) // 行向量
    val v9 = DenseVector(1, 2, 3, 4).t //
    val v10 = DenseVector.tabulate(3) { i => 2 * i }
    val m4 = DenseMatrix.tabulate(3, 2) { case (i, j) => i + j }
    val v11 = new DenseVector(Array(1, 2, 3, 4)) // 数组转化向量
    val m5 = new DenseMatrix(2, 3, Array(11, 12, 13, 21, 22, 23)) // 指定行列数,数组转化矩阵
    val v12 = DenseVector.rand(4) // 随机向量
    val m6 = DenseMatrix.rand(2, 3)
    println("----m1----")
    println(m1)
    println("----v1----")
    println(v1)
    println("----v3----")
    println(v3)
    println("----v4----")
    println(v4)
    println("----m2----")
    println(m2)
    println("----v6----")
    println(v6)
    println("----v8----")
    println(v8)
    println("----v9----")
    println(v9)
    println("----v10----")
    println(v10)
    println("----m4----")
    println(m4)
    println("----v11----")
    println(v11)
    println("----m5----")
    println(m5)
    println("----v12----")
    println(v12)
    println("----m6----")
    println(m6)
    println("--------")

    // 3.1.2 Breeze 元素访问以及操作函数
    val a = DenseVector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    a(0) //第一个元素
    a(1 to 4) // 2到5个元素
    a(5 to 0 by -1) //从第6个元素到第一个元素,步进为-1
    a(1 to -1) //第一个元素到最后一个元素
    a(-1) //最后一个元素
    val m = DenseMatrix((1.0, 2.0, 3.0), (3.0, 4.0, 5.0))
    m(0, 1) // 1行2列
    m(::, 1) // 第2列


    // 元素操作
    val m_1 = DenseMatrix((1.0, 2.0, 3.0), (3.0, 4.0, 5.0))
    m_1.reshape(3, 2) // (按列顺序)改为3行2列
    m_1.toDenseVector // (按列顺序)转化为向量

    val m_3 = DenseMatrix((1.0, 2.0, 3.0), (4.0, 5.0, 6.0), (7.0, 8.0, 9.0))
    lowerTriangular(m_3) // 上三角
    upperTriangular(m_3) // 下三角
    m_3.copy // 复制一个矩阵
    diag(m_3) // 对角矩阵
    m(::, 2) := 5.0 // 第3列全部赋值为5.0
    m(1 to 2, 1 to 2) := 5.0 // 2到3行,2到3列赋值

    val a_1 = DenseVector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    a_1(1 to 4) := 5 // 2到5的元素赋值为5
    a_1(1 to 4) := DenseVector(1, 2, 3, 4)

    val a1 = DenseMatrix((1.0, 2.0, 3.0), (4.0, 5.0, 6.0))
    val a2 = DenseMatrix((1.0, 1.0, 1.0), (2.0, 2.0, 2.0))
    DenseMatrix.vertcat(a1, a2) // 矩阵垂直连接
    DenseMatrix.horzcat(a1, a2) // 矩阵横向连接

    val b1 = DenseVector(1, 2, 3, 4)
    val b2 = DenseVector(1, 1, 1, 1)
    DenseVector.vertcat(b1, b2) // 向量垂直连接

    // 3.1.3 Breeze 数值计算函数
    val a_3 = DenseMatrix((1.0, 2.0, 3.0), (4.0, 5.0, 6.0))
    val b_3 = DenseMatrix((1.0, 1.0, 1.0), (2.0, 2.0, 2.0))

    a_3 + b_3 // 元素对应位置加法
    a_3 :* b_3 //元素乘法
    a_3 :/ b_3
    a_3 :< a_3 // 元素比较,返回boolean值
    a_3 :== b_3
    a_3 :+= 1.0 // 元素自加
    a_3 :*= 2.0 // 元素自乘

    max(a_3) // 最大的元素
    argmax(a_3) // 最大元素对应的位置
    DenseVector(1, 2, 3, 4) dot DenseVector(1, 1, 1, 1) // 向量点乘

    // 3.1.4 Breeze 求和函数
    val a_4 = DenseMatrix((1.0, 2.0, 3.0), (4.0, 5.0, 6.0), (7.0, 8.0, 9.0))
    sum(a_4) // 所有元素求和
    sum(a_4, Axis._0) // 按列求和
    sum(a_4, Axis._1) // 按行求和
    trace(a_4) // 对角线求和
    accumulate(DenseVector(1, 2, 3, 4)) // 向量累计和
    // accumulate(a_4)

    // 3.1.5 Breeze 布尔函数
    val a_5 = DenseVector(true, false, true)
    val b_5 = DenseVector(false, true, true)
    a_5 :& b_5 // 布尔与
    a_5 :| b_5 // 布尔或
    !a_5 // 布尔非
    val a_5_2 = DenseVector(1.0, 0.0, -2.0)
    any(a_5_2) // 是否任一是0
    all(a_5_2) // 是否全部是0

    // 3.1.6 Breeze 线性代数函数
    val a_6 = DenseMatrix((1.0, 2.0, 3.0), (4.0, 5.0, 6.0), (7.0, 8.0, 9.0))
    val b_6 = DenseMatrix((1.0, 1.0, 1.0), (1.0, 1.0, 1.0), (1.0, 1.0, 1.0))
    a_6 \ b_6 // 矩阵除法
    a_6.t // 转置矩阵
    det(a_6) //矩阵特征值
    inv(a_6) // 求逆矩阵
    val svd.SVD(u, s, v) = svd(a_6) // 奇异值分解
    a_6.rows // 行
    a_6.cols // 列

    // 3.1.7 Breeze 取整函数
    val a_7 = DenseVector(1.2, 0.6, -2.3)
    round(a_7) // 四舍五入
    ceil(a_7) // 小数进1
    floor(a_7) // 小数略为0
    signum(a_7) // 符号函数
    abs(a_7) // 绝对值
  }
}
