# Demo Model Context Server (Java)

This is a simple demo server built with Java and SparkJava that simulates a Model Context Protocol server.
It provides a sample context as a JSON response.

## Prerequisites

- Java JDK 8 or higher
- Apache Maven

## Building the Server

To build the server, navigate to the project's root directory and run:

```bash
mvn clean package
```

This will compile the code and create a JAR file in the `target/` directory.

## Running the Server

After building, you can run the server using:

```bash
mvn exec:java -Dexec.mainClass="com.example.DemoServer"
```

Alternatively, you can run the fat JAR produced by `mvn package` (if `maven-shade-plugin` or similar was configured, which it isn't currently, so this command is more direct):

The server will start and listen on port `4567` by default.

## Endpoints

### GET /context

Returns a JSON object containing sample context data.

**Optional Query Parameter:**

- `userId` (String): If provided, this ID will be included in the `userId` field of the response. If not provided, a default ID will be used.

**Example Request:**

```
GET http://localhost:4567/context
```

**Example Request with `userId`:**

```
GET http://localhost:4567/context?userId=testUser99
```

**Example Response:**

```json
{
  "userId": "testUser99", // or "defaultUser" if no parameter is given
  "theme": "dark",
  "preferences": {
    "notifications": "enabled",
    "language": "en"
  },
  "timestamp": "..." // current ISO 8601 timestamp
}
```

## Error Responses

- If an invalid route is accessed, a `404 Not Found` error with a JSON body `{"error":"Route not found"}` is returned.
- If an internal server error occurs, a `500 Internal Server Error` with a JSON body like `{"error":"Internal server error"}` is returned.
