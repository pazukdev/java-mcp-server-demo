package com.example.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DemoMcpServerIT {

    private static Thread serverThread;
    private static final int TEST_PORT = 8088; // Use a different port than default
    private static final String SERVER_URL = "http://localhost:" + TEST_PORT + "/mcp"; // MCP Server Endpoint

    private CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Helper class for deserializing MCP JSON response
    static class TestMcpResponse {
        public String status;
        public TestMcpContext context;
        public String output; // This will be a JSON string, needs further parsing
        public String errorDetails;
    }

    static class TestMcpContext {
        public String correlationId;
        public String operationType;
        public String sourceApplication;
        // Add other context fields if needed
    }


    @BeforeAll
    public static void startServer() throws InterruptedException {
        serverThread = new Thread(() -> DemoMcpServerApplication.main(new String[]{String.valueOf(TEST_PORT)}));
        serverThread.setDaemon(true); // Allow JVM to exit
        serverThread.start();
        // Wait for server to start. In a real-world scenario, use a more robust health check.
        Thread.sleep(3000); // Increased delay slightly for more reliability
    }

    @AfterAll
    public static void stopServer() throws InterruptedException {
        if (serverThread != null) {
            serverThread.interrupt(); // Signal server thread to stop
            serverThread.join(2000); // Wait for the thread to die
        }
    }

    @BeforeEach
    public void setupClient() {
        httpClient = HttpClients.createDefault();
    }

    @AfterEach
    public void closeClient() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    private String buildMcpRequest(String correlationId, String operationType, String sourceApplication, String inputJsonString) throws Exception {
        Map<String, Object> contextMap = Map.of(
                "correlationId", correlationId,
                "operationType", operationType,
                "sourceApplication", sourceApplication
        );
        Map<String, Object> requestMap = Map.of(
                "context", contextMap,
                "input", inputJsonString // input is an already stringified JSON
        );
        return objectMapper.writeValueAsString(requestMap);
    }

    @Test
    void testServer_HandleSumRequest_Success() throws Exception {
        String inputJson = "{\"number1\": 50, \"number2\": 30}";
        String correlationId = "integ-test-corr-id-success";
        String requestBody = buildMcpRequest(correlationId, "calculateSum", "integrationTest", inputJson);

        HttpPost httpPost = new HttpPost(SERVER_URL);
        httpPost.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        TestMcpResponse mcpResponse = httpClient.execute(httpPost, response -> {
            assertEquals(200, response.getCode(), "HTTP status code should be 200");
            HttpEntity entity = response.getEntity();
            assertNotNull(entity, "Response entity should not be null");
            String responseString = EntityUtils.toString(entity);
            return objectMapper.readValue(responseString, TestMcpResponse.class);
        });

        assertEquals("SUCCESS", mcpResponse.status);
        assertNotNull(mcpResponse.output, "Output should not be null for SUCCESS");
        assertNull(mcpResponse.errorDetails, "ErrorDetails should be null for SUCCESS");

        // Parse the inner JSON string in the 'output' field
        Map<String, Integer> outputMap = objectMapper.readValue(mcpResponse.output, new TypeReference<Map<String, Integer>>() {});
        assertEquals(80, outputMap.get("sum").intValue());

        assertNotNull(mcpResponse.context, "Response context should not be null");
        assertEquals(correlationId, mcpResponse.context.correlationId);
        assertEquals("calculateSum", mcpResponse.context.operationType);
        assertEquals("integrationTest", mcpResponse.context.sourceApplication);
    }

    @Test
    void testServer_HandleSumRequest_InvalidInput() throws Exception {
        String inputJson = "{\"number2\": 30}"; // Missing number1
        String correlationId = "integ-test-corr-id-invalid";
        String requestBody = buildMcpRequest(correlationId, "calculateSum", "integrationTest", inputJson);

        HttpPost httpPost = new HttpPost(SERVER_URL);
        httpPost.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        TestMcpResponse mcpResponse = httpClient.execute(httpPost, response -> {
            assertEquals(200, response.getCode(), "HTTP status code should be 200");
            HttpEntity entity = response.getEntity();
            assertNotNull(entity, "Response entity should not be null");
            String responseString = EntityUtils.toString(entity);
            return objectMapper.readValue(responseString, TestMcpResponse.class);
        });

        assertEquals("INVALID_ARGUMENT", mcpResponse.status);
        assertNull(mcpResponse.output, "Output should be null for INVALID_ARGUMENT");
        assertNotNull(mcpResponse.errorDetails, "ErrorDetails should not be null for INVALID_ARGUMENT");
        assertTrue(mcpResponse.errorDetails.contains("Missing 'number1' or 'number2' in input"),
                "Error details mismatch. Got: " + mcpResponse.errorDetails);

        assertNotNull(mcpResponse.context, "Response context should not be null");
        assertEquals(correlationId, mcpResponse.context.correlationId);
    }

    @Test
    void testServer_HandleSumRequest_MalformedJsonInput() throws Exception {
        String inputJson = "{\"number1\": 10, \"number2\" : oops}"; // Malformed JSON in input field
        String correlationId = "integ-test-corr-id-malformed";
        String requestBody = buildMcpRequest(correlationId, "calculateSum", "integrationTest", inputJson);

        HttpPost httpPost = new HttpPost(SERVER_URL);
        httpPost.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        TestMcpResponse mcpResponse = httpClient.execute(httpPost, response -> {
            assertEquals(200, response.getCode(), "HTTP status code should be 200");
            HttpEntity entity = response.getEntity();
            assertNotNull(entity, "Response entity should not be null");
            String responseString = EntityUtils.toString(entity);
            return objectMapper.readValue(responseString, TestMcpResponse.class);
        });

        assertEquals("INVALID_ARGUMENT", mcpResponse.status);
        assertNull(mcpResponse.output, "Output should be null for INVALID_ARGUMENT");
        assertNotNull(mcpResponse.errorDetails, "ErrorDetails should not be null for INVALID_ARGUMENT");
        assertTrue(mcpResponse.errorDetails.contains("Invalid input JSON format"),
                "Error details mismatch. Got: " + mcpResponse.errorDetails);

        assertNotNull(mcpResponse.context, "Response context should not be null");
        assertEquals(correlationId, mcpResponse.context.correlationId);
    }
}
