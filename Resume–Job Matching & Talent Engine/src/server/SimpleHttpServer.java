package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dsa.SkillIndexer;
import storage.DataStore;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Built-in Lightweight Java HTTP Server using com.sun.net.httpserver.HttpServer
 * Requires zero external dependencies, no Spring Boot, no Node.js.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class SimpleHttpServer {

    private final int port;
    private final DataStore dataStore;
    private final SkillIndexer skillIndexer;
    private HttpServer server;

    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("html", "text/html; charset=UTF-8");
        MIME_TYPES.put("htm", "text/html; charset=UTF-8");
        MIME_TYPES.put("css", "text/css; charset=UTF-8");
        MIME_TYPES.put("js", "application/javascript; charset=UTF-8");
        MIME_TYPES.put("json", "application/json; charset=UTF-8");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("txt", "text/plain; charset=UTF-8");
    }

    public SimpleHttpServer(int port, DataStore dataStore, SkillIndexer skillIndexer) {
        this.port = port;
        this.dataStore = dataStore;
        this.skillIndexer = skillIndexer;
    }

    public void start() throws IOException {
        int activePort = this.port;
        try {
            server = HttpServer.create(new InetSocketAddress(activePort), 0);
        } catch (IOException ex) {
            // Port may be in use, try alternate port 8081
            activePort = this.port + 1;
            server = HttpServer.create(new InetSocketAddress(activePort), 0);
        }

        // Setup API handler context
        ApiHandler apiHandler = new ApiHandler(dataStore, skillIndexer);
        server.createContext("/api", apiHandler);

        // Setup Static Web file handler context
        server.createContext("/", new StaticFileHandler());

        // Use multi-threaded executor with daemon threads for handling concurrent browser requests
        this.executor = Executors.newFixedThreadPool(8, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        server.setExecutor(this.executor);
        server.start();

        System.out.println("==================================================================");
        System.out.println("  RÉSUMÉ-JOB MATCHING & TALENT ENGINE  |  DSA-3 (Team 14)");
        System.out.println("  KL University, Hyderabad  |  Section 10");
        System.out.println("==================================================================");
        System.out.println("  * Server running at: http://localhost:" + activePort);
        System.out.println("  * Landing Page:     http://localhost:" + activePort + "/");
        System.out.println("  * Dashboard:        http://localhost:" + activePort + "/dashboard");
        System.out.println("  * Candidates:       http://localhost:" + activePort + "/candidates");
        System.out.println("  * Jobs:             http://localhost:" + activePort + "/jobs");
        System.out.println("  * Match Engine:     http://localhost:" + activePort + "/match");
        System.out.println("  * Assignments:      http://localhost:" + activePort + "/assignments");
        System.out.println("  * Team Optimizer:   http://localhost:" + activePort + "/team-optimizer");
        System.out.println("  * Analytics:        http://localhost:" + activePort + "/analytics");
        System.out.println("  * Architecture:     http://localhost:" + activePort + "/architecture");
        System.out.println("==================================================================");
    }

    private java.util.concurrent.ExecutorService executor;

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /**
     * Handler for serving static HTML, CSS, JavaScript, and asset files.
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Route mapping for clean URLs
            String filePath;
            if (path.equals("/") || path.isEmpty()) {
                filePath = "web/index.html";
            } else if (path.equals("/dashboard") || path.equals("/dashboard.html")) {
                filePath = "web/dashboard.html";
            } else if (path.equals("/candidates") || path.equals("/candidates.html")) {
                filePath = "web/candidates.html";
            } else if (path.equals("/jobs") || path.equals("/jobs.html")) {
                filePath = "web/jobs.html";
            } else if (path.equals("/match") || path.equals("/match.html")) {
                filePath = "web/match.html";
            } else if (path.equals("/assignments") || path.equals("/assignments.html")) {
                filePath = "web/assignments.html";
            } else if (path.equals("/team-optimizer") || path.equals("/team-optimizer.html")) {
                filePath = "web/team-optimizer.html";
            } else if (path.equals("/analytics") || path.equals("/analytics.html")) {
                filePath = "web/analytics.html";
            } else if (path.equals("/architecture") || path.equals("/architecture.html")) {
                filePath = "web/architecture.html";
            } else {
                // Remove leading slash if any
                String relPath = path.startsWith("/") ? path.substring(1) : path;
                filePath = relPath.startsWith("web/") ? relPath : "web/" + relPath;
            }

            File file = new File(filePath);
            if (!file.exists() || file.isDirectory()) {
                // Fallback to index.html if not found or 404
                String notFound = "<!DOCTYPE html><html><head><title>404 Not Found</title><style>body{background:#030403;color:#FBFBE0;font-family:sans-serif;padding:40px;text-align:center;}a{color:#ECF223;}</style></head><body><h1>404 Page Not Found</h1><p>The requested page was not found.</p><p><a href='/'>Return to Home</a></p></body></html>";
                byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            // Determine MIME type
            String ext = "";
            int dotIdx = file.getName().lastIndexOf('.');
            if (dotIdx > 0) {
                ext = file.getName().substring(dotIdx + 1).toLowerCase();
            }
            String mimeType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }
}
