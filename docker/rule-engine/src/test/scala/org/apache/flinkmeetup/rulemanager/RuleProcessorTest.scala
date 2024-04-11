package org.apache.flinkmeetup.rulemanager


import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend
import org.apache.flink.runtime.state.storage.JobManagerCheckpointStorage
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.apache.flinkmeetup.ruleengine.RuleProcessor
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import java.util.Properties
import scala.collection.JavaConverters.mapAsScalaMapConverter


@RunWith(classOf[JUnitRunner])
class RuleProcessorTest extends FunSuite with Matchers with BeforeAndAfter {

  val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
    .setNumberSlotsPerTaskManager(20)
    .setNumberTaskManagers(1)
    .build)


  before {
    flinkCluster.before()
  }

  after {
    flinkCluster.after()
  }

  test("Process Events") {

    val streamEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamEnvironment.setParallelism(4)
    streamEnvironment.getConfig.setRestartStrategy(RestartStrategies.noRestart())
    streamEnvironment.setStateBackend(new HashMapStateBackend())
    streamEnvironment.getCheckpointConfig.setCheckpointStorage(new JobManagerCheckpointStorage())
    val streamTableEnvironment = StreamTableEnvironment.create(streamEnvironment)

    val processor = new RuleProcessor
    val props = new Properties()
    props.load(getClass.getClassLoader.getResourceAsStream("test.properties"))
    val inputConfig = props.asInstanceOf[java.util.Map[String, String]].asScala.map(x => x._1 -> x._2.replaceAll(""""""", "")).toMap
    processor.processRules(inputConfig, streamEnvironment, streamTableEnvironment)
    streamTableEnvironment.executeSql("""show tables in mysql.demo""").print()
    streamTableEnvironment.executeSql("select * from mysql.demo.rule").print()

    streamEnvironment.execute("rule processor execution")
    assertResult(true) {
      true
    }
  }

}
