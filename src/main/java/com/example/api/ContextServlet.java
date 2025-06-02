package com.example.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map; // For error response

import com.google.gson.Gson;
import com.example.model.ContextData;
import com.example.model.UserPreferences;

public class ContextServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String userIdParam = request.getParameter("userId");
        String userIdToUse = (userIdParam != null && !userIdParam.isEmpty()) ? userIdParam : "defaultUser";

        UserPreferences preferences = new UserPreferences("enabled", "en");
        ContextData contextData = new ContextData(
                userIdToUse,
                "dark",
                preferences,
                Instant.now().toString()
        );

        try {
            response.getWriter().write(gson.toJson(contextData));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            // Basic error handling for the servlet
            System.err.println("Error in ContextServlet: " + e.getMessage());
            e.printStackTrace(); // For more detailed logging to console/Logback

            // Ensure response is not committed before trying to write error
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // It's good practice to ensure the content type is still JSON for the error
                response.setContentType("application/json");
                try {
                    response.getWriter().write(gson.toJson(Map.of("error", "Failed to generate context", "details", e.getMessage())));
                } catch (IOException ioEx) {
                    // If writing the error response itself fails
                    System.err.println("Error writing error response to client: " + ioEx.getMessage());
                }
            } else {
                System.err.println("Response already committed. Cannot send error JSON to client for ContextServlet error: " + e.getMessage());
            }
        }
    }
}
