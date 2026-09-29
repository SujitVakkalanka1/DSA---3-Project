package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dsa.*;
import model.Candidate;
import model.Job;
import model.MatchResult;
import storage.DataStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * REST API Handler for candidate, job, matching, Hungarian assignment,
 * Greedy set cover team optimization, and analytics endpoints.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class ApiHandler implements HttpHandler {

    private final DataStore dataStore;
    private final SkillIndexer skillIndexer;

    public ApiHandler(DataStore dataStore, SkillIndexer skillIndexer) {
        this.dataStore = dataStore;
        this.skillIndexer = skillIndexer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        // Handle CORS Preflight
        if (method.equals("OPTIONS")) {
            sendResponse(exchange, 204, "");
            return;
        }

        try {
            if (path.startsWith("/api/candidates")) {
                handleCandidates(exchange, method, uri);
            } else if (path.startsWith("/api/jobs")) {
                handleJobs(exchange, method, uri);
            } else if (path.equals("/api/match/matrix")) {
                handleMatchMatrix(exchange);
            } else if (path.equals("/api/match")) {
                handleMatch(exchange, method);
            } else if (path.equals("/api/assignments")) {
                handleAssignments(exchange);
            } else if (path.equals("/api/optimize-team")) {
                handleOptimizeTeam(exchange, method);
            } else if (path.equals("/api/analytics")) {
                handleAnalytics(exchange);
            } else if (path.equals("/api/normalize")) {
                handleNormalize(exchange, method);
            } else if (path.equals("/api/skill-index")) {
                handleSkillIndex(exchange);
            } else if (path.equals("/api/reset")) {
                handleReset(exchange, method);
            } else if (path.equals("/api/string-search")) {
                handleStringSearch(exchange, method);
            } else if (path.equals("/api/network-flow")) {
                handleNetworkFlow(exchange);
            } else if (path.equals("/api/bitmask-team")) {
                handleBitmaskTeam(exchange, method);
            } else if (path.equals("/api/parallel-benchmark")) {
                handleParallelBenchmark(exchange);
            } else {
                sendJsonResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void handleCandidates(HttpExchange exchange, String method, URI uri) throws IOException {
        if (method.equals("GET")) {
            List<Candidate> list = dataStore.getAllCandidates();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).toJson());
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        } else if (method.equals("POST")) {
            String body = readBody(exchange);
            String name = extractString(body, "name");
            String expStr = extractString(body, "experienceYears");
            String avail = extractString(body, "availability");
            List<String> rawSkills = extractList(body, "rawSkills");

            int exp = expStr.isEmpty() ? 0 : Integer.parseInt(expStr);
            Candidate created = dataStore.addCandidate(name, rawSkills, exp, avail.isEmpty() ? "Immediate" : avail);
            sendJsonResponse(exchange, 201, created.toJson());
        } else if (method.equals("PUT")) {
            String body = readBody(exchange);
            String id = extractString(body, "id");
            String name = extractString(body, "name");
            String expStr = extractString(body, "experienceYears");
            String avail = extractString(body, "availability");
            List<String> rawSkills = extractList(body, "rawSkills");

            int exp = expStr.isEmpty() ? 0 : Integer.parseInt(expStr);
            Candidate updated = dataStore.updateCandidate(id, name, rawSkills, exp, avail.isEmpty() ? "Immediate" : avail);
            if (updated != null) {
                sendJsonResponse(exchange, 200, updated.toJson());
            } else {
                sendJsonResponse(exchange, 404, "{\"error\":\"Candidate not found\"}");
            }
        } else if (method.equals("DELETE")) {
            String query = uri.getQuery();
            String id = getQueryParam(query, "id");
            if (id != null && dataStore.deleteCandidate(id)) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Candidate deleted\"}");
            } else {
                sendJsonResponse(exchange, 404, "{\"error\":\"Candidate not found or invalid id\"}");
            }
        } else {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private void handleJobs(HttpExchange exchange, String method, URI uri) throws IOException {
        if (method.equals("GET")) {
            List<Job> list = dataStore.getAllJobs();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).toJson());
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        } else if (method.equals("POST")) {
            String body = readBody(exchange);
            String title = extractString(body, "title");
            String dept = extractString(body, "department");
            String expStr = extractString(body, "minExperienceYears");
            String desc = extractString(body, "description");
            List<String> requiredSkills = extractList(body, "requiredSkills");

            int exp = expStr.isEmpty() ? 0 : Integer.parseInt(expStr);
            Job created = dataStore.addJob(title, dept, requiredSkills, exp, desc);
            sendJsonResponse(exchange, 201, created.toJson());
        } else if (method.equals("PUT")) {
            String body = readBody(exchange);
            String id = extractString(body, "id");
            String title = extractString(body, "title");
            String dept = extractString(body, "department");
            String expStr = extractString(body, "minExperienceYears");
            String desc = extractString(body, "description");
            List<String> requiredSkills = extractList(body, "requiredSkills");

            int exp = expStr.isEmpty() ? 0 : Integer.parseInt(expStr);
            Job updated = dataStore.updateJob(id, title, dept, requiredSkills, exp, desc);
            if (updated != null) {
                sendJsonResponse(exchange, 200, updated.toJson());
            } else {
                sendJsonResponse(exchange, 404, "{\"error\":\"Job not found\"}");
            }
        } else if (method.equals("DELETE")) {
            String query = uri.getQuery();
            String id = getQueryParam(query, "id");
            if (id != null && dataStore.deleteJob(id)) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Job deleted\"}");
            } else {
                sendJsonResponse(exchange, 404, "{\"error\":\"Job not found or invalid id\"}");
            }
        } else {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private void handleMatch(HttpExchange exchange, String method) throws IOException {
        if (!method.equals("POST")) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        String body = readBody(exchange);
        String candidateId = extractString(body, "candidateId");
        String jobId = extractString(body, "jobId");

        Candidate c = dataStore.getCandidateById(candidateId);
        Job j = dataStore.getJobById(jobId);

        if (c == null || j == null) {
            sendJsonResponse(exchange, 404, "{\"error\":\"Candidate or Job not found\"}");
            return;
        }

        MatchResult mr = WeightedMatcher.computeMatch(c, j);
        sendJsonResponse(exchange, 200, mr.toJson());
    }

    private void handleMatchMatrix(HttpExchange exchange) throws IOException {
        List<Candidate> candidates = dataStore.getAllCandidates();
        List<Job> jobs = dataStore.getAllJobs();

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"candidateNames\":[");
        for (int i = 0; i < candidates.size(); i++) {
            sb.append("\"").append(escape(candidates.get(i).getName())).append("\"");
            if (i < candidates.size() - 1) sb.append(",");
        }
        sb.append("],\"jobTitles\":[");
        for (int j = 0; j < jobs.size(); j++) {
            sb.append("\"").append(escape(jobs.get(j).getTitle())).append("\"");
            if (j < jobs.size() - 1) sb.append(",");
        }
        sb.append("],\"matrix\":[");
        for (int i = 0; i < candidates.size(); i++) {
            sb.append("[");
            for (int j = 0; j < jobs.size(); j++) {
                MatchResult mr = WeightedMatcher.computeMatch(candidates.get(i), jobs.get(j));
                sb.append(mr.getTotalScore());
                if (j < jobs.size() - 1) sb.append(",");
            }
            sb.append("]");
            if (i < candidates.size() - 1) sb.append(",");
        }
        sb.append("]}");

        sendJsonResponse(exchange, 200, sb.toString());
    }

    private void handleAssignments(HttpExchange exchange) throws IOException {
        List<Candidate> candidates = dataStore.getAllCandidates();
        List<Job> jobs = dataStore.getAllJobs();

        HungarianAlgorithm.AssignmentResult result = HungarianAlgorithm.computeOptimalAssignment(candidates, jobs);
        sendJsonResponse(exchange, 200, result.toJson());
    }

    private void handleOptimizeTeam(HttpExchange exchange, String method) throws IOException {
        if (!method.equals("POST")) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = readBody(exchange);
        List<String> targetSkills = extractList(body, "skills");
        if (targetSkills.isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\":\"No skills provided for team optimization\"}");
            return;
        }

        List<Candidate> pool = dataStore.getAllCandidates();
        GreedySetCover.TeamOptimizationResult result = GreedySetCover.optimizeTeam(pool, targetSkills);
        sendJsonResponse(exchange, 200, result.toJson());
    }

    private void handleAnalytics(HttpExchange exchange) throws IOException {
        List<Candidate> candidates = dataStore.getAllCandidates();
        List<Job> jobs = dataStore.getAllJobs();

        int totalCandidates = candidates.size();
        int totalJobs = jobs.size();

        // Compute average match across all pairs
        double sumMatch = 0.0;
        int pairCount = 0;
        for (Candidate c : candidates) {
            for (Job j : jobs) {
                MatchResult mr = WeightedMatcher.computeMatch(c, j);
                sumMatch += mr.getTotalScore();
                pairCount++;
            }
        }
        double avgMatch = pairCount > 0 ? (sumMatch / pairCount) : 0.0;

        // Skill coverage: Percentage of unique job required skills covered by candidates
        Set<String> allJobSkills = new HashSet<>();
        for (Job j : jobs) {
            if (j.getRequiredSkills() != null) allJobSkills.addAll(j.getRequiredSkills());
        }
        Set<String> allCandSkills = new HashSet<>();
        for (Candidate c : candidates) {
            if (c.getSkills() != null) allCandSkills.addAll(c.getSkills());
        }
        int coveredJobSkills = 0;
        for (String req : allJobSkills) {
            if (allCandSkills.contains(req)) coveredJobSkills++;
        }
        double skillCoverage = allJobSkills.isEmpty() ? 100.0 : ((double) coveredJobSkills / allJobSkills.size()) * 100.0;

        // Skill frequency map (top skills)
        Map<String, Integer> freq = skillIndexer.getSkillFrequencyMap();
        List<Map.Entry<String, Integer>> sortedSkills = new ArrayList<>(freq.entrySet());
        sortedSkills.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"totalCandidates\":").append(totalCandidates).append(",");
        sb.append("\"totalJobs\":").append(totalJobs).append(",");
        sb.append("\"averageMatch\":").append(Math.round(avgMatch * 10.0) / 10.0).append(",");
        sb.append("\"skillCoverage\":").append(Math.round(skillCoverage * 10.0) / 10.0).append(",");
        sb.append("\"uniqueSkillsCount\":").append(freq.size()).append(",");

        sb.append("\"topSkills\":[");
        int count = Math.min(10, sortedSkills.size());
        for (int i = 0; i < count; i++) {
            Map.Entry<String, Integer> entry = sortedSkills.get(i);
            sb.append("{\"skill\":\"").append(escape(entry.getKey()))
              .append("\",\"count\":").append(entry.getValue()).append("}");
            if (i < count - 1) sb.append(",");
        }
        sb.append("],");

        // Availability breakdown
        Map<String, Integer> availMap = new HashMap<>();
        for (Candidate c : candidates) {
            availMap.put(c.getAvailability(), availMap.getOrDefault(c.getAvailability(), 0) + 1);
        }
        sb.append("\"availability\":[");
        int ai = 0;
        for (Map.Entry<String, Integer> e : availMap.entrySet()) {
            sb.append("{\"status\":\"").append(escape(e.getKey())).append("\",\"count\":").append(e.getValue()).append("}");
            if (++ai < availMap.size()) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        sendJsonResponse(exchange, 200, sb.toString());
    }

    private void handleNormalize(HttpExchange exchange, String method) throws IOException {
        if (!method.equals("POST")) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = readBody(exchange);
        String raw = extractString(body, "skill");
        LevenshteinDistance.NormalizationResult nr = LevenshteinDistance.normalize(raw);
        int damerauDist = LevenshteinDistance.computeDamerauLevenshtein(nr.rawSkill, nr.canonicalSkill);

        String json = String.format(
            "{\"rawSkill\":\"%s\",\"canonicalSkill\":\"%s\",\"editDistance\":%d,\"damerauDistance\":%d,\"wasNormalized\":%b,\"method\":\"%s\"}",
            escape(nr.rawSkill), escape(nr.canonicalSkill), nr.editDistance, damerauDist, nr.wasNormalized, nr.method
        );
        sendJsonResponse(exchange, 200, json);
    }

    private void handleSkillIndex(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 200, skillIndexer.toJson());
    }

    private void handleReset(HttpExchange exchange, String method) throws IOException {
        if (!method.equals("POST")) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        dataStore.resetToSampleData();
        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Data reset to original sample state successfully\"}");
    }

    private void handleStringSearch(HttpExchange exchange, String method) throws IOException {
        if (!method.equals("POST")) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = readBody(exchange);
        String text = extractString(body, "text");
        String pattern = extractString(body, "pattern");

        if (text.isEmpty()) text = "Experienced Java backend engineer with Spring Boot, SQL, DSA, Docker, and Kubernetes.";
        if (pattern.isEmpty()) pattern = "Java";

        StringAlgorithms.KmpResult kmp = StringAlgorithms.kmpSearch(text, pattern);
        StringAlgorithms.RabinKarpResult rk = StringAlgorithms.rabinKarpSearch(text, pattern);
        List<Integer> zMatches = StringAlgorithms.zSearch(text, pattern);
        StringAlgorithms.SuffixArrayResult sa = StringAlgorithms.buildSuffixArrayAndLCP(text);

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"pattern\":\"").append(escape(pattern)).append("\",");
        sb.append("\"textLength\":").append(text.length()).append(",");

        // KMP
        sb.append("\"kmp\":{");
        sb.append("\"comparisons\":").append(kmp.comparisons).append(",");
        sb.append("\"matchIndices\":[");
        for (int i = 0; i < kmp.matchIndices.size(); i++) {
            sb.append(kmp.matchIndices.get(i));
            if (i < kmp.matchIndices.size() - 1) sb.append(",");
        }
        sb.append("],\"piTable\":[");
        for (int i = 0; i < kmp.piTable.length; i++) {
            sb.append(kmp.piTable[i]);
            if (i < kmp.piTable.length - 1) sb.append(",");
        }
        sb.append("]},");

        // Rabin-Karp
        sb.append("\"rabinKarp\":{");
        sb.append("\"comparisons\":").append(rk.comparisons).append(",");
        sb.append("\"hashCollisions\":").append(rk.hashCollisions).append(",");
        sb.append("\"patternHash\":").append(rk.patternHash).append(",");
        sb.append("\"matchIndices\":[");
        for (int i = 0; i < rk.matchIndices.size(); i++) {
            sb.append(rk.matchIndices.get(i));
            if (i < rk.matchIndices.size() - 1) sb.append(",");
        }
        sb.append("]},");

        // Z-Algorithm
        sb.append("\"zMatches\":[");
        for (int i = 0; i < zMatches.size(); i++) {
            sb.append(zMatches.get(i));
            if (i < zMatches.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Suffix Array LRS
        sb.append("\"longestRepeatedPhrase\":\"").append(escape(sa.longestRepeatedSubstring)).append("\"");
        sb.append("}");

        sendJsonResponse(exchange, 200, sb.toString());
    }

    private void handleNetworkFlow(HttpExchange exchange) throws IOException {
        List<Candidate> candidates = dataStore.getAllCandidates();
        List<Job> jobs = dataStore.getAllJobs();

        NetworkFlowEngine.FlowResult flowResult = NetworkFlowEngine.computeMaxFlowMatching(candidates, jobs, 50.0);
        sendJsonResponse(exchange, 200, flowResult.toJson());
    }

    private void handleBitmaskTeam(HttpExchange exchange, String method) throws IOException {
        if (!method.equals("POST")) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = readBody(exchange);
        List<String> targetSkills = extractList(body, "skills");
        if (targetSkills.isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\":\"No skills provided for exact bitmask optimization\"}");
            return;
        }

        List<Candidate> pool = dataStore.getAllCandidates();
        BitmaskTeamOptimizer.BitmaskResult bitmaskResult = BitmaskTeamOptimizer.optimizeTeamExact(pool, targetSkills);
        sendJsonResponse(exchange, 200, bitmaskResult.toJson());
    }

    private void handleParallelBenchmark(HttpExchange exchange) throws IOException {
        List<Candidate> candidates = dataStore.getAllCandidates();
        List<Job> jobs = dataStore.getAllJobs();

        ParallelAnalytics.BenchmarkResult benchmark = ParallelAnalytics.runBenchmark(candidates, jobs);
        sendJsonResponse(exchange, 200, benchmark.toJson());
    }

    // Helper utilities for request body and parameters
    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getQueryParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2 && parts[0].equalsIgnoreCase(param)) {
                return parts[1];
            }
        }
        return null;
    }

    private String extractString(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";
        int start = idx + pattern.length();
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) start++;

        if (start < json.length() && json.charAt(start) == '"') {
            int end = json.indexOf("\"", start + 1);
            return (end != -1) ? json.substring(start + 1, end) : "";
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') end++;
            return json.substring(start, end).trim();
        }
    }

    private List<String> extractList(String json, String key) {
        List<String> list = new ArrayList<>();
        String[] patterns = {
            "\"" + key + "\":[",
            "\"" + key + "\": [",
            key + ":[",
            key + ": ["
        };
        int idx = -1;
        int matchedLen = 0;
        for (String pat : patterns) {
            idx = json.indexOf(pat);
            if (idx != -1) {
                matchedLen = pat.length();
                break;
            }
        }
        if (idx == -1) return list;

        int arrStart = idx + matchedLen;
        int arrEnd = json.indexOf("]", arrStart);
        if (arrEnd == -1) return list;

        String inside = json.substring(arrStart, arrEnd);
        String[] parts = inside.split(",");
        for (String p : parts) {
            String item = p.trim().replace("\"", "");
            if (!item.isEmpty()) list.add(item);
        }
        return list;
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
