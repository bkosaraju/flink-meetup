package org.apache.flinkmeetup.ruleengine


import lombok.extern.slf4j.Slf4j
import org.apache.commons.io.IOUtils
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.apache.flinkmeetup.utils.{RuleManagerUtils, Session}

import java.nio.charset.Charset
import java.util.Properties
import scala.collection.JavaConverters.mapAsScalaMapConverter

@Slf4j class RuleProcessor extends Session with RuleManagerUtils {


  def startApp(config: Map[String, String]): Unit = {
    val streamEnvironment = createStreamEnvironment(config)
    val streamTableEnvironment = StreamTableEnvironment.create(streamEnvironment)
    processRules(config, streamEnvironment, streamTableEnvironment)
  }

  def processRules(config: Map[String, String], streamEnvironment: StreamExecutionEnvironment, streamTableEnvironment: StreamTableEnvironment): Unit = {

    //load external catalog
    loadExternalCatalog(config, streamTableEnvironment)

    //load data from table
    config.getOrElse("flinkSqls", "").split(",").filter(_.nonEmpty).sorted.map(sqlFile => {
      logger.info(s"executing Sql: $sqlFile")
      streamTableEnvironment.executeSql(IOUtils.toString(getClass.getResourceAsStream(sqlFile), Charset.defaultCharset()))
    })
    //Create Input stream
    val dataStream: DataStream[Row] = streamTableEnvironment.toChangelogStream(streamTableEnvironment.from(config.getOrElse("dataStreamTable", "datastreamtable")))

    val broadcastRuleTable = streamTableEnvironment.from(config.getOrElse("broadcastRuleStreamTable", "rule"))
    val broadcastRuleInitStream: DataStream[Row] = streamTableEnvironment.toDataStream(broadcastRuleTable)


    //init the stream
    val broadcastRuleStream = broadcastRuleInitStream.broadcast(mapRuleStateDescriptor)

    //join the streams
    val outcomeStream = dataStream.connect(broadcastRuleStream).process[Row](new ProcessFunction())
    //target write create a table
    streamTableEnvironment.createTemporaryView("processData", outcomeStream);


    streamEnvironment.execute("RuleEngineProcessing")
  }
}

object RuleProcessor {
  def apply(config: Map[String, String]): Unit = {
    val processor = new RuleProcessor()
    processor.startApp(config)
  }

  def apply(): Unit = {
    val props = new Properties()
    val configResources = getClass.getClassLoader.getResourceAsStream("app.properties")
    props.load(configResources)
    val inputConfig = props.asInstanceOf[java.util.Map[String, String]].asScala.map(x => x._1 -> x._2.replaceAll(""""""", "")).toMap
    RuleProcessor(inputConfig)
  }

}
