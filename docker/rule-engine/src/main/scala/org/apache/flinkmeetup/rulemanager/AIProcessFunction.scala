package org.apache.flinkmeetup.rulemanager

import com.google.gson.{Gson, JsonParser}
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction
import org.apache.flink.types.Row
import org.apache.flink.util.Collector
import org.apache.flinkmeetup.utils.{GeminiUtils, MCPUtils, Prompts, RuleManagerUtils}
import org.slf4j.LoggerFactory

import java.util
import scala.collection.JavaConverters.{iterableAsScalaIterableConverter, mapAsScalaMapConverter}
import scala.collection.convert.ImplicitConversions.`map AsJavaMap`

class AIProcessFunction extends BroadcastProcessFunction[Row, Row, Row] with RuleManagerUtils {
  def logger = LoggerFactory.getLogger(getClass)

  override def processElement(inputRow: Row, ctx: BroadcastProcessFunction[Row, Row, Row]#ReadOnlyContext, out: Collector[Row]): Unit = {
    //Rules are stored as Map<RuleId, Map[RuleKey, RuleValue]>
    val rules = ctx.getBroadcastState(mapRuleStateDescriptor).immutableEntries().asScala.map(itm => itm.getKey -> itm.getValue.asScala.toMap).toMap
    val resultRow = Row.withNames()
    //case insensitive comparison
    val inputMap = getRowAsMap(inputRow).asScala.map(itm => itm._1.trim.toLowerCase -> itm._2)
    resultRow.setField(MESSAGE_ID, inputRow.getField(MESSAGE_ID))
    resultRow.setField(ACTION_POLICY_VALUE, "") //Get All RuleIds and associate Value Value for ruleValue attribute
    rules.foreach(inputRule => {
      val ruleName = inputRule._2.getOrDefault(RULE_NAME, "NEVER MATCH")
      val ruleValue = inputRule._2.getOrDefault(RULE_VALUE, "NEVER MATCH")
      val actionValue = inputRule._2.getOrDefault(ACTION_POLICY_VALUE, "")
      val actioPolicyId = inputRule._2.getOrDefault(ACTION_POLICY_ID, "")
      logger.debug("searching for rule: " + inputRule._1 + " with value: " + ruleValue)
      val inputMessage = inputRow.getField(MESSAGE_VALUE).asInstanceOf[String]
      if (!resultRow.getFieldNames(true).contains(RULE_NAME)) {
        val response = GeminiUtils.getGeminiClient(System.getenv("GEMINI_API_KEY")).models.generateContent(GeminiUtils.MODEL_NAME, Prompts.generateRuleEvolutionPrompt(inputMessage, ruleValue), null).text().replaceAll("\n$", "")
        response match {
          case GeminiUtils.MATCH => resultRow.setField(RULE_NAME, ruleName)
            resultRow.setField(ACTION_POLICY_ID, actioPolicyId)
            resultRow.setField(ACTION_POLICY_VALUE, actionValue)
            logger.info(s"Rule matched: ${ruleName} with response: ${GeminiUtils.MATCH} for input:\n ${minifyJson(inputMessage)}")
          case GeminiUtils.NO_MATCH => logger.debug(s"Rule Not matched: ${ruleName} with response: ${GeminiUtils.NO_MATCH} for input:\n ${minifyJson(inputMessage)}") // Continue checking other rules
          case _ => logger.warn(s"Unexpected response from AI: $response for input:\n ${minifyJson(inputMessage)}")
            resultRow.setField(RULE_NAME, "-2")
        }
      }
    })
    val srcCols = inputRow.getFieldNames(true).asScala
    srcCols.foreach(itm => resultRow.setField(itm, inputRow.getField(itm)))
    if (!resultRow.getFieldNames(true).contains(RULE_NAME)) {
      resultRow.setField(RULE_NAME, "-1")
      resultRow.setField(ACTION_POLICY_ID, "-1")
      logger.info(s"match NOT detected for: ${resultRow}")
    } else if (resultRow.getFieldAs(RULE_NAME).equals(-2)) {
      logger.warn(s"Unexpected response from AI for input: ${inputRow.getField(MESSAGE_VALUE)}")
      resultRow.setField(RULE_NAME, "-2")
      resultRow.setField(ACTION_POLICY_ID, "-2")
    } else {
      MCPUtils.actionExecutor(resultRow.getFieldAs[String](ACTION_POLICY_VALUE), resultRow.toString)
    } //Set the actionId and actionValue
    out.collect(resultRow)
  }

  override def processBroadcastElement(broadCastRow: Row, ctx: BroadcastProcessFunction[Row, Row, Row]#Context, out: Collector[Row]): Unit = {
    val rule = broadCastRow.getFieldAs[String](RULE_NAME).trim
    val currentStatus = if (ctx.getBroadcastState(mapRuleStateDescriptor).contains(rule)) {
      ctx.getBroadcastState(mapRuleStateDescriptor).get(rule)
    } else {
      new util.HashMap[String, String]()
    }
    currentStatus.put(RULE_NAME, broadCastRow.getFieldAs[String](RULE_NAME).trim)
    currentStatus.put(RULE_VALUE, broadCastRow.getFieldAs[String](RULE_VALUE).trim)
    currentStatus.put(ACTION_POLICY_VALUE, broadCastRow.getFieldAs[String](ACTION_POLICY_VALUE).trim)
    currentStatus.put(ACTION_POLICY_ID, broadCastRow.getFieldAs[String](ACTION_POLICY_ID).trim)
    ctx.getBroadcastState(mapRuleStateDescriptor).put(rule, currentStatus)
  }

  private def minifyJson(jsonString: String): String = {
    val gson = new Gson()
    gson.toJson(JsonParser.parseString(jsonString))
  }
}



