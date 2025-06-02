# Demo Model Context Server (Java)

This is a simple demo server built with Java, SparkJava, and Vaadin. It simulates a Model Context Protocol server by providing context via a JSON API and also includes a basic Vaadin UI.

## Prerequisites

- Java JDK 17 or higher
- Apache Maven
- Node.js and npm (for local Vaadin development and frontend builds)

## Configuration

### API Keys

This application uses external services for web search (emulated Google Search) and LLM content generation (emulated OpenAI GPT-4o-mini). To connect to the actual services, you would need to:

1.  **Google Custom Search API:**
    *   Obtain an API Key and a Custom Search Engine ID (CX).
    *   Set the `GOOGLE_API_KEY_PLACEHOLDER` and `GOOGLE_CX_PLACEHOLDER` in `src/main/java/com/example/service/SearchService.java` with your actual key and CX, or preferably, modify the code to read them from environment variables or a configuration file.

2.  **OpenAI API:**
    *   Obtain an API Key from OpenAI.
    *   Set the `OPENAI_API_KEY_PLACEHOLDER` in `src/main/java/com/example/service/OpenAIService.java` with your actual key, or preferably, modify the code to read it from an environment variable or a configuration file.

**Note:** The current code uses placeholders and emulated responses for these services.

## Building the Server

To build the server, navigate to the project's root directory and run:

```bash
mvn clean package
```

This will compile the Java code and process Vaadin frontend resources. The build process uses the `maven-shade-plugin` to package the application into an executable 'fat JAR' (e.g., `target/demo-server-1.0-SNAPSHOT.jar`) which includes all necessary dependencies, making it suitable for standalone execution.

### Vaadin Frontend

Building and running Vaadin applications locally for development typically requires Node.js and npm to be installed for managing frontend dependencies and building the frontend bundle. The `vaadin-maven-plugin` (if configured, though not explicitly added yet in this project's POM for frontend builds) usually handles this process. For production builds (like on Heroku), this is often handled by the buildpack if configured correctly.

## Running the Server

After building, you can run the SparkJava server (which provides the backend APIs) using:

```bash
java -jar target/demo-server-1.0-SNAPSHOT.jar
```
Alternatively, during development, you can use the Maven exec plugin (as previously mentioned, though the fat JAR is preferred for standalone runs):
```bash
mvn exec:java -Dexec.mainClass="com.example.DemoServer"
```
The backend server will start and listen on port `4567` by default.

**Note on Vaadin UI:** Running the Vaadin UI in development mode typically involves the `vaadin-maven-plugin` and its `vaadin:dev` goal, or by running the application in an embedded Jetty/Tomcat server that can serve Servlets. The current SparkJava setup does not automatically serve the Vaadin UI. Integrating Vaadin's servlet for a full UI experience within an embedded server managed by or alongside SparkJava would be an additional setup step.

## Deployment (Heroku)

For deployment on platforms like Heroku, a `Procfile` is included:
```Procfile
web: java -jar target/demo-server-1.0-SNAPSHOT.jar
```
This command tells Heroku how to start the application using the executable JAR created by the `maven-shade-plugin`.
A `system.properties` file is also included to specify the Java runtime version for Heroku:
```
java.runtime.version=17
```

## Endpoints

The SparkJava server provides the following API endpoints:

### GET /context

Returns a JSON object containing sample context data.

**Optional Query Parameter:**

- `userId` (String): If provided, this ID will be included in the `userId` field of the response. If not provided, a default ID will be used.

**Example Request:**
```
GET http://localhost:4567/context?userId=testUser99
```

### POST /api/search-context

Accepts a JSON payload with a search query, performs an emulated web search, gets an emulated LLM summary, and returns both.

**Request Body:**
```json
{
  "query": "your search term"
}
```

**Example Request:**
```
POST http://localhost:4567/api/search-context
Content-Type: application/json

{
  "query": "Vaadin and SparkJava"
}
```

**Example Response (structure):**
```json
{
  "searchResults": [
    {
      "title": "Simulated Search Result 1 for Vaadin and SparkJava",
      "link": "http://example.com/result1",
      "snippet": "This is a dummy snippet..."
    }
    // ... more results
  ],
  "llmSummary": "This is a simulated LLM response to the prompt: 'Based on the following search results...'"
}
```

## Error Responses (API)

- If an invalid API route is accessed, a `404 Not Found` error with a JSON body `{"error":"Route not found"}` is returned.
- If an API call to a service (Search or LLM) fails, a `503 Service Unavailable` error with a JSON body like `{"error":"Search service failed", "details":"..."}` is returned.
- If there's a general internal server error for an API request, a `500 Internal Server Error` with a JSON body like `{"error":"Internal server error"}` is returned.
- Invalid JSON payloads to POST endpoints will result in a `400 Bad Request`.
```
