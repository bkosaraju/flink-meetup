package org.apache.flinkmeetup.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GeminiUtils {

    static Logger logger = LoggerFactory.getLogger(GeminiUtils.class);
    public static final String MODEL_NAME = "gemini-2.0-flash-001";
    public static final String MATCH = "MATCH";
    public static final String NO_MATCH = "NO_MATCH";

    public static Client getGeminiClient(String apiKey) {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public static Client getGeminiClient() {
        return Client.builder()
                .apiKey(System.getenv().getOrDefault("GEMINI_API_KEY","NA"))
                .build();
    }

    public static GenerateContentConfig convertMCPToolsToGeminiConfig(List<McpSyncClient> mcpClients){
        List<McpSchema.Tool> toolList = mcpClients.stream()
                .flatMap(client -> client.listTools().tools().stream())
                .toList();
        List<Tool> tools = toolList.stream().map(mcpTool -> Tool.builder().functionDeclarations(
                List.of(
                        FunctionDeclaration
                                .builder()
                                .name(mcpTool.name())
                                .description(mcpTool.description())
                                .parameters(convertSchemaToParameters(mcpTool.inputSchema()))
                                .build()
                )).build()).toList();

        return GenerateContentConfig.builder()
                .tools(tools)
                .build();
    }
    public static Schema convertSchemaToParameters(McpSchema.JsonSchema mcpInputSchema){
        var om = new ObjectMapper();
        var opt=  om.convertValue(mcpInputSchema, JsonNode.class);
        return Schema.fromJson(opt.toPrettyString());
    }

    public static String cleanCodeBlock(String codeBlock) {
        return codeBlock
                .replaceAll("```(?:json|java|python)?\\s*", "")
                .replaceAll("```", "")
                .replaceAll("^`+|`+$", "")
                .trim();
    }


    static String generateGeneralResponse(String message){
        return getGeminiClient().models.generateContent(MODEL_NAME, message, null).text();
    }

    static JsonNode generateMCPQuery(GenerateContentConfig generatedContentConfig, String message, String previousError) {
        String McpQuerytext  = getGeminiClient()
                .models.generateContent(
                        GeminiUtils.MODEL_NAME,
                        Prompts.generateCallToMCP(message, previousError),
                        generatedContentConfig).text();
        ObjectMapper targetOm = new ObjectMapper();
        JsonNode jsonNode = (JsonNode) targetOm.createObjectNode();
        try {
            jsonNode = targetOm.readTree(GeminiUtils.cleanCodeBlock(McpQuerytext));
        } catch (JsonProcessingException e) {
            logger.error("error occurred while parsing the output",e);
        }
        return jsonNode;
    }

}