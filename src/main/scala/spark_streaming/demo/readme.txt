http://www.cnblogs.com/smartloli/p/5560897.html
Flow
	日志节点--存储于消息中间件-->Kafka Cluster--计算模型-->
	Spark Cluster-->持久化计算结果-->DB(HDFS,HBase,Redis,MySQL等)
	通过上图，我们可以看出，首先是采集上报的日志数据，将其存放于消息中间件，
	这里消息中间件采用的是 Kafka，然后在使用计算模型按照业务指标实现相应的计算内容，
	最后是将计算后的结果进行持久化，DB 的选择可以多样化，
	这里笔者就直接使用了 Redis 来作为演示的存储介质，
	大家所示在使用中，可以替换比如将结果存放到 HDFS，HBase Cluster，或是 MySQL 等都行。
	这里，我们使用 Spark SQL 来替换掉 Storm 的业务实现编写。
		See: object IPLoginAnalytics.scala
	上面代码，通过 Thread.sleep() 来控制数据生产的速度。
	接下来，我们来看看如何实现每个用户在各个区域所分布的情况，它是按照坐标分组，平台和用户ID过滤进行累加次数，逻辑用 SQL 实现较为简单，
	关键是在实现过程中需要注意的一些问题，比如对象的序列化问题。
	这里，细节的问题，我们先不讨论，先看下实现的代码，如下所示：
		See:KafkaIPLoginProducer.scala
	我们在开发环境进行测试的时候，使用 local[k] 部署模式，在本地启动 K 个 Worker 线程来进行计算，
	而这 K 个 Worker 在同一个 JVM 中，上面的示例，
	默认使用 local[k] 模式。这里我们需要普及一下 Spark 的架构，架构图来自 Spark 的官网，[链接地址]
		http://spark.apache.org/docs/latest/cluster-overview.html

	这里，不管是在 local[k] 模式，Standalone 模式，还是 Mesos 或是 YARN 模式，
	整个 Spark Cluster 的结构都可以用改图来阐述，只是各个组件的运行环境略有不同，
	从而导致他们可能运行在分布式环境，本地环境，亦或是一个 JVM 实利当中。
	例如，在 local[k] 模式，上图表示在同一节点上的单个进程上的多个组件，
	而对于 YARN 模式，驱动程序是在 YARN Cluster 之外的节点上提交 Spark 应用，其他组件都是运行在 YARN Cluster 管理的节点上的。
	而对于 Spark Cluster 部署应用后，在进行相关计算的时候会将 RDD 数据集上的函数发送到集群中的 Worker 上的 Executor，
	然而，这些函数做操作的对象必须是可序列化的。
	上述代码利用 Scala 的语言特性，解决了这一问题。

4.结果预览
	在完成上述代码后，我们执行代码，看看预览结果如下，执行结果，如下所示：






