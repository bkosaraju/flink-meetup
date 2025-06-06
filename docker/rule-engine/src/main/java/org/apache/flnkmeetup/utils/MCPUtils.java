package org.apache.flnkmeetup.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.*;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.time.Duration;
import java.util.List;

public class MCPUtils {

    void takeAction() {
        String binaryPath = "npx";
        ServerParameters params = ServerParameters
                .builder("npx")
                .args("-y", "@modelcontextprotocol/server-slack")
                .env(System.getenv())
                .build();


        var stdioTransport = new StdioClientTransport(params);
        McpSyncClient mcpClient = McpClient.sync(stdioTransport)
                .requestTimeout(Duration.ofSeconds(20))
                .capabilities(
                        McpSchema.ClientCapabilities
                                .builder()
                                .roots(true)
                                .sampling()
                                .build())
                .clientInfo(new McpSchema.Implementation("Flink Meetup Client", "1.0.0"))
                .sampling(request -> {
                    McpSchema.Role response = McpSchema.Role.ASSISTANT;
            return new McpSchema.CreateMessageResult(response, new McpSchema.TextContent("Sampling response"), "custom-model", McpSchema.CreateMessageResult.StopReason.END_TURN);
        }).build();

        System.out.println("Attempting to initialise McpSyncClient..");
        mcpClient.initialize();
        System.out.println("Initialised McpSyncClient!");
        List<McpSchema.Tool> toolList = mcpClient.listTools().tools().stream().toList();
        List<Tool> tools = toolList.stream().map(mcpTool -> Tool.builder().functionDeclarations(
                List.of(
                        FunctionDeclaration
                                .builder()
                                .name(mcpTool.name())
                                .description(mcpTool.description())
                                .parameters(convertSchemaToParameters(mcpTool.inputSchema()))
                                .build()
                )).build()).toList();

        GenerateContentConfig generatedContentConfig = GenerateContentConfig.builder()
                .tools(tools)
                .build();

        //Generate an MCP tool call for slack_post_message with Chanel id = "#mcp-flink-meetup" and text = "Hi from MCP Via Gemini and Flink".

                String message = """
                Given the user intent:
                
                List all the available channels in Slack with their IDs
               
                Generate a JSON object representing a tool call. The JSON should contain the 'tool_name' and 'parameters' keys.
                The output should be a single JSON object with no other text or formatting.
                Do **NOT** include any backticks (`), triple backticks (```), or code blocks in the response.
                """;
        GenerateContentResponse geminiQueryToMCP = GeminiUtils.getGeminiClient(System.getenv("GEMINI_API_KEY"))
                .models.generateContent(
                        "gemini-2.0-flash-001", message, generatedContentConfig);

        System.out.println(geminiQueryToMCP.text());
        ObjectMapper targetOm = new ObjectMapper();
        JsonNode jsonNode = (JsonNode) targetOm.createObjectNode();;
        try {
            jsonNode = targetOm.readTree(geminiQueryToMCP.text());
        } catch (JsonProcessingException e) {
            System.out.println("Unable to parse the message "+ geminiQueryToMCP.text());
        }

        System.out.println(jsonNode.get("tool_name").asText());
        System.out.println(jsonNode.get("parameters").toString());
        if (jsonNode != null && ! jsonNode.isEmpty()) {
            McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest(jsonNode.get("tool_name").asText(), jsonNode.get("parameters").toString());
            List<McpSchema.Content> finalResponse = mcpClient.callTool(callToolRequest).content();
            System.out.println(finalResponse);
        } else {
            System.out.println("Invalid JSON format in the response: " + geminiQueryToMCP.text());
            return;
        }

        mcpClient.closeGracefully();
    }

    Schema convertSchemaToParameters(McpSchema.JsonSchema mcpInputSchema){
        var om = new ObjectMapper();
        var opt=  om.convertValue(mcpInputSchema, JsonNode.class);
        return Schema.fromJson(opt.toPrettyString());
}
}
