package com.example.mcp;

import com.google.mcp.server.McpServer;
import com.google.mcp.server.netty.NettyMcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoMcpServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(DemoMcpServerApplication.class);

    public static void main(String[] args) {
        int port = 8080; // Default port

        String mcpPortEnv = System.getenv("MCP_PORT");
        if (mcpPortEnv != null) {
            try {
                port = Integer.parseInt(mcpPortEnv);
                logger.info("Using port from MCP_PORT environment variable: {}", port);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port format in MCP_PORT environment variable: '{}'. Using default port {}.", mcpPortEnv, port);
            }
        } else if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                logger.info("Using port from command-line argument: {}", port);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port format in command-line argument: '{}'. Using default port {}.", args[0], port);
            }
        } else {
            logger.info("No MCP_PORT environment variable or command-line argument provided. Using default port: {}", port);
        }

        SumModelRequestHandler requestHandler = new SumModelRequestHandler();

        NettyMcpServer.Builder serverBuilder = NettyMcpServer.builder();
        serverBuilder.setPort(port);
        serverBuilder.setHandler(requestHandler); // The SDK expects RequestHandler, not McpHandler
        McpServer server = serverBuilder.build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Demo MCP Server...");
            server.stop();
            logger.info("Server stopped.");
        }));

        try {
            server.start(); // This method is blocking and will keep the main thread alive.
            logger.info("Demo MCP Server started on port: {}", port);
            // server.start() will block until the server is shut down.
            // So, no need for server.awaitTermination() or a loop here.
        } catch (Exception e) { // Catching a broader exception for server start issues
            logger.error("Failed to start the MCP server", e);
        }
    }
}
