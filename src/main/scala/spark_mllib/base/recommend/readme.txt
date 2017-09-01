协同过滤
http://www.cnblogs.com/pinard/p/6349233.html
关联算法做协同过滤
	频繁集挖掘，找到满足支持度阈值的关联物品的频繁N项集或者序列。
	将频繁项集或序列里的其他物品按一定的评分准则推荐给用户，这个评分准则可以包括支持度，置信度和提升度等
	Apriori
	http://www.cnblogs.com/pinard/p/6293298.html
	FP Tree算法
	http://www.cnblogs.com/pinard/p/6307064.html
	PrefixSpan算法
	http://www.cnblogs.com/pinard/p/6323182.html

聚类算法做协同过滤
	基于用户或者项目的协同过滤
	K-Means,
	http://www.cnblogs.com/pinard/p/6164214.html
	BIRCH,
	http://www.cnblogs.com/pinard/p/6179132.html
	DBSCAN
	http://www.cnblogs.com/pinard/p/6208966.html
	谱聚类
	http://www.cnblogs.com/pinard/p/6221564.html

分类算法做协同过滤
	根据用户评分的高低，将分数分成几段
	逻辑回归
	http://www.cnblogs.com/pinard/p/6029432.html
	朴素贝叶斯
	http://www.cnblogs.com/pinard/p/6069267.html

回归算法做协同过滤
	评分可以是一个连续的值而不是离散的值，通过回归模型我们可以得到目标用户对物品的预测打分
	线性回归
		http://www.cnblogs.com/pinard/p/6004041.html
	决策树算法
		http://www.cnblogs.com/pinard/p/6053344.html
	支持向量机原理(五)线性支持回归
		http://www.cnblogs.com/pinard/p/6113120.html

矩阵分解做协同过滤
	目前使用也很广泛的一种方法
		传统的奇异值分解SVD要求矩阵不能有缺失数据，必须是稠密的，
		而我们的用户物品评分矩阵是一个很典型的稀疏矩阵，直接使用传统的SVD到协同过滤是比较复杂的。
	目前主流的矩阵分解推荐算法主要是SVD的一些变种，比如FunkSVD，BiasSVD和SVD++。
	这些算法和传统SVD的最大区别是不再要求将矩阵分解为UΣVT的形式，而变是两个低秩矩阵PTQ的乘积形式。


理论基础:
	奇异值分解(SVD)原理与在降维中的应用
	http://www.cnblogs.com/pinard/p/6251584.html
	矩阵分解在协同过滤推荐算法中的应用:
	http://www.cnblogs.com/pinard/p/6351319.html
	矩阵分解模型:
	http://www.cnblogs.com/AngelaSunny/p/5230781.html
	Kaggle 竞赛之广告推荐FM算法
	搜索、推荐和广告的大融合也是未来推荐系统的发展趋势之一
	分解机(Factorization Machines)推荐算法原理
	http://www.cnblogs.com/pinard/p/6370127.html


ALS
	http://www.cnblogs.com/txq157/p/6111593.html
	http://www.csdn.net/article/2015-05-07/2824641
	http://blog.csdn.net/oucpowerman/article/details/49847979
	http://blog.csdn.net/antkillerfarm/article/details/53734658
	http://snglw.blog.51cto.com/5832405/1662153

spark推荐:
	用Spark学习FP Tree算法和PrefixSpan算法
	http://www.cnblogs.com/pinard/p/6340162.html
	用Spark学习矩阵分解推荐算法
	http://www.cnblogs.com/pinard/p/6364932.html
	Spark机器学习之协同过滤
	http://blog.csdn.net/cheng9981/article/details/70142900
	基于Spark ALS的离线推荐系统实践
	http://blog.csdn.net/sctu_vroy/article/details/52957158
	推荐系统_Spark实践
	http://www.cnblogs.com/muchen/p/6882465.html
	http://www.cnblogs.com/xiaoyesoso/p/5570079.html
	基于Spark Mllib，SparkSQL的电影推荐系统
	http://blog.csdn.net/qq1010885678/article/details/46052055
	基于Spark机器学习和实时流计算的智能推荐系统
	http://blog.csdn.net/qq1010885678/article/details/46675501
	git@github.com:gaoxuesong/SparkRecommender.git
	git@github.com:LeechanX/Netflix-Recommender-with-Spark.git
	流程图:
	http://blog.csdn.net/u013749540/article/details/51755067
	https://www.zhihu.com/question/31319140
	spark基于用户的协同过滤算法与坑点，提交job
	http://blog.csdn.net/wangqi880/article/details/52883455
	Spark机器学习之协同过滤算法
	http://www.cnblogs.com/ksWorld/p/6808092.html
	ALS 在 Spark MLlib 中的实现
	http://www.dataguru.cn/article-7049-1.html
	# spark 2.0 新
	http://blog.csdn.net/wangqi880/article/details/52923356
	# spark fp关联
	http://blog.csdn.net/wangqi880/article/details/52910078
	# 推荐定时任务主要流程
	http://blog.csdn.net/jthink_/article/details/49127573
	
	https://www.ibm.com/developerworks/cn/views/web/libraryview.jsp?view_by=search&sort_by=Date&sort_order=desc&view_by=Search&search_by=%E6%8E%A2%E7%B4%A2%E6%8E%A8%E8%8D%90%E5%BC%95%E6%93%8E%E5%86%85%E9%83%A8%E7%9A%84%E7%A7%98%E5%AF%86&dwsearch.x=12&dwsearch.y=11&dwsearch=Go
	推荐系统源码
	http://download.csdn.net/download/qq1010885678/8858315
	推荐系统算法架构及Spark的应用.pdf
	http://doc.mbalib.com/view/3ebcacb4aa9ecd6f17919cb92e309233.html
	spark 学习系列
	http://blog.csdn.net/u013063153/article/category/6495611/7
	july机器学习算法班
    http://blog.csdn.net/zhzhji440/article/category/2789587
案例:
	美团推荐算法实践:
	https://tech.meituan.com/mt-recommend-practice.html
	http://blog.csdn.net/wangqi880/article/details/49838619
