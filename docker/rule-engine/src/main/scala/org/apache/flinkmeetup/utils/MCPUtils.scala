/*
package org.apache.flinkmeetup.utils

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.transport.{ServerParameters, StdioClientTransport}
import io.modelcontextprotocol.spec.McpSchema.{ClientCapabilities, CreateMessageResult}

import java.time.Duration

object MCPUtils {

  def takeAction(): Unit = {
    val binaryPath = "src/main/resources/mcp-kafka-darwin-amd64"

    // Check if binary exists and is executable
    val binaryFile = new java.io.File(binaryPath)
    if (!binaryFile.exists()) {
      println(s"Binary not found at: ${binaryFile.getAbsolutePath}")
      return
    }

    if (!binaryFile.canExecute()) {
      println(s"Binary is not executable. Run: chmod +x $binaryPath")
      return
    }
    val kafkaTrnasportParam = ServerParameters.builder(binaryPath)
      .args(" --bootstrap-servers=localhost:9092", " --consumer-group-id=cg-mcp-kafka").build()
    val kafkaTransporter = new StdioClientTransport(kafkaTrnasportParam)
    val capabilities = ClientCapabilities.builder()
      .roots(true)      // Enable filesystem roots support with list changes notifications
      .sampling()       // Enable LLM sampling support
      .build();

    println("Initializing MCP Client with Kafka Transporter...")
    val client = McpClient
      .async(kafkaTransporter)
      .capabilities(capabilities)
      .requestTimeout(Duration.ofSeconds(30))
      .build()

    println(client.initialize().then
    println("MCP Client initialized successfully.")
    val availablePrompts = client.listPrompts().prompts()
    availablePrompts.forEach(itm => System.out.println(s"Available Prompt: ${itm.name()}"))
  }

}
*/
