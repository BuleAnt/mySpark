spark mllib 机器学习实践

Rdd是MLlib专用的数据格式,他参考了scala的函数式变成思想,并大胆因

基本统计量
数理统计中,基本统计量包括数据的平均值,方差.这是一组求数据统计量的基本内容.
在MLlib中,统计量的计算主要用到Statistics类库.
它的主要包括内容有:
	colStats: 以列为基础计算统计量的基本数据
	chiSqTest: 对数据集内的数据进行皮尔逊距离计算,根据参数的不同,返回格式有差异
	corr: 对两个数据集进行相关系数计算,根据参量的不同,返回格式有差异