package com.example;

// Removed SparkJava imports: import static spark.Spark.*;
// Gson, Services, Models, Custom Exceptions etc. are kept for when API endpoints are re-added
import com.example.model.ContextData;
import com.example.model.UserPreferences;
import com.example.model.SearchResultItem;
import com.example.model.SearchContextResponse;
import com.example.exception.OpenAIServiceException;
import com.example.exception.SearchServiceException;
import com.example.service.OpenAIService;
import com.example.service.SearchService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

// Jetty and Vaadin imports
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import com.vaadin.flow.server.VaadinServlet;
import com.example.ui.MainView; // Your Vaadin UI class
import com.example.api.ContextServlet; // Import for the Context API servlet
import com.example.api.SearchContextServlet; // Import for the Search Context API servlet

import java.time.Instant; // Kept for now, used in old /context route
import java.util.HashMap; // Kept for now
import java.util.List;    // Kept for now
import java.util.Map;     // Kept for now

public class DemoServer {

    // Inner class for parsing the search request payload - kept for future API re-integration
    private static class SearchRequest {
        private String query;
        public SearchRequest() {}
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
    }

    private static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            try {
                return Integer.parseInt(herokuPort);
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse PORT environment variable: " + herokuPort + ". Defaulting to 4567.");
                return 4567;
            }
        }
        return 4567; // Default port if PORT env var is not set
    }

    public static void main(String[] args) throws Exception {
        int port = getHerokuAssignedPort();
        Server server = new Server(port);

        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        // Vaadin Servlet
        ServletHolder vaadinServletHolder = new ServletHolder(VaadinServlet.class);
        // Corrected init parameter for UI class for generic Servlet spec (Vaadin might also pick up from annotations)
        // However, explicit is good. Vaadin's own documentation sometimes shows `UI` or relies on annotations.
        // For VaadinServlet, the standard way if not using annotations for auto-detection is often just the class name.
        // But to be very specific for servlet init params:
        vaadinServletHolder.setInitParameter("UI", MainView.class.getName()); // Using "UI" as per standard for some setups.
                                                                                // Vaadin 10+ often uses a different mechanism.
                                                                                // If this doesn't work, it might be `vaadin.frontend.url.es6` or similar,
                                                                                // or simply relying on @Route scan.
                                                                                // For VaadinServlet, "UI" is a common parameter.

        // Enable production mode if VAADIN_PRODUCTION_MODE environment variable is set to true
        String productionModeEnv = System.getenv("VAADIN_PRODUCTION_MODE");
        if (productionModeEnv != null && productionModeEnv.equalsIgnoreCase("true")) {
            // For Vaadin 14+ (including 24), setting a system property is a common way
            // or using a servlet init parameter "productionMode".
            vaadinServletHolder.setInitParameter("productionMode", "true");
            System.out.println("Vaadin production mode enabled via VAADIN_PRODUCTION_MODE environment variable.");
        } else {
            // Default to development mode
            vaadinServletHolder.setInitParameter("productionMode", "false"); // Explicitly false
            System.out.println("Vaadin development mode enabled (VAADIN_PRODUCTION_MODE not 'true' or not set).");
        }

        contextHandler.addServlet(vaadinServletHolder, "/*");

        // --- API Endpoints ---
        // Note: Gson, SearchService, OpenAIService are not instantiated here anymore
        // unless they are passed to the servlets that need them.
        // For ContextServlet, Gson is self-contained.

        // Add ContextServlet for the /context API endpoint
        ServletHolder contextServletHolder = new ServletHolder(ContextServlet.class);
        contextHandler.addServlet(contextServletHolder, "/context");

        // Add SearchContextServlet for the /api/search-context API endpoint
        ServletHolder searchContextServletHolder = new ServletHolder(SearchContextServlet.class);
        contextHandler.addServlet(searchContextServletHolder, "/api/search-context");

        // --- End API Endpoints ---

        server.setHandler(contextHandler);

        server.start();
        System.out.println("Jetty server started on port " + port + ".");
        System.out.println("Vaadin UI should be available at http://localhost:" + port + "/");
        System.out.println("API endpoint /context is active.");
        System.out.println("API endpoint /api/search-context is active."); // Updated message

        server.join();
    }
}
