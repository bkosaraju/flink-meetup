package org.apache.flnkmeetup.utils;

import com.google.genai.Client;

public class GeminiUtils {

    public static final String MODEL_NAME = "gemini-2.0-flash-001";
    public static final String MATCH = "MATCH";
    public static final String NO_MATCH = "NO_MATCH";

    public static Client getGeminiClient(String apiKey) {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public static String generateRuleEvolutionPrompt(String jsonPayloadStr, String ruleString) {
        return """
            You are a rule evaluation engine.
            I will provide you with a JSON payload and a rule.
            Your task is to determine if the rule is matched by the JSON payload.
            JSON Payload:
            ```
            %s
            ```
            Rule to evaluate:
            ```
            %s
            ```
            Evaluation Instructions:

            Carefully read the rule.
            Access the specified attribute in the JSON payload.
            Compare the value of the attribute to the criteria specified in the rule.
            If the attribute is not found in the payload, the rule is NOT matched.
            If the rule explicitly states an "equal to" or "is equal to" condition, perform an exact string comparison
            Your response MUST be ONLY one of these words: "MATCH", "NO_MATCH".
            Do NOT add any other text, explanation, or punctuation."""
                .formatted(jsonPayloadStr, ruleString);
    }

    public String generateCallToMCP(String inputString, String previousError ) {
        if (previousError != null ){
            return """
                You are a tool call generator.
                I will provide you with a user intent and a previous error message.
                Your task is to generate a JSON object representing a tool call.
                The JSON should contain the 'tool_name' and 'parameters' keys.
                The output should be a single JSON object with no other text or formatting.
                Do **NOT** include any backticks (`), triple backticks (```), or code blocks in the response.
                
                User Intent:
                ```
                %s
                ```

                Previous Error:
                ```
                %s
                ```
                
                Generate the tool call based on the user intent and previous error.""".formatted(inputString, previousError);
        } else {
            return """
                You are a tool call generator.
                I will provide you with a user intent.
                Your task is to generate a JSON object representing a tool call.
                The JSON should contain the 'tool_name' and 'parameters' keys.
                The output should be a single JSON object with no other text or formatting.
                Do **NOT** include any backticks (`), triple backticks (```), or code blocks in the response.

                User Intent:
                ```
                %s
                ```
                Generate the tool call based on the user intent.""".formatted(inputString);

        }
    }
}