# Demo MCP Server for Sum Calculation

## Description

This project is a demonstration of a simple Java-based Model Controller Platform (MCP) server. It implements a basic "sum" model that takes two numbers as input and returns their sum. The project showcases:
-   MCP SDK usage (`com.google.mcp:mcp-sdk`).
-   Standard Maven project structure.
-   JSON request/response handling.
-   Logging with SLF4J and Logback.
-   Unit and integration testing.
-   Building an executable JAR.

## Features

*   MCP server listening on a configurable port (default 8080, endpoint `/mcp`).
*   Handles requests to perform a sum of two numbers (`number1`, `number2`).
*   Extracts and returns MCP context (`correlationId`, `operationType`, `sourceApplication`) in the response.
*   Basic error handling for invalid or malformed input JSON, and missing input fields.
*   Logging using SLF4J with a Logback backend for console output.
*   Unit tests (JUnit 5 & Mockito) for the request handler logic.
*   Integration tests (JUnit 5 & Apache HttpClient, using Maven Failsafe Plugin) for server end-to-end testing.
*   JaCoCo code coverage reports.

## Prerequisites

*   Java JDK 11 or higher.
*   Apache Maven 3.6.x or higher.

## Build Instructions

To compile the project, run all tests (unit and integration), and package the application into an executable JAR:

```bash
mvn clean install
```

This command will:
1.  Clean previous builds.
2.  Compile the source code.
3.  Run unit tests (Surefire plugin).
4.  Package the application into a JAR.
5.  Run integration tests (Failsafe plugin) against the packaged application.
6.  Install the JAR into your local Maven repository.

A JaCoCo code coverage report will also be generated in `target/site/jacoco/index.html`.

## Running the Server

The server can be run using the executable JAR created by the `maven-assembly-plugin`. The artifactId in `pom.xml` is `mcp-project`.

**Default Port (8080):**

```bash
java -jar target/mcp-project-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Custom Port:**

You can specify a custom port using either a command-line argument or an environment variable.

*   Using command-line argument (e.g., port 9090):
    ```bash
    java -jar target/mcp-project-1.0-SNAPSHOT-jar-with-dependencies.jar 9090
    ```

*   Using `MCP_PORT` environment variable (e.g., port 9090):
    ```bash
    MCP_PORT=9090 java -jar target/mcp-project-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

The server will log its startup and the port it's listening on.

## Running Tests Separately

*   **Running only unit tests:**
    ```bash
    mvn test
    ```
    This executes tests run by the Surefire plugin (typically those not ending in `IT`).

*   **Running unit and integration tests:**
    The `mvn verify` command will run both unit tests and integration tests (those run by the Failsafe plugin, e.g., `*IT.java`).
    ```bash
    mvn verify
    ```
    Alternatively, `mvn install` also runs all tests.

## Example MCP Request

You can interact with the server using `curl` or any HTTP client. The MCP server expects requests at the `/mcp` endpoint.

**Example 1: Successful Sum Request**

Request:
```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "context": {
    "correlationId": "curl-test-001",
    "operationType": "sum",
    "sourceApplication": "curl"
  },
  "input": "{\"number1\": 123, \"number2\": 456}"
}' http://localhost:8080/mcp
```

Expected Response:
```json
{
  "context": {
    "correlationId": "curl-test-001",
    "operationType": "sum",
    "sourceApplication": "curl",
    "domainId": null,      // Default field from SDK
    "modelId": null,       // Default field from SDK
    "modelVersion": null // Default field from SDK
  },
  "status": "SUCCESS",
  "input": null,
  "output": "{\"sum\":579}",
  "errorDetails": null
}
```
*(Note: The `input` field in the response might be null or reflect the original request input depending on MCP SDK behavior and configuration. The `context` in the response may also include additional fields populated by the SDK.)*

**Example 2: Error Response (Missing a Number)**

Request (missing `number2`):
```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "context": {
    "correlationId": "curl-test-002",
    "operationType": "sum",
    "sourceApplication": "curl"
  },
  "input": "{\"number1\": 123}"
}' http://localhost:8080/mcp
```

Expected Response:
```json
{
  "context": {
    "correlationId": "curl-test-002",
    "operationType": "sum",
    "sourceApplication": "curl",
    "domainId": null,
    "modelId": null,
    "modelVersion": null
  },
  "status": "INVALID_ARGUMENT",
  "input": null,
  "output": null,
  "errorDetails": "Missing 'number1' or 'number2' in input"
}
```

## Project Structure

*   `pom.xml`: Maven project configuration, dependencies, and build plugins.
*   `src/main/java/com/example/mcp/`:
    *   `DemoMcpServerApplication.java`: Main class containing the `main` method to configure and start the Netty-based MCP server.
    *   `SumModelRequestHandler.java`: Implements `com.google.mcp.server.RequestHandler`. It handles the incoming MCP requests, parses the input, performs the sum calculation, and constructs the response.
    *   `SumInput.java`: A simple POJO (Plain Old Java Object) used by Jackson to deserialize the JSON string from the `input` field of the MCP request into Java types.
*   `src/main/resources/`:
    *   `logback.xml`: Configuration file for Logback (the SLF4J implementation used).
*   `src/test/java/com/example/mcp/`:
    *   `SumModelRequestHandlerTest.java`: Unit tests for `SumModelRequestHandler` using JUnit 5 and Mockito.
    *   `DemoMcpServerIT.java`: Integration tests for the server, using JUnit 5, Apache HttpClient, and managed by the Maven Failsafe plugin. It starts the server and sends real HTTP requests.
