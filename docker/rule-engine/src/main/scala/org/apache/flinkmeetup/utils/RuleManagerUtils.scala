package org.apache.flinkmeetup.utils

import main.org.apache.flinkmeetup.DefaultConfig
import org.apache.flink.api.common.state.MapStateDescriptor
import org.apache.flink.api.common.typeinfo.{BasicTypeInfo, TypeInformation}
import org.apache.flink.configuration.{CheckpointingOptions, Configuration}
import org.apache.flink.connector.jdbc.catalog.JdbcCatalog
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.environment.ExecutionCheckpointingOptions
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row

import java.util
import scala.collection.JavaConverters.iterableAsScalaIterableConverter

trait RuleManagerUtils extends DefaultConfig{
  def getRowAsMap(inputRow: Row, excludeColumns: String = ""): util.HashMap[String, String] = {
    val rowMap = new util.HashMap[String, String]()
    if (inputRow != null && inputRow.getFieldNames(true).asScala != null) {
      val inputColumns = inputRow.getFieldNames(true).asScala.filterNot(itm => excludeColumns.split(",").contains(itm))
      inputColumns.foreach(itm => if (inputRow.getField(itm) != null) {
        rowMap.put(itm.toLowerCase, getStringOrNull(inputRow.getField(itm)))
      })
    }
    rowMap
  }

  val cacheType: TypeInformation[util.HashMap[String, String]] = createTypeInformation[java.util.HashMap[String,String]]
  val mapRuleStateDescriptor: MapStateDescriptor[String, java.util.HashMap[String,String] ] = {
    new MapStateDescriptor("rules",
      BasicTypeInfo.STRING_TYPE_INFO,
      cacheType
    )
  }
  //Ignore for comparision
  val META_COLUMNS: Seq[String] = Seq( "rulename", "actionid")
  val ACTION_POLICY_ID = "actionid"
  val RULE_NAME = "rulename"
  val RULE_VALUE = "rulevalue"
  val MESSAGE_VALUE = "message"
  val MESSAGE_ID = "id"
  val ACTION_POLICY_VALUE = "actionvalue"


  private [flinkmeetup] def getStringOrNull(field: AnyRef): String = if (field == null ) null else field.toString

  def createStreamEnvironment (config: Map[String, String]) : StreamExecutionEnvironment = {
    val streamEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamEnvironment.enableCheckpointing(config.getOrElse("defaultCheckpointInterval", DEFAULT_CHECKPOINT_INTERVAL).toString.toLong)
    streamEnvironment.getCheckpointConfig.setCheckpointingConsistencyMode(org.apache.flink.core.execution.CheckpointingMode.EXACTLY_ONCE)
    streamEnvironment.getCheckpointConfig.setMinPauseBetweenCheckpoints(config.getOrElse("minPauseBetweenCheckPoints", MIN_PAUSE_CHECKPOINT_INTERVAL).toString.toLong)
    streamEnvironment.getCheckpointConfig.setCheckpointTimeout(config.getOrElse("checkpointTimeout", CHECK_POINT_TIMEOUT).toString.toLong)
    streamEnvironment.getCheckpointConfig.setTolerableCheckpointFailureNumber(config.getOrElse("tolerableCheckpointFailureNumber", TOLERABLE_CHECKPOINT_FALIRUS).toString.toInt)
    streamEnvironment.getCheckpointConfig.setMaxConcurrentCheckpoints(config.getOrElse("maxConcurrentCheckpoints", MAX_CONCURRENT_CHECKPOINTS).toString.toInt)
    streamEnvironment.getCheckpointConfig.setExternalizedCheckpointCleanup(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    streamEnvironment.getCheckpointConfig.enableUnalignedCheckpoints()
    val checkpointConfig = new Configuration()
    val additionalConfigurations = new Configuration()
    additionalConfigurations.set(CheckpointingOptions.CHECKPOINT_STORAGE, "filesystem")
    additionalConfigurations.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, "file:///flink/checkpoints")
    additionalConfigurations.set[java.lang.Boolean](ExecutionCheckpointingOptions.ENABLE_CHECKPOINTS_AFTER_TASKS_FINISH, java.lang.Boolean.TRUE)
    if(config.contains("flinkDeploymentName")) {
      additionalConfigurations.setString("pipeline.name",s"streaming_${config("flinkDeploymentName")}")
    }
    streamEnvironment.configure(additionalConfigurations)
    streamEnvironment
  }


  private[flinkmeetup] def loadExternalCatalog(config: Map[String, String], tableEnvironment: StreamTableEnvironment): Unit = {


    val catalog = new JdbcCatalog(
        Thread.currentThread.getContextClassLoader,
        config.getOrElse("ExternalCatalogName","mysql"),
        config.getOrElse("ExternalCatalogDatabase","demo"),
        config.getOrElse("ExternalCatalogUserName","demo"),
        config.getOrElse("ExternalCatalogPassword","demo"),
        config.getOrElse("ExternalCatalogBaseUrl","jdbc:mysql://mysql:3306"),
        null// default catalog name, can be anything
      )
      tableEnvironment.registerCatalog(config.getOrElse("ExternalCatalogName","mysql"), catalog)
  }
}
