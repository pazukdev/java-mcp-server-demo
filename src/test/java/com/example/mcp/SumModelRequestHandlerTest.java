package com.example.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.mcp.server.McpRequest;
import com.google.mcp.server.McpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SumModelRequestHandlerTest {

    private SumModelRequestHandler handler;

    @Mock
    private McpRequest mockRequest;

    @Mock
    private McpResponse.Builder mockResponseBuilder;

    @Mock
    private McpRequest.Context mockContext;

    // ObjectMapper is part of the handler, not mocked here unless specific ObjectMapper behavior needs testing.

    @BeforeEach
    void setUp() {
        handler = new SumModelRequestHandler();

        // Mock basic context behavior
        when(mockRequest.getContext()).thenReturn(mockContext);
        when(mockContext.getCorrelationId()).thenReturn("test-corr-id");
        when(mockContext.getOperationType()).thenReturn("test-op-type");
        when(mockContext.getSourceApplication()).thenReturn("test-app");

        // Ensure McpResponse.Builder setContext is stubbed
        when(mockResponseBuilder.setContext(any(McpRequest.Context.class))).thenReturn(mockResponseBuilder);
    }

    @Test
    void testHandleRequest_Success() {
        when(mockRequest.getInput()).thenReturn("{\"number1\": 10, \"number2\": 20}");

        handler.handleRequest(mockRequest, mockResponseBuilder);

        verify(mockResponseBuilder).setContext(mockContext);
        verify(mockResponseBuilder).setOutput(eq("{\"sum\":30}"));
        verify(mockResponseBuilder).setStatus(McpResponse.Status.SUCCESS);
    }

    @Test
    void testHandleRequest_Error_InvalidJsonInput() {
        when(mockRequest.getInput()).thenReturn("{\"number1\": 10, \"number2\": 20"); // Malformed JSON

        handler.handleRequest(mockRequest, mockResponseBuilder);

        verify(mockResponseBuilder).setContext(mockContext);
        verify(mockResponseBuilder).setStatus(McpResponse.Status.INVALID_ARGUMENT);
        verify(mockResponseBuilder).setErrorDetails(contains("Invalid input JSON format"));
    }

    // Note: The following two tests (MissingNumber1, MissingNumber2) assume that SumInput uses Integer
    // and the handler performs null checks. If SumInput uses primitive int, Jackson defaults
    // missing fields to 0, and these tests would fail as the handler would proceed to calculate a sum.
    // The prompt asks to verify for "Missing 'number1' or 'number2' in input", so these tests are
    // written according to that requirement. The main code (SumInput, SumModelRequestHandler)
    // would need adjustment for these tests to pass.
    // UPDATE: SumInput and SumModelRequestHandler have been updated. These tests should now pass
    // without the custom handler overrides.

    @Test
    void testHandleRequest_Error_MissingNumber1() {
        when(mockRequest.getInput()).thenReturn("{\"number2\": 20}");
        handler.handleRequest(mockRequest, mockResponseBuilder);

        verify(mockResponseBuilder).setContext(mockContext);
        verify(mockResponseBuilder).setStatus(McpResponse.Status.INVALID_ARGUMENT);
        verify(mockResponseBuilder).setErrorDetails(eq("Missing 'number1' or 'number2' in input"));
    }

    @Test
    void testHandleRequest_Error_MissingNumber2() {
        when(mockRequest.getInput()).thenReturn("{\"number1\": 10}");
        handler.handleRequest(mockRequest, mockResponseBuilder);

        verify(mockResponseBuilder).setContext(mockContext);
        verify(mockResponseBuilder).setStatus(McpResponse.Status.INVALID_ARGUMENT);
        verify(mockResponseBuilder).setErrorDetails(eq("Missing 'number1' or 'number2' in input"));
    }

    @Test
    void testHandleRequest_Error_NonIntegerInputNumber1() {
        when(mockRequest.getInput()).thenReturn("{\"number1\": \"abc\", \"number2\": 20}");

        handler.handleRequest(mockRequest, mockResponseBuilder);

        verify(mockResponseBuilder).setContext(mockContext);
        verify(mockResponseBuilder).setStatus(McpResponse.Status.INVALID_ARGUMENT);
        verify(mockResponseBuilder).setErrorDetails(contains("Invalid input JSON format"));
    }

    @Test
    void testHandleRequest_Error_NonIntegerInputNumber2() {
        when(mockRequest.getInput()).thenReturn("{\"number1\": 10, \"number2\": \"xyz\"}");

        handler.handleRequest(mockRequest, mockResponseBuilder);

        verify(mockResponseBuilder).setContext(mockContext);
        verify(mockResponseBuilder).setStatus(McpResponse.Status.INVALID_ARGUMENT);
        verify(mockResponseBuilder).setErrorDetails(contains("Invalid input JSON format"));
    }

    @Test
    void testHandleRequest_Error_NullInputPayload() {
        when(mockRequest.getInput()).thenReturn(null);

        handler.handleRequest(mockRequest, mockResponseBuilder);

        verify(mockResponseBuilder).setContext(mockContext);
        verify(mockResponseBuilder).setStatus(McpResponse.Status.INVALID_ARGUMENT);
        verify(mockResponseBuilder).setErrorDetails(eq("Input payload is null or empty"));
    }

    @Test
    void testHandleRequest_Error_EmptyInputPayload() {
        when(mockRequest.getInput()).thenReturn("");

        handler.handleRequest(mockRequest, mockResponseBuilder);

        verify(mockResponseBuilder).setContext(mockContext);
        verify(mockResponseBuilder).setStatus(McpResponse.Status.INVALID_ARGUMENT);
        verify(mockResponseBuilder).setErrorDetails(eq("Input payload is null or empty"));
    }
}
