package org.apache.flinkmeetup.ruleengine

import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction
import org.apache.flink.types.Row
import org.apache.flink.util.Collector
import org.apache.flinkmeetup.utils.{RuleManagerUtils, Session}

import java.util
import scala.collection.JavaConverters.{iterableAsScalaIterableConverter, mapAsScalaMapConverter}

class ProcessFunction extends BroadcastProcessFunction[Row, Row, Row] with Session with RuleManagerUtils {

  override def processElement(inputRow: Row, ctx: BroadcastProcessFunction[Row, Row, Row]#ReadOnlyContext, out: Collector[Row]): Unit = {
    val rules = ctx.getBroadcastState(mapRuleStateDescriptor).immutableEntries().asScala.toList.sortBy {_.getValue.size()}(Ordering.Int.reverse)
    val actionRow = Row.withNames()
    //case insensitive comparison
    val inputMap = getRowAsMap(inputRow).asScala.map(itm => itm._1.trim.toLowerCase -> itm._2.trim.toLowerCase)
    rules.foreach(itm => {
      val ruleId = itm.getKey
      val ruleEvent = itm.getValue.asScala.map(itm => itm._1.trim.toLowerCase -> itm._2.trim.toLowerCase)
      val comparableRules = ruleEvent.filterNot(itm => metaColumns.contains(itm._1))
      val ruleKeys = comparableRules.keys.toList
      val inputColumns = inputMap.keySet.toList
        //inputRow.getFieldNames(true).asScala.toList.map(_.toLowerCase())
      if (inputColumns.intersect(ruleKeys).length.equals(ruleKeys.length) && ! actionRow.getFieldNames(true).contains(ruleIdColumn)) {
        val matchedRules = comparableRules.filter(itm => itm._2.trim.equalsIgnoreCase(inputMap(itm._1)))
        if (matchedRules.size.equals(comparableRules.size)) {
          ruleEvent.foreach( itm => actionRow.setField(itm._1, itm._2))
          actionRow.setField(ruleIdColumn, ruleId)
        }
      }
    }
    )

    val srcCols = inputRow.getFieldNames(true).asScala
    srcCols.foreach(itm => actionRow.setField(itm, inputRow.getField(itm)))

    if (! actionRow.getFieldNames(true).contains(ruleIdColumn)) {
      actionRow.setField(ruleIdColumn, "-1")
      actionRow.setField(actionPolicyIdColumn, "-1")
      logger.info(s"match NOT detected for: ${actionRow}")
      println(s"match NOT detected for: ${actionRow}")
    } else {
      logger.info(s"match DETECTED for: ${actionRow}")
      println(s"match DETECTED for: ${actionRow}" )
    }



    out.collect(actionRow)
  }

  override def processBroadcastElement(broadCastRow: Row, ctx: BroadcastProcessFunction[Row, Row, Row]#Context, out: Collector[Row]): Unit = {
    val rule = broadCastRow.getFieldAs[String](ruleIdColumn).trim
    val currentStatus= if (ctx.getBroadcastState(mapRuleStateDescriptor).contains(rule)) {
      ctx.getBroadcastState(mapRuleStateDescriptor).get(rule)
    } else {
      new util.HashMap[String, String]()
    }
    currentStatus.put(broadCastRow.getFieldAs[String](ruleKeyColumn).trim, broadCastRow.getFieldAs[String](ruleValueColumn).trim)
    ctx.getBroadcastState(mapRuleStateDescriptor).put(
      rule,
      currentStatus
    )
  }
}



