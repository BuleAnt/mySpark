在使用Spark streaming消费kafka数据时，程序异常中断的情况下发现会有数据丢失的风险，本文简单介绍如何解决这些问题。
流处理中的几种可靠性语义：
	1、At most once - 每条数据最多被处理一次（0次或1次），这种语义下会出现数据丢失的问题；
	2、At least once - 每条数据最少被处理一次 (1次或更多)，这个不会出现数据丢失，但是会出现数据重复；
	3、Exactly once - 每条数据只会被处理一次，没有数据会丢失，并且没有数据会被多次处理，这种语义是大家最想要的，但是也是最难实现的。
Kafka高级API
	如果不做容错，将会带来数据丢失，因为Receiver一直在接收数据，
	在其没有处理的时候（已通知zk数据接收到），Executor突然挂掉(或是driver挂掉通知executor关闭)，缓存在内存中的数据就会丢失。
	因为这个问题，Spark1.2开始加入了WAL（Write ahead log）开启 WAL，
	将receiver获取数据的存储级别修改为StorageLevel.MEMORY_AND_DISK_SER，使用代码片段如下：
	conf.set("spark.streaming.receiver.writeAheadLog.enable","true")
	ssc.checkpoint("checkpoint")
	但是开启WAL后，依旧存在数据丢失问题，即使按官方说的设置了WAL，依旧会有数据丢失，这是为什么？
	因为在任务中断时receiver也被强行终止了，将会造成数据丢失，提示如下：
		ERROR ReceiverTracker: Deregistered receiver for stream 0: Stopped by driver
		WARN BlockGenerator: Cannot stop BlockGenerator as its not in the Active state [state = StoppedAll]
		WARN BatchedWriteAheadLog: BatchedWriteAheadLog Writer queue interrupted.
	在Streaming程序的最后添加代码，只有在确认所有receiver都关闭的情况下才终止程序。
	我们可以调用StreamingContext的stop方法，其原型如下：
		def stop(stopSparkContext: Boolean, stopGracefully: Boolean): Unit
	可以如下使用：
		sys.addShutdownHook({
		 ssc.stop(true,true)
		)})
WAL带来的问题
	WAL实现的是At-least-once语义。
	如果在写入到外部存储的数据还没有将offset更新到zookeeper就挂掉，这些数据将会被反复消费。
	同时，因为需要把数据写入到可靠的外部系统，这会牺牲系统的整个吞吐量。

Kafka Direct API
	Kafka direct API 的运行方式，将不再使用receiver来读取数据，也不用使用WAL机制。
	同时保证了exactly-once语义，不会在WAL中消费重复数据。
	不过需要自己完成将offset写入zk的过程。调用方式可以参见下面：



























