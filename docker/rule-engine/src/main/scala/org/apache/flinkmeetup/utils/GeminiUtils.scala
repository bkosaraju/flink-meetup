package org.apache.flinkmeetup.utils

import com.google.genai.Client
import java.io.Serializable

object GeminiUtils extends Serializable {

  val MODEL_NAME = "gemini-2.0-flash-001"
  val MATCH = "MATCH"
  val NO_MATCH = "NO_MATCH"

  def getGeminiClient(apiKey: String): Client = {
    Client.builder()
      .apiKey(apiKey)
      .build()
  }

  def generateRuleEvolutionPrompt(jsonPayloadStr: String, ruleString: String): String = {
        s"""
        You are a rule evaluation engine.
        I will provide you with a JSON payload and a rule.
        Your task is to determine if the rule is matched by the JSON payload.
        JSON Payload:
        ```
        $jsonPayloadStr
        ```
        Rule to evaluate:
        ```
        $ruleString
        ```
        Evaluation Instructions:

        Carefully read the rule.
        Access the specified attribute in the JSON payload.
        Compare the value of the attribute to the criteria specified in the rule.
        If the attribute is not found in the payload, the rule is NOT matched.
        If the rule explicitly states an "equal to" or "is equal to" condition, perform an exact string comparison
        Your response MUST be ONLY one of these words: "MATCH", "NO_MATCH".
        Do NOT add any other text, explanation, or punctuation."""
    }
  }