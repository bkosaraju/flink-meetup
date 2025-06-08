package org.apache.flinkmeetup.utils;

public class Prompts {
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

    public static String generateCallToMCP(String inputString, String previousError) {
        if (previousError != null ){
            return """
                You are a tool call generator.
                I will provide you with a user intent and a previous error message.
                Your task is to generate a JSON object representing a tool call.
                The JSON should contain the 'tool_name' and 'parameters' keys.
                Do NOT add any other text, explanation, or punctuation.
                
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
                Do NOT add any other text, explanation, or punctuation.

                User Intent:
                ```
                %s
                ```
                Generate the tool call based on the user intent.""".formatted(inputString);

        }
    }

    static String contextualizeMessage(String message, String contextualInfo) {
        return """
            You are a context-aware assistant.
            I have a message template that needs to be populated with data from the following JSON context.  
            Please replace the placeholders in the template with the corresponding values extracted from the JSON. 
            For placeholders that result in multiple values, join them with a comma. If a value is missing from the JSON context for a particular placeholder, replace the placeholder with 'N/A'.  
            Return the filled-in message as a single line of text.
            Do NOT add any other text, explanation, or punctuation.
          
            Message:
            ```
            %s
            ```

            Contextual Information:
            ```
            %s
            ```

            Generate the response by incorporating the contextual information into the message.""".formatted(message, contextualInfo);
    }

}
