package org.apache.flinkmeetup.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.genai.types.*;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MCPUtils {

    static Logger logger = LoggerFactory.getLogger(MCPUtils.class);
    static int MAX_TRY_COUNT = 3;

    public static void actionExecutor(String message, String contextualInfo) {
        List<McpSyncClient> mcpClients = List.of(
                McpClientFactory.createMcpClient("npx", "-y", "@modelcontextprotocol/server-slack"),
                McpClientFactory.createMcpClient("npx", "-y", "@bytebase/dbhub")
        );

        GenerateContentConfig generatedContentConfig = GeminiUtils.convertMCPToolsToGeminiConfig(mcpClients);
        //String message = "List all the available channels in Slack with their IDs";

        String contextualMessage = Prompts.contextualizeMessage(message, contextualInfo);
        JsonNode jsonNode = GeminiUtils.generateMCPQuery(generatedContentConfig, contextualMessage, null);
        int iterationCont = 0;
        iterativeToolCallExecutor(jsonNode, mcpClients, iterationCont, generatedContentConfig, contextualMessage, "");
        McpClientFactory.shutdownAllClients(mcpClients);
    }

    private static void iterativeToolCallExecutor(JsonNode mpcJsonPayload, List<McpSyncClient> mcpClients, int iterationCont, GenerateContentConfig generatedContentConfig, String originalMessage, String previousErrors) {
        try {
            if (mpcJsonPayload != null && !mpcJsonPayload.isEmpty()) {
                logger.info("payload for MCP server :\n{}",mpcJsonPayload);
                McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest(mpcJsonPayload.get("tool_name").asText(), mpcJsonPayload.get("parameters").toString());
                JsonNode finalJsonNode = mpcJsonPayload;
                McpSyncClient targetMcpClient = mcpClients.stream().filter(itm -> itm.listTools().tools().stream().anyMatch(tool -> tool.name().equals(finalJsonNode.get("tool_name").asText()))).findFirst().orElse(null);
                McpSchema.CallToolResult mcpResponse = targetMcpClient.callTool(callToolRequest);
                logger.info("MCP Response: {} at iteration {}", mcpResponse, iterationCont);
                if (Boolean.TRUE.equals(mcpResponse.isError()) && iterationCont < MAX_TRY_COUNT) {
                    String errorMessage = "{}\nError Occurred while calling MCP tool on Iteration - {} : \n".formatted(previousErrors, iterationCont, mcpResponse.content());
                    iterativeToolCallExecutor(GeminiUtils.generateMCPQuery(generatedContentConfig, originalMessage, errorMessage), mcpClients, ++iterationCont, generatedContentConfig, originalMessage, errorMessage);
                }
            }
        } catch (Exception e) {
            logger.error("Exception in iterativeMCPToolCallExecutor: {} \nfor payload {}", e, mpcJsonPayload);
        }
    }
}
