package com.example.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import com.example.model.SearchContextResponse; // Backend response POJO
// import com.example.model.SearchRequest; // Not strictly needed if constructing JSON directly
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map; // For simple JSON construction with Map.of


@Route("") // Map to the root path of the Vaadin application
public class MainView extends VerticalLayout {

    private final HttpClient httpClient = HttpClient.newHttpClient(); // Reusable HttpClient
    private final Gson gson = new Gson(); // Reusable Gson instance

    public MainView() {
        H1 pageTitle = new H1("Model Context Search UI (Vaadin)");

        TextField searchQuery = new TextField("Enter your search query");
        searchQuery.setWidth("300px");

        Button searchButton = new Button("Search and Contextualize");
        Pre resultsArea = new Pre();
        resultsArea.setWidthFull();
        resultsArea.getStyle().set("border", "1px solid #ccc");
        resultsArea.getStyle().set("padding", "10px");
        resultsArea.setMinHeight("200px"); // Ensure it's visible

        HorizontalLayout inputLayout = new HorizontalLayout(searchQuery, searchButton);
        inputLayout.setAlignItems(Alignment.BASELINE);

        searchButton.addClickListener(event -> {
            String query = searchQuery.getValue();
            if (query == null || query.trim().isEmpty()) {
                Notification.show("Please enter a search query.");
                return;
            }

            // Prepare request body
            String requestBody = gson.toJson(Map.of("query", query));

            // Create HttpRequest
            HttpRequest backendRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/api/search-context")) // SparkJava backend URL
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            resultsArea.setText("Loading results for '" + query + "'..."); // Indicate loading

            // Send request asynchronously
            httpClient.sendAsync(backendRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // This block runs in the HTTP client's thread
                    if (response.statusCode() == 200) {
                        try {
                            SearchContextResponse scr = gson.fromJson(response.body(), SearchContextResponse.class);
                            return scr;
                        } catch (JsonSyntaxException e) {
                            // Handle JSON parsing error
                            System.err.println("Error parsing JSON response from backend: " + e.getMessage());
                            // It's important to signal error to the next stage
                            throw new RuntimeException("Error parsing backend response. Body: " + response.body(), e);
                        }
                    } else {
                        // Handle HTTP error status codes
                        System.err.println("Backend returned error status: " + response.statusCode() + " Body: " + response.body());
                         // It's important to signal error to the next stage
                        throw new RuntimeException("Backend error. Status: " + response.statusCode() + ". Body: " + response.body());
                    }
                })
                .thenAccept(scr -> {
                    // This block also runs in the HTTP client's thread initially
                    // UI updates must be pushed to the UI thread via ui.access()
                    getUI().ifPresent(ui -> ui.access(() -> {
                        if (scr != null) {
                            // For now, display pretty-printed JSON.
                            // In a real app, you'd format this nicely.
                            resultsArea.setText(gson.toJson(scr)); // Using the existing gson instance for pretty printing
                            Notification.show("Search successful!");
                        }
                        // Error case is now handled by exceptionally block after a throw in thenApply
                    }));
                })
                .exceptionally(e -> {
                    // This block handles exceptions from sendAsync or any previous stage (like thenApply)
                    getUI().ifPresent(ui -> ui.access(() -> {
                        resultsArea.setText("Error: " + e.getMessage());
                        Notification.show("Failed to fetch data: " + e.getMessage());
                        // Log the full exception for debugging
                        e.printStackTrace();
                    }));
                    return null; // Standard for exceptionally block
                });
        });

        setAlignItems(Alignment.CENTER); // Center the content of the VerticalLayout
        add(pageTitle, inputLayout, resultsArea);

        // Optional: Add some padding or spacing around the main layout
        getStyle().set("padding", "20px");
    }
}
