package org.apache.flinkmeetup.utils;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.time.Duration;
import java.util.List;

public class McpClientFactory {
    public static McpSyncClient createMcpClient(String binaryPath, String... args) {
        ServerParameters params = ServerParameters
                .builder(binaryPath)
                .args(args)
                .env(System.getenv())
                .build();


        var stdioTransport = new StdioClientTransport(params);
        McpSyncClient mcpClient = McpClient.sync(stdioTransport)
                .requestTimeout(Duration.ofSeconds(30))
                .capabilities(
                        McpSchema.ClientCapabilities
                                .builder()
                                .roots(true)
                                .sampling()
                                .build())
                .clientInfo(new McpSchema.Implementation("Flink Meetup Client", "1.0.0"))
                .sampling(request -> {
                    McpSchema.Role response = McpSchema.Role.ASSISTANT;
                    return new McpSchema.CreateMessageResult(
                            response,
                            new McpSchema.TextContent("Response"),
                            "custom-model",
                            McpSchema.CreateMessageResult.StopReason.END_TURN
                    );
                }).build();

        mcpClient.initialize();
        return mcpClient;
    }
    static void shutdownAllClients(List<McpSyncClient> clients) {
        for (McpSyncClient client : clients) {
            if (client != null) {
                client.closeGracefully();
            }
        }
    }
}
