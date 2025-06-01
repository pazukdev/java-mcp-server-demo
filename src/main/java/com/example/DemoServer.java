package com.example;

import static spark.Spark.*;
import com.example.model.ContextData;
import com.example.model.UserPreferences;
import com.google.gson.Gson;
import java.time.Instant; // Using Instant for a modern way to get a timestamp

public class DemoServer {
    public static void main(String[] args) {
        port(4567); // Set the port for the server
        Gson gson = new Gson();

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

        System.out.println("Server started on port 4567, with /context endpoint and error handlers.");
    }
}
