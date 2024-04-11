package org.apache.flinkmeetup.applauncher

import org.apache.flinkmeetup.ruleengine.RuleProcessor
import org.apache.flinkmeetup.utils.Session

import java.util.Properties
import scala.collection.JavaConverters.mapAsScalaMapConverter

object Launcher extends Session {
    def main(args: Array[String]): Unit = {
        RuleProcessor()
    }
}

