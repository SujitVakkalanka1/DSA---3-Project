import dsa.SkillIndexer;
import server.SimpleHttpServer;
import storage.DataStore;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * End-to-End Server & API Integration Test
 * Verifies all HTML pages, CSS/JS static assets, and REST API endpoints.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class ServerIntegrationTest {

    private static final int TEST_PORT = 8089;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================================");
        System.out.println("  STARTING END-TO-END SERVER INTEGRATION TEST (PORT " + TEST_PORT + ")");
        System.out.println("==================================================================");

        SkillIndexer skillIndexer = new SkillIndexer();
        DataStore dataStore = new DataStore(skillIndexer);
        SimpleHttpServer server = new SimpleHttpServer(TEST_PORT, dataStore, skillIndexer);
        server.start();

        int passed = 0;
        int total = 0;

        try {
            // 1. Static Web Pages Tests
            String[] pages = {
                "/",
                "/dashboard",
                "/candidates",
                "/jobs",
                "/match",
                "/assignments",
                "/team-optimizer",
                "/analytics",
                "/architecture",
                "/css/style.css",
                "/js/api.js",
                "/js/app.js"
            };

            for (String p : pages) {
                total++;
                if (testEndpoint("GET", p, null, 200, null)) passed++;
            }

            // 2. REST API Endpoint Tests
            total++;
            if (testEndpoint("GET", "/api/candidates", null, 200, "Aarav Shah")) passed++;

            total++;
            if (testEndpoint("GET", "/api/jobs", null, 200, "Java Backend Developer")) passed++;

            total++;
            if (testEndpoint("POST", "/api/normalize", "{\"skill\":\"Javascrpt\"}", 200, "JavaScript")) passed++;

            total++;
            if (testEndpoint("POST", "/api/match", "{\"candidateId\":\"C-101\",\"jobId\":\"J-101\"}", 200, "totalScore")) passed++;

            total++;
            if (testEndpoint("GET", "/api/match/matrix", null, 200, "matrix")) passed++;

            total++;
            if (testEndpoint("GET", "/api/assignments", null, 200, "assignments")) passed++;

            total++;
            if (testEndpoint("POST", "/api/optimize-team", "{\"skills\":[\"Java\",\"SQL\",\"DSA\",\"HTML\",\"CSS\",\"Git\"]}", 200, "selectedTeam")) passed++;

            total++;
            if (testEndpoint("GET", "/api/analytics", null, 200, "totalCandidates")) passed++;

            total++;
            if (testEndpoint("GET", "/api/skill-index", null, 200, "skill")) passed++;

            // 3. Candidate CRUD Test
            total++;
            String newCandJson = "{\"name\":\"Test Student\",\"rawSkills\":[\"Java\",\"Spring\"],\"experienceYears\":1,\"availability\":\"Immediate\"}";
            if (testEndpoint("POST", "/api/candidates", newCandJson, 201, "Test Student")) passed++;

            // 4. New 6-Module DSA Endpoints Tests
            total++;
            if (testEndpoint("POST", "/api/string-search", "{\"text\":\"Java Developer with Spring Boot and SQL\",\"pattern\":\"Java\"}", 200, "kmp")) passed++;

            total++;
            if (testEndpoint("GET", "/api/network-flow", null, 200, "maxFlow")) passed++;

            total++;
            if (testEndpoint("POST", "/api/bitmask-team", "{\"skills\":[\"Java\",\"SQL\",\"DSA\"]}", 200, "optimalTeamSize")) passed++;

            total++;
            if (testEndpoint("GET", "/api/parallel-benchmark", null, 200, "availableProcessors")) passed++;

        } finally {
            server.stop();
            System.out.println("Server stopped.");
        }

        System.out.println("\n==================================================================");
        System.out.println(String.format("  INTEGRATION TEST SUMMARY: %d / %d PASSED", passed, total));
        System.out.println("==================================================================");

        if (passed == total) {
            System.out.println("  ✓ ALL ENDPOINTS & STATIC PAGES VERIFIED SUCCESSFULLY!");
        } else {
            System.err.println("  ✕ SOME ENDPOINT TESTS FAILED.");
            System.exit(1);
        }
    }

    private static boolean testEndpoint(String method, String path, String body, int expectedStatus, String expectedSubstring) {
        System.out.print(String.format("Testing %-6s %-28s ... ", method, path));
        try {
            URL url = new URL(BASE_URL + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }

            int status = conn.getResponseCode();
            if (status != expectedStatus) {
                System.out.println("FAILED (Status " + status + " != " + expectedStatus + ")");
                return false;
            }

            try (InputStream is = (status >= 400 ? conn.getErrorStream() : conn.getInputStream())) {
                String responseText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (expectedSubstring != null && !responseText.contains(expectedSubstring)) {
                    System.out.println("FAILED (Missing expected substring: " + expectedSubstring + ")");
                    return false;
                }
            }

            System.out.println("PASSED (200 OK)");
            return true;
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return false;
        }
    }
}
