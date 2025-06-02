package com.example.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.example.model.SearchContextResponse;
import com.example.model.SearchResultItem;
import com.example.service.SearchService;
import com.example.service.OpenAIService;
import com.example.exception.SearchServiceException;
import com.example.exception.OpenAIServiceException;

public class SearchContextServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final SearchService searchService = new SearchService();
    private final OpenAIService openAIService = new OpenAIService();

    // Inner class for parsing the search request payload
    private static class SearchApiRequest {
        String query;
        // No explicit getters/setters needed if Gson can access fields directly (default behavior)
        // or if fields are public. Private fields are fine with default Gson.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        SearchApiRequest searchApiRequest;
        String query;

        try {
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            if (requestBody == null || requestBody.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(Map.of("error", "Request body is missing or empty.")));
                return;
            }
            searchApiRequest = gson.fromJson(requestBody, SearchApiRequest.class);
            if (searchApiRequest == null || searchApiRequest.query == null || searchApiRequest.query.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(Map.of("error", "Query field in JSON payload is missing or empty.")));
                return;
            }
            query = searchApiRequest.query;
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Invalid JSON payload", "details", e.getMessage())));
            return;
        } catch (Exception e) { // Catching broader exceptions during request reading/parsing
            System.err.println("Error processing request in SearchContextServlet: " + e.getMessage());
            e.printStackTrace();
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(gson.toJson(Map.of("error", "Failed to read or parse request.", "details", e.getMessage())));
            }
            return;
        }

        try {
            List<SearchResultItem> searchResults = searchService.performSearch(query);

            StringBuilder promptBuilder = new StringBuilder("Based on the following search results, provide a brief summary:\n");
            for (SearchResultItem item : searchResults) {
                promptBuilder.append("Title: ").append(item.getTitle()).append("\n");
                promptBuilder.append("Snippet: ").append(item.getSnippet()).append("\n\n");
            }
            String llmPrompt = promptBuilder.toString();
            String llmSummary = openAIService.getCompletion(llmPrompt);

            SearchContextResponse finalResponse = new SearchContextResponse(searchResults, llmSummary);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(finalResponse));

        } catch (SearchServiceException e) {
            System.err.println("SearchServiceException in SearchContextServlet: " + e.getMessage());
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().write(gson.toJson(Map.of("error", "Search service failed", "details", e.getMessage())));
            }
        } catch (OpenAIServiceException e) {
            System.err.println("OpenAIServiceException in SearchContextServlet: " + e.getMessage());
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().write(gson.toJson(Map.of("error", "LLM service failed", "details", e.getMessage())));
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in SearchContextServlet: " + e.getMessage());
            e.printStackTrace();
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(gson.toJson(Map.of("error", "An unexpected error occurred.", "details", e.getMessage())));
            }
        }
    }
}
