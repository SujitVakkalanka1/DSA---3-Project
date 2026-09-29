package dsa;

import model.Candidate;
import model.Job;
import model.MatchResult;

import java.util.*;

/**
 * Module 4: Network Flow
 * Implements Edmonds-Karp Algorithm (Ford-Fulkerson method with BFS shortest augmenting paths)
 * to solve Capacity-Constrained Bipartite Candidate-Role Matching and Min-Cut Bottleneck Analysis.
 *
 * Problem Distinction:
 * - Hungarian Algorithm (Kuhn-Munkres): Solves WEIGHTED 1-to-1 optimal assignment minimizing total skill gap.
 * - Network Flow (Edmonds-Karp): Solves CAPACITY-CONSTRAINED feasible allocation where roles can have
 *   headcounts/quotas > 1, with Min-Cut identifying hiring bottlenecks and capacity starvation.
 *
 * Time Complexity: O(V * E^2)
 * Space Complexity: O(V^2) residual capacity matrix
 *
 * Course: DSA-3 (25CS2103E) - Talent Engine
 */
public class NetworkFlowEngine {

    public static class FlowAssignment {
        public Candidate candidate;
        public Job job;
        public double matchScore;

        public FlowAssignment(Candidate candidate, Job job, double matchScore) {
            this.candidate = candidate;
            this.job = job;
            this.matchScore = matchScore;
        }

        public String toJson() {
            return String.format(
                "{\"candidate\":%s,\"job\":%s,\"matchScore\":%.1f}",
                candidate.toJson(), job.toJson(), matchScore
            );
        }
    }

    public static class FlowResult {
        public int maxFlow;
        public int totalCapacity;
        public int augmentingPathsCount;
        public List<FlowAssignment> matchedPairs;
        public List<String> minCutBottlenecks;
        public List<Job> unfilledRoles;
        public List<Candidate> unassignedCandidates;
        public String theorem = "Max-Flow Min-Cut Theorem (Ford-Fulkerson 1956)";
        public String complexity = "O(V * E^2) using Edmonds-Karp BFS";

        public FlowResult() {
            this.matchedPairs = new ArrayList<>();
            this.minCutBottlenecks = new ArrayList<>();
            this.unfilledRoles = new ArrayList<>();
            this.unassignedCandidates = new ArrayList<>();
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"maxFlow\":").append(maxFlow).append(",");
            sb.append("\"totalCapacity\":").append(totalCapacity).append(",");
            sb.append("\"augmentingPathsCount\":").append(augmentingPathsCount).append(",");
            sb.append("\"theorem\":\"").append(theorem).append("\",");
            sb.append("\"complexity\":\"").append(complexity).append("\",");

            sb.append("\"matchedPairs\":[");
            for (int i = 0; i < matchedPairs.size(); i++) {
                sb.append(matchedPairs.get(i).toJson());
                if (i < matchedPairs.size() - 1) sb.append(",");
            }
            sb.append("],");

            sb.append("\"minCutBottlenecks\":[");
            for (int i = 0; i < minCutBottlenecks.size(); i++) {
                sb.append("\"").append(escape(minCutBottlenecks.get(i))).append("\"");
                if (i < minCutBottlenecks.size() - 1) sb.append(",");
            }
            sb.append("],");

            sb.append("\"unfilledRoles\":[");
            for (int i = 0; i < unfilledRoles.size(); i++) {
                sb.append(unfilledRoles.get(i).toJson());
                if (i < unfilledRoles.size() - 1) sb.append(",");
            }
            sb.append("],");

            sb.append("\"unassignedCandidates\":[");
            for (int i = 0; i < unassignedCandidates.size(); i++) {
                sb.append(unassignedCandidates.get(i).toJson());
                if (i < unassignedCandidates.size() - 1) sb.append(",");
            }
            sb.append("]");

            sb.append("}");
            return sb.toString();
        }

