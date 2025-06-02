# Demo Model Context Server (Java with Jetty and Vaadin)

This is a demo server built with Java using an embedded Jetty server. It serves a Vaadin UI for user interaction and provides RESTful APIs (simulating a Model Context Protocol server) for fetching contextual data and performing searches augmented by an LLM.

## Prerequisites

- **Java JDK 17 or higher:** Required for compiling and running the application.
- **Apache Maven:** Used for project build and dependency management.
- **Node.js and npm:** Required by Vaadin for frontend dependency management and building the frontend bundle. Ensure these are installed and available in your system's PATH for local development and if building locally for production.

## Configuration

### API Keys

This application uses external services for web search (emulated Google Search) and LLM content generation (emulated OpenAI GPT-4o-mini). To connect to the actual services, you would need to:

1.  **Google Custom Search API:**
    *   Obtain an API Key and a Custom Search Engine ID (CX).
    *   Update the placeholder values in `src/main/java/com/example/service/SearchService.java` (or preferably, modify the code to read from environment variables or a configuration file).

2.  **OpenAI API:**
    *   Obtain an API Key from OpenAI.
    *   Update the placeholder value in `src/main/java/com/example/service/OpenAIService.java` (or preferably, modify the code to read from an environment variable or a configuration file).

**Note:** The current code uses placeholders and provides emulated responses for these services, allowing the application to run without real API keys.

## Building the Server

To build the server, navigate to the project's root directory and run:
```bash
mvn clean package
```
This command performs several actions:
- Compiles the Java code.
- Uses the `vaadin-maven-plugin` to prepare and build the Vaadin frontend assets (this step requires Node.js/npm).
- Utilizes the `maven-shade-plugin` to package the application into an executable "fat JAR" (e.g., `target/demo-server-1.0-SNAPSHOT.jar`). This JAR includes all necessary dependencies (Java classes, Vaadin frontend, Jetty, etc.) and is configured to be self-executable.

## Running the Server (Locally)

After a successful build, you can run the application using the executable JAR:
```bash
java -jar target/demo-server-1.0-SNAPSHOT.jar
```
The server will start and listen on the port specified by the `PORT` environment variable. If `PORT` is not set, it defaults to `4567`.

Once started:
- The **Vaadin UI** is accessible at the root path: `http://localhost:<PORT>/` (e.g., `http://localhost:4567/`).
- The **API endpoints** are available at:
    - `GET http://localhost:<PORT>/context`
    - `POST http://localhost:<PORT>/api/search-context`

## Deployment (Heroku)

This application is configured for deployment on Heroku:

-   **`Procfile`**: Specifies how Heroku should run the web process.
    ```Procfile
    web: java -jar target/demo-server-1.0-SNAPSHOT.jar
    ```
-   **`system.properties`**: Tells Heroku to use a Java 17 runtime.
    ```
    java.runtime.version=17
    ```
-   **Environment Variables on Heroku**: For optimal performance and to enable Vaadin's production mode, set the following environment variable in your Heroku app settings:
    *   `VAADIN_PRODUCTION_MODE=true`
    *   Optionally, if you configure the services to use environment variables for API keys (recommended), set them here as well (e.g., `GOOGLE_API_KEY`, `GOOGLE_CX`, `OPENAI_API_KEY`).

## API Endpoints

The embedded Jetty server provides the following API endpoints:

### GET /context

Returns a JSON object containing sample context data.

**Optional Query Parameter:**
- `userId` (String): If provided, this ID will be included in the `userId` field of the response. If not provided, a default ID ("defaultUser") will be used.

**Example Request:**
```
GET http://localhost:4567/context?userId=testUser99
```

### POST /api/search-context

Accepts a JSON payload with a search query, performs an emulated web search, gets an emulated LLM summary, and returns both.

**Request Body (JSON):**
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
  "query": "Vaadin with Jetty"
}
```

**Example Response (JSON structure):**
```json
{
  "searchResults": [
    {
      "title": "Simulated Search Result 1 for Vaadin with Jetty",
      "link": "http://example.com/result1",
      "snippet": "This is a dummy snippet..."
    }
    // ... more results
  ],
  "llmSummary": "This is a simulated LLM response to the prompt: 'Based on the following search results...'"
}
```

## Error Responses (API)

-   If an invalid API route is accessed, the Jetty server might return a generic 404 if not specifically handled by a servlet. The Vaadin servlet mapped to `/*` will handle root paths.
-   For defined API endpoints:
    -   `SearchServiceException` or `OpenAIServiceException` (if the emulated services were to throw them based on specific inputs) will result in a `503 Service Unavailable` with a JSON body like `{"error":"Search service failed", "details":"..."}`.
    -   Invalid JSON payloads to POST endpoints (e.g., `/api/search-context`) will result in a `400 Bad Request`.
    -   Other general internal server errors within the servlets will result in a `500 Internal Server Error`.
```
