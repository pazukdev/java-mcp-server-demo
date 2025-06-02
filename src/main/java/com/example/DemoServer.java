package com.example;

import static spark.Spark.*;
import com.example.model.ContextData;
import com.example.model.UserPreferences;
import com.example.model.SearchResultItem;
import com.example.model.SearchContextResponse;
import com.example.exception.OpenAIServiceException; // Import custom exception
import com.example.exception.SearchServiceException; // Import custom exception
import com.example.service.OpenAIService;
import com.example.service.SearchService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException; // For catching GSON parsing errors

import java.time.Instant; // Using Instant for a modern way to get a timestamp
import java.util.HashMap; // Added import for HashMap
import java.util.List;    // Added import
import java.util.Map;     // Added import

public class DemoServer {

    // Inner class for parsing the search request payload - now a proper POJO
    private static class SearchRequest {
        private String query;

        // Default constructor for Gson
        public SearchRequest() {}

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    private static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            try {
                return Integer.parseInt(herokuPort);
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse PORT environment variable: " + herokuPort + ". Defaulting to 4567.");
                // Optionally log with SLF4J if/when it's configured
                return 4567;
            }
        }
        return 4567; // Default port if PORT env var is not set
    }

    public static void main(String[] args) {
        port(getHerokuAssignedPort()); // Use the dynamic port
        Gson gson = new Gson();
        SearchService searchService = new SearchService(); // Instantiated SearchService
        OpenAIService openAIService = new OpenAIService(); // Instantiated OpenAIService

        // Define a GET route for /context
        get("/context", (request, response) -> {
            try {
                response.type("application/json");

                // Retrieve userId from query parameter
                String userIdParam = request.queryParams("userId");
                String userIdToUse;

                if (userIdParam != null && !userIdParam.isEmpty()) {
                    userIdToUse = userIdParam;
                } else {
                    userIdToUse = "defaultUser"; // Default userId if not provided
                }

                // Create sample UserPreferences
                UserPreferences preferences = new UserPreferences("enabled", "en");

                // Create ContextData using the determined userId
                ContextData contextDataObject = new ContextData(
                        userIdToUse,
                        "dark",
                        preferences,
                        Instant.now().toString() // Get current timestamp as ISO 8601 string
                );

                // Serialize ContextData to JSON and return
                return gson.toJson(contextDataObject);
            } catch (Exception e) {
                e.printStackTrace(); // Log the error
                response.status(500);
                return "{\"error\":\"Internal server error\"}";
            }
        });

        // Define a POST route for /api/search-context
        post("/api/search-context", (request, response) -> {
            response.type("application/json");
            try {
                // Ensure request.body() is not null before parsing
                String requestBody = request.body();
                if (requestBody == null || requestBody.trim().isEmpty()) {
                    response.status(400); // Bad Request
                    return gson.toJson(Map.of("error", "Request body is missing or empty."));
                }

                SearchRequest searchRequest = gson.fromJson(requestBody, SearchRequest.class);

                if (searchRequest == null || searchRequest.getQuery() == null || searchRequest.getQuery().trim().isEmpty()) {
                    response.status(400); // Bad Request
                    return gson.toJson(Map.of("error", "Query parameter is missing or empty in JSON payload."));
                }
                String query = searchRequest.getQuery();

                List<SearchResultItem> searchResults;
                try {
                    searchResults = searchService.performSearch(query);
                } catch (SearchServiceException e) {
                    e.printStackTrace(); // Log the specific service error
                    response.status(503); // Service Unavailable
                    return gson.toJson(Map.of("error", "Search service failed", "details", e.getMessage()));
                }

                // Construct prompt for LLM using SearchResultItem getters
                StringBuilder promptBuilder = new StringBuilder("Based on the following search results, provide a brief summary:\n");
                for (SearchResultItem item : searchResults) {
                    promptBuilder.append("Title: ").append(item.getTitle()).append("\n");
                    promptBuilder.append("Snippet: ").append(item.getSnippet()).append("\n\n");
                }
                String llmPrompt = promptBuilder.toString();

                String llmSummary;
                try {
                    llmSummary = openAIService.getCompletion(llmPrompt);
                } catch (OpenAIServiceException e) {
                    e.printStackTrace(); // Log the specific service error
                    response.status(503); // Service Unavailable
                    return gson.toJson(Map.of("error", "LLM service failed", "details", e.getMessage()));
                }

                // Structure final response using SearchContextResponse POJO
                SearchContextResponse finalResponse = new SearchContextResponse(searchResults, llmSummary);

                return gson.toJson(finalResponse);
            } catch (JsonSyntaxException e) {
                e.printStackTrace(); // Log JSON parsing errors
                response.status(400); // Bad Request
                return gson.toJson(Map.of("error", "Invalid JSON payload: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace(); // Log other generic errors
                response.status(500);
                // Ensure the specific error message from the exception is included for better diagnostics
                return gson.toJson(Map.of("error", "Internal server error: " + e.getMessage()));
            }
        });

        // Handler for 404 - Route not found
        notFound((req, res) -> {
            res.type("application/json");
            res.status(404);
            return "{\"error\":\"Route not found\"}";
        });

        // Handler for 500 - Internal server error (global)
        internalServerError((req, res) -> {
            res.type("application/json");
            res.status(500);
            return "{\"error\":\"Unexpected internal server error\"}";
        });

        System.out.println("Server starting on port: " + port() + " with /context and /api/search-context endpoints and error handlers.");
    }
}
