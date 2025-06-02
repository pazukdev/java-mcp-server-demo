package com.example.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.mcp.server.McpRequest;
import com.google.mcp.server.McpResponse;
import com.google.mcp.server.RequestHandler;
import com.google.mcp.server.McpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SumModelRequestHandler implements RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(SumModelRequestHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleRequest(McpRequest request, McpResponse.Builder responseBuilder) {
        McpContext context = request.getContext();
        String correlationId = context.getCorrelationId();
        String operationType = context.getOperationType();
        String sourceApplication = context.getSourceApplication();

        logger.info("Received request: correlationId={}, operationType={}, sourceApplication={}, inputPayload={}",
                correlationId, operationType, sourceApplication, request.getInput());

        responseBuilder.setContext(context);

        String inputPayload = request.getInput();
        if (inputPayload == null || inputPayload.trim().isEmpty()) { // Added trim() for robustness
            logger.error("Input payload is null or empty. Correlation ID: {}", correlationId);
            responseBuilder.setStatus(McpResponse.Status.INVALID_ARGUMENT);
            responseBuilder.setErrorDetails("Input payload is null or empty"); // Exact message match
            return;
        }

        SumInput sumInput;
        try {
            sumInput = objectMapper.readValue(inputPayload, SumInput.class);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing input JSON. Correlation ID: {}. Error: {}", correlationId, e.getMessage());
            responseBuilder.setStatus(McpResponse.Status.INVALID_ARGUMENT);
            responseBuilder.setErrorDetails("Invalid input JSON format: " + e.getMessage());
            return;
        }

        // As SumInput uses primitive int, Jackson will deserialize missing fields to 0, not null.
        // So, a specific check for null is not needed here if we stick to primitive int.
        // If SumInput used Integer objects, then a null check would be appropriate.
        // For this implementation, we assume number1 and number2 will be present or defaulted to 0 by Jackson.

        // Added check for null numbers after deserialization, now that SumInput uses Integer
        if (sumInput.getNumber1() == null || sumInput.getNumber2() == null) {
            logger.error("Missing 'number1' or 'number2' in input payload: {}. Correlation ID: {}", inputPayload, correlationId);
            responseBuilder.setStatus(McpResponse.Status.INVALID_ARGUMENT);
            responseBuilder.setErrorDetails("Missing 'number1' or 'number2' in input");
            return;
        }

        int sum = sumInput.getNumber1() + sumInput.getNumber2(); // .intValue() is not strictly needed due to auto-unboxing

        Map<String, Integer> outputMap = new HashMap<>();
        outputMap.put("sum", sum);

        try {
            String outputPayload = objectMapper.writeValueAsString(outputMap);
            responseBuilder.setOutput(outputPayload);
            responseBuilder.setStatus(McpResponse.Status.SUCCESS);
            logger.info("Sending response: correlationId={}, status={}, outputPayload={}",
                    correlationId, McpResponse.Status.SUCCESS, outputPayload);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing output JSON. Correlation ID: {}. Error: {}", correlationId, e.getMessage());
            responseBuilder.setStatus(McpResponse.Status.INTERNAL_ERROR); // Or another appropriate error status
            responseBuilder.setErrorDetails("Error creating output JSON: " + e.getMessage());
        }
    }
}
