package org.apache.flinkmeetup.rulemanager

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.transport.{ServerParameters, StdioClientTransport}
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction
import org.apache.flink.types.Row
import org.apache.flink.util.Collector
import org.apache.flinkmeetup.utils.{GeminiUtils, RuleManagerUtils, Session}

import java.util
import scala.collection.JavaConverters.{iterableAsScalaIterableConverter, mapAsScalaMapConverter}
import scala.collection.convert.ImplicitConversions.`map AsJavaMap`

class AIProcessFunction extends BroadcastProcessFunction[Row, Row, Row] with Session with RuleManagerUtils {

  override def processElement(inputRow: Row, ctx: BroadcastProcessFunction[Row, Row, Row]#ReadOnlyContext, out: Collector[Row]): Unit = {
    //Rules are stored as Map<RuleId, Map[RuleKey, RuleValue]>
    val rules = ctx.getBroadcastState(mapRuleStateDescriptor).immutableEntries().asScala
      .map(itm => itm.getKey -> itm.getValue.asScala.toMap).toMap
    val resultRow = Row.withNames()
    //case insensitive comparison
    val inputMap = getRowAsMap(inputRow)
      .asScala
      .map(itm => itm._1.trim.toLowerCase -> itm._2)

    //Get All RuleIds and associate Value Value for ruleValue attribute
    rules.foreach(inputRule => {
      val ruleName = inputRule._1
      val rule = inputRule._2.getOrDefault(ruleName, "")
      println("searching for rule: " + inputRule._1 + " with value: " + rule)
      val inputMessage = inputRow.getField(MESSAGE_VALUE).asInstanceOf[String]
      if ( ! resultRow.getFieldNames(true).contains(RULE_NAME)) {
        val response = GeminiUtils.getGeminiClient(System.getenv("GEMINI_API_KEY"))
          .models
          .generateContent(GeminiUtils.MODEL_NAME, GeminiUtils.generateRuleEvolutionPrompt(inputMessage, rule), null).text().replaceAll("\n$","")
        response match {
          case GeminiUtils.MATCH =>
            resultRow.setField(RULE_NAME, ruleName)
            resultRow.setField(ACTION_POLICY_ID, inputRule._2.get(ACTION_POLICY_ID))
            logger.info(s"Rule matched: ${ruleName} with response: ${GeminiUtils.MATCH} for input:\n $inputMessage")
          case GeminiUtils.NO_MATCH =>
            logger.debug(s"Rule Not matched: ${ruleName} with response: ${GeminiUtils.NO_MATCH} for input:\n ${inputMessage}")
            println(s"Rule Not matched: ${ruleName} with response: ${GeminiUtils.NO_MATCH} for input:\n ${inputMessage}")
          // Continue checking other rules
          case _ =>
            logger.warn(s"Unexpected response from AI: $response for input:\n $inputMessage")
            println(s"Unexpected response from AI: $response for input:\n $inputMessage")
            resultRow.setField(RULE_NAME, "-2")
        }
      }
    })
    val srcCols = inputRow.getFieldNames(true).asScala
    srcCols.foreach(itm => resultRow.setField(itm, inputRow.getField(itm)))

    if (! resultRow.getFieldNames(true).contains(RULE_NAME) ) {
      resultRow.setField(RULE_NAME, "-1")
      resultRow.setField(ACTION_POLICY_ID, "-1")
      logger.info(s"match NOT detected for: ${resultRow}")
      println(s"match NOT detected for: ${resultRow}")
    } else if (resultRow.getFieldAs(RULE_NAME).equals(-2) ){
      logger.warn(s"Unexpected response from AI for input: ${inputRow.getField(MESSAGE_VALUE)}")
      println(s"Unexpected response from AI for input: ${inputRow.getField(MESSAGE_VALUE)}")
      resultRow.setField(RULE_NAME, "-2")
      resultRow.setField(ACTION_POLICY_ID, "-2")
    } else {
      logger.info(s"match DETECTED for: ${resultRow}")
      println(s"match DETECTED for: ${resultRow}" )
    }
    out.collect(resultRow)
  }



  override def processBroadcastElement(broadCastRow: Row, ctx: BroadcastProcessFunction[Row, Row, Row]#Context, out: Collector[Row]): Unit = {
    val rule = broadCastRow.getFieldAs[String](RULE_NAME).trim
    val currentStatus= if (ctx.getBroadcastState(mapRuleStateDescriptor).contains(rule)) {
      ctx.getBroadcastState(mapRuleStateDescriptor).get(rule)
    } else {
      new util.HashMap[String, String]()
    }
    currentStatus.put(broadCastRow.getFieldAs[String](RULE_NAME).trim, broadCastRow.getFieldAs[String](RULE_VALUE).trim)
    ctx.getBroadcastState(mapRuleStateDescriptor).put(
      rule,
      currentStatus
    )
  }
}