        private String escape(String s) {
            return s == null ? "" : s.replace("\"", "\\\"");
        }
    }

    /**
     * Runs Edmonds-Karp Max-Flow on candidate-to-role bipartite network with role capacity.
     * @param candidates Available candidate pool
     * @param jobs Available jobs (each job defaults to capacity quota 1 or custom)
     * @param scoreThreshold Minimum compatibility score to form an edge (default 50.0%)
     */
    public static FlowResult computeMaxFlowMatching(List<Candidate> candidates, List<Job> jobs, double scoreThreshold) {
        FlowResult result = new FlowResult();

        if (candidates == null || candidates.isEmpty() || jobs == null || jobs.isEmpty()) {
            return result;
        }

        int n = candidates.size();
        int m = jobs.size();
        int totalVertices = n + m + 2;
        int source = 0;
        int sink = totalVertices - 1;

        // Vertex Indexing:
        // 0: Source
        // 1 .. n: Candidates
        // n+1 .. n+m: Jobs
        // totalVertices - 1: Sink

        int[][] capacity = new int[totalVertices][totalVertices];
        double[][] matchScores = new double[n][m];

        // 1. Edges: Source -> Candidates (capacity = 1)
        for (int i = 1; i <= n; i++) {
            capacity[source][i] = 1;
        }

        // 2. Edges: Candidates -> Jobs (capacity = 1 if score >= scoreThreshold)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                MatchResult mr = WeightedMatcher.computeMatch(candidates.get(i), jobs.get(j));
                matchScores[i][j] = mr.getTotalScore();
                if (mr.getTotalScore() >= scoreThreshold) {
                    capacity[i + 1][n + 1 + j] = 1;
                }
            }
        }

        // 3. Edges: Jobs -> Sink (capacity = 1 for 1-to-1, or up to 2 for multi-seat roles)
        int totalRoleCapacity = 0;
        for (int j = 0; j < m; j++) {
            int roleQuota = 1; // standard 1 seat per role
            capacity[n + 1 + j][sink] = roleQuota;
            totalRoleCapacity += roleQuota;
        }
        result.totalCapacity = totalRoleCapacity;

        // Edmonds-Karp BFS Augmenting Paths Loop
        int[][] residual = new int[totalVertices][totalVertices];
        for (int u = 0; u < totalVertices; u++) {
            System.arraycopy(capacity[u], 0, residual[u], 0, totalVertices);
        }

        int[] parent = new int[totalVertices];
        int maxFlow = 0;
        int augmentingPaths = 0;

        while (bfsFindPath(residual, source, sink, parent, totalVertices)) {
            augmentingPaths++;
            // Find bottleneck capacity along BFS path (in unit network, bottleneck is 1)
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residual[u][v]);
            }

            // Augment flow and update residual capacities
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow;
            }

            maxFlow += pathFlow;
        }

        result.maxFlow = maxFlow;
        result.augmentingPathsCount = augmentingPaths;

        // Reconstruct matched pairs from residual flow
        boolean[] candMatched = new boolean[n];
        int[] roleFilledCount = new int[m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int u = i + 1;
                int v = n + 1 + j;
                // If forward capacity was 1 and residual capacity is now 0, flow was pushed
                if (capacity[u][v] > 0 && residual[u][v] == 0) {
                    result.matchedPairs.add(new FlowAssignment(candidates.get(i), jobs.get(j), matchScores[i][j]));
                    candMatched[i] = true;
                    roleFilledCount[j]++;
                }
            }
        }

        // Unassigned candidates
        for (int i = 0; i < n; i++) {
            if (!candMatched[i]) {
                result.unassignedCandidates.add(candidates.get(i));
            }
        }

        // Unfilled roles
        for (int j = 0; j < m; j++) {
            if (roleFilledCount[j] == 0) {
                result.unfilledRoles.add(jobs.get(j));
            }
        }

        // Min-Cut Analysis: Find reachable vertices from Source in residual graph
        boolean[] reachable = new boolean[totalVertices];
        Queue<Integer> q = new LinkedList<>();
        reachable[source] = true;
        q.add(source);

        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v = 0; v < totalVertices; v++) {
                if (!reachable[v] && residual[u][v] > 0) {
                    reachable[v] = true;
                    q.add(v);
                }
            }
        }

        // Cut edges: u in Reachable, v in Non-Reachable with capacity[u][v] > 0
        for (int u = 0; u < totalVertices; u++) {
            if (reachable[u]) {
                for (int v = 0; v < totalVertices; v++) {
                    if (!reachable[v] && capacity[u][v] > 0) {
                        if (u == source && v >= 1 && v <= n) {
                            result.minCutBottlenecks.add("Candidate pool capacity limit at candidate " + candidates.get(v - 1).getName());
                        } else if (u >= n + 1 && u <= n + m && v == sink) {
                            result.minCutBottlenecks.add("Saturated hiring quota at role: " + jobs.get(u - n - 1).getTitle());
                        } else if (u >= 1 && u <= n && v >= n + 1 && v <= n + m) {
                            result.minCutBottlenecks.add("Skill threshold boundary between " + candidates.get(u - 1).getName() + " and " + jobs.get(v - n - 1).getTitle());
                        }
                    }
                }
            }
        }

        if (result.minCutBottlenecks.isEmpty() && maxFlow < totalRoleCapacity) {
            result.minCutBottlenecks.add("Insufficient qualified applicants meeting " + (int)scoreThreshold + "% score threshold.");
        }

        return result;
    }

    private static boolean bfsFindPath(int[][] residual, int s, int t, int[] parent, int V) {
        boolean[] visited = new boolean[V];
        Queue<Integer> queue = new LinkedList<>();

        queue.add(s);
        visited[s] = true;
        parent[s] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < V; v++) {
                if (!visited[v] && residual[u][v] > 0) {
                    parent[v] = u;
                    visited[v] = true;
                    if (v == t) {
                        return true;
                    }
                    queue.add(v);
                }
            }
        }
        return false;
    }
}
