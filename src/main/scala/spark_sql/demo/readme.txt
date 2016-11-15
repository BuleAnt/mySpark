自定义聚合函数需要实现UserDefinedAggregateFunction，以下是该抽象类的定义，加了一点注释：
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.expressions

import org.apache.spark.sql.catalyst.expressions.aggregate.{Complete, AggregateExpression2}
import org.apache.spark.sql.execution.aggregate.ScalaUDAF
import org.apache.spark.sql.{Column, Row}
import org.apache.spark.sql.types._
import org.apache.spark.annotation.Experimental

/**
 * :: Experimental ::
 * The base class for implementing user-defined aggregate functions (UDAF).
 */
@Experimental
abstract class UserDefinedAggregateFunction extends Serializable {

  /**
   * A [[StructType]] represents data types of input arguments of this aggregate function.
   * For example, if a [[UserDefinedAggregateFunction]] expects two input arguments
   * with type of [[DoubleType]] and [[LongType]], the returned [[StructType]] will look like
   *
   * ```
   *   new StructType()
   *    .add("doubleInput", DoubleType)
   *    .add("longInput", LongType)
   * ```
   *
   * The name of a field of this [[StructType]] is only used to identify the corresponding
   * input argument. Users can choose names to identify the input arguments.
   */
   //输入参数的数据类型定义
  def inputSchema: StructType

  /**
   * A [[StructType]] represents data types of values in the aggregation buffer.
   * For example, if a [[UserDefinedAggregateFunction]]'s buffer has two values
   * (i.e. two intermediate values) with type of [[DoubleType]] and [[LongType]],
   * the returned [[StructType]] will look like
   *
   * ```
   *   new StructType()
   *    .add("doubleInput", DoubleType)
   *    .add("longInput", LongType)
   * ```
   *
   * The name of a field of this [[StructType]] is only used to identify the corresponding
   * buffer value. Users can choose names to identify the input arguments.
   */
   //聚合的中间过程中产生的数据的数据类型定义
  def bufferSchema: StructType

  /**
   * The [[DataType]] of the returned value of this [[UserDefinedAggregateFunction]].
   */
   //聚合结果的数据类型定义
  def dataType: DataType

  /**
   * Returns true if this function is deterministic, i.e. given the same input,
   * always return the same output.
   */
   //一致性检验，如果为true,那么输入不变的情况下计算的结果也是不变的。
  def deterministic: Boolean

  /**
   * Initializes the given aggregation buffer, i.e. the zero value of the aggregation buffer.
   *
   * The contract should be that applying the merge function on two initial buffers should just
   * return the initial buffer itself, i.e.
   * `merge(initialBuffer, initialBuffer)` should equal `initialBuffer`.
   */
   //设置聚合中间buffer的初始值，但需要保证这个语义：两个初始buffer调用下面实现的merge方法后也应该为初始buffer。
   //即如果你初始值是1，然后你merge是执行一个相加的动作，两个初始buffer合并之后等于2，不会等于初始buffer了。这样的初始值就是有问题的，所以初始值也叫"zero value"
  def initialize(buffer: MutableAggregationBuffer): Unit

  /**
   * Updates the given aggregation buffer `buffer` with new input data from `input`.
   *
   * This is called once per input row.
   */
   //用输入数据input更新buffer值,类似于combineByKey
  def update(buffer: MutableAggregationBuffer, input: Row): Unit

  /**
   * Merges two aggregation buffers and stores the updated buffer values back to `buffer1`.
   *
   * This is called when we merge two partially aggregated data together.
   */
   //合并两个buffer,将buffer2合并到buffer1.在合并两个分区聚合结果的时候会被用到,类似于reduceByKey
   //这里要注意该方法没有返回值，在实现的时候是把buffer2合并到buffer1中去，你需要实现这个合并细节。
  def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit

  /**
   * Calculates the final result of this [[UserDefinedAggregateFunction]] based on the given
   * aggregation buffer.
   */
   //计算并返回最终的聚合结果
  def evaluate(buffer: Row): Any

  /**
   * Creates a [[Column]] for this UDAF using given [[Column]]s as input arguments.
   */
   //所有输入数据进行聚合
  @scala.annotation.varargs
  def apply(exprs: Column*): Column = {
    val aggregateExpression =
      AggregateExpression2(
        ScalaUDAF(exprs.map(_.expr), this),
        Complete,
        isDistinct = false)
    Column(aggregateExpression)
  }

  /**
   * Creates a [[Column]] for this UDAF using the distinct values of the given
   * [[Column]]s as input arguments.
   */
   //所有输入数据去重后进行聚合
  @scala.annotation.varargs
  def distinct(exprs: Column*): Column = {
    val aggregateExpression =
      AggregateExpression2(
        ScalaUDAF(exprs.map(_.expr), this),
        Complete,
        isDistinct = true)
    Column(aggregateExpression)
  }
}

/**
 * :: Experimental ::
 * A [[Row]] representing an mutable aggregation buffer.
 *
 * This is not meant to be extended outside of Spark.
 */
@Experimental
abstract class MutableAggregationBuffer extends Row {

  /** Update the ith value of this buffer. */
  def update(i: Int, value: Any): Unit
}

 下面我们自己实现一个求平均数的聚合函数：

 自定义聚合函数需要实现以上抽象类的这8个方法。

 下面我们写一个测试自定义UDAF的测试类：
 AvgTest.scala
 使用原生的avg和自定义的avg的输出的结果一致：
 id:a,avg:1.6
 id:b,avg:1.1

 id:a,avg:1.6
 id:b,avg:1.1
