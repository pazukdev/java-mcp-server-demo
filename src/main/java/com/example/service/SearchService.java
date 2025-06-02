package com.example.service;

import com.example.model.SearchResultItem;
import com.example.exception.SearchServiceException; // Import custom exception
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchService {

    private static final String GOOGLE_API_KEY_PLACEHOLDER = "YOUR_GOOGLE_API_KEY";
    private static final String GOOGLE_CX_PLACEHOLDER = "YOUR_GOOGLE_CX";

    public SearchService() {
        // Constructor can be used for initialization if needed
    }

    public List<SearchResultItem> performSearch(String query) {
        try {
            if (GOOGLE_API_KEY_PLACEHOLDER.equals("YOUR_GOOGLE_API_KEY") || GOOGLE_API_KEY_PLACEHOLDER.isEmpty() ||
                GOOGLE_CX_PLACEHOLDER.equals("YOUR_GOOGLE_CX") || GOOGLE_CX_PLACEHOLDER.isEmpty()) {
                System.out.println("Google API Key or CX not configured/empty. Returning dummy search results for query: " + query);
            } else {
                System.out.println("Google API Key and CX are present (placeholders). Simulating search for query: " + query);
                // Actual API call would go here
            }

            // Test condition to simulate an error
            if ("throwsearcherror".equalsIgnoreCase(query)) {
                throw new RuntimeException("Simulated underlying search error from test condition.");
            }

            List<SearchResultItem> results = new ArrayList<>();

            results.add(new SearchResultItem(
                    "Simulated Search Result 1 for " + query,
                    "http://example.com/result1",
                    "This is a dummy snippet for the first search result."
            ));

            results.add(new SearchResultItem(
                    "Simulated Search Result 2 for " + query,
                    "http://example.com/result2",
                    "Another dummy snippet, this time for the second result."
            ));

            return results;
        } catch (Exception e) {
            // Catch any exception (including the simulated one) and wrap it
            throw new SearchServiceException("Error during emulated search: " + e.getMessage(), e);
        }
    }

    // Simple main method for testing
    public static void main(String[] args) {
        SearchService searchService = new SearchService();
        try {
            System.out.println("Testing normal search:");
            List<SearchResultItem> dummyResults = searchService.performSearch("test query");
            for (SearchResultItem result : dummyResults) {
                System.out.println("Title: " + result.getTitle());
                System.out.println("Link: " + result.getLink());
                System.out.println("Snippet: " + result.getSnippet());
                System.out.println("---");
            }

            System.out.println("\nTesting search with simulated error:");
            searchService.performSearch("throwsearcherror"); // This should throw
        } catch (SearchServiceException e) {
            System.err.println("Caught expected SearchServiceException: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }
        }
    }
}
