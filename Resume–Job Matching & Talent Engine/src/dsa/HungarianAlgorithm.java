package dsa;

import model.Candidate;
import model.Job;
import model.MatchResult;

import java.util.*;

/**
 * Hungarian Algorithm (Kuhn-Munkres Algorithm)
 * Solves the Bipartite Weighted Maximum Matching problem in polynomial time O(N^3).
 *
 * In this project:
 * - We have N Candidates and M Jobs.
 * - Compatibility score between candidate i and job j is Score(i, j) in [0, 100].
 * - Cost is defined as Cost(i, j) = 100 - Score(i, j) (skill gap to minimize).
 * - Hungarian algorithm finds the bijective assignment that minimizes total cost
 *   (i.e., maximizes global matching compatibility).
 *
 * Time Complexity: O(K^3) where K = max(N, M).
 * Space Complexity: O(K^2) for the cost matrix.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class HungarianAlgorithm {

    public static class AssignmentResult {
        public List<MatchResult> assignments;
        public List<Candidate> unassignedCandidates;
        public List<Job> unfilledJobs;
        public double totalScore;
        public double averageScore;
        public double totalSkillGap;
        public double[][] costMatrix;
        public double[][] scoreMatrix;
        public List<String> candidateNames;
        public List<String> jobTitles;

        public AssignmentResult() {
            this.assignments = new ArrayList<>();
            this.unassignedCandidates = new ArrayList<>();
            this.unfilledJobs = new ArrayList<>();
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"totalScore\":").append(Math.round(totalScore * 10.0) / 10.0).append(",");
            sb.append("\"averageScore\":").append(Math.round(averageScore * 10.0) / 10.0).append(",");
            sb.append("\"totalSkillGap\":").append(Math.round(totalSkillGap * 10.0) / 10.0).append(",");

            // Assignments array
            sb.append("\"assignments\":[");
            for (int i = 0; i < assignments.size(); i++) {
                sb.append(assignments.get(i).toJson());
                if (i < assignments.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Unassigned candidates
            sb.append("\"unassignedCandidates\":[");
            for (int i = 0; i < unassignedCandidates.size(); i++) {
                sb.append(unassignedCandidates.get(i).toJson());
                if (i < unassignedCandidates.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Unfilled jobs
            sb.append("\"unfilledJobs\":[");
            for (int i = 0; i < unfilledJobs.size(); i++) {
                sb.append(unfilledJobs.get(i).toJson());
                if (i < unfilledJobs.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Candidate names
            sb.append("\"candidateNames\":[");
            for (int i = 0; i < candidateNames.size(); i++) {
                sb.append("\"").append(escape(candidateNames.get(i))).append("\"");
                if (i < candidateNames.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Job titles
            sb.append("\"jobTitles\":[");
            for (int i = 0; i < jobTitles.size(); i++) {
                sb.append("\"").append(escape(jobTitles.get(i))).append("\"");
                if (i < jobTitles.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Score matrix
            sb.append("\"scoreMatrix\":[");
            for (int i = 0; i < scoreMatrix.length; i++) {
                sb.append("[");
                for (int j = 0; j < scoreMatrix[i].length; j++) {
                    sb.append(Math.round(scoreMatrix[i][j] * 10.0) / 10.0);
                    if (j < scoreMatrix[i].length - 1) sb.append(",");
                }
                sb.append("]");
                if (i < scoreMatrix.length - 1) sb.append(",");
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
     * Solves the optimal candidate-to-job assignment using the Hungarian Algorithm.
     */
    public static AssignmentResult computeOptimalAssignment(List<Candidate> candidates, List<Job> jobs) {
        AssignmentResult result = new AssignmentResult();

        if (candidates == null || candidates.isEmpty() || jobs == null || jobs.isEmpty()) {
            return result;
        }

        int numCandidates = candidates.size();
        int numJobs = jobs.size();
        int dim = Math.max(numCandidates, numJobs);

        // Precompute compatibility score matrix and cost matrix
        double[][] scoreMatrix = new double[numCandidates][numJobs];
        double[][] costMatrix = new double[dim][dim];

        List<String> candNames = new ArrayList<>();
        for (Candidate c : candidates) candNames.add(c.getName());
        List<String> jTitles = new ArrayList<>();
        for (Job j : jobs) jTitles.add(j.getTitle());

        result.candidateNames = candNames;
        result.jobTitles = jTitles;

        // Fill cost matrix: Cost = 100 - Score (so higher score has lower cost)
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (i < numCandidates && j < numJobs) {
                    MatchResult mr = WeightedMatcher.computeMatch(candidates.get(i), jobs.get(j));
                    scoreMatrix[i][j] = mr.getTotalScore();
                    costMatrix[i][j] = 100.0 - mr.getTotalScore();
                } else {
                    // Dummy row or column: high cost to avoid preferential assignment
                    costMatrix[i][j] = 1000.0;
                }
            }
        }

        result.scoreMatrix = scoreMatrix;
        result.costMatrix = costMatrix;

        // Execute Kuhn-Munkres Algorithm on the cost matrix (1-indexed for dual potentials)
        int n = dim;
        double[] u = new double[n + 1];
        double[] v = new double[n + 1];
        int[] p = new int[n + 1]; // p[j] represents candidate assigned to job j
        int[] way = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            double[] minv = new double[n + 1];
            Arrays.fill(minv, Double.POSITIVE_INFINITY);
            boolean[] used = new boolean[n + 1];

            do {
                used[j0] = true;
                int i0 = p[j0];
                double delta = Double.POSITIVE_INFINITY;
                int j1 = 0;

                for (int j = 1; j <= n; j++) {
                    if (!used[j]) {
                        double cur = costMatrix[i0 - 1][j - 1] - u[i0] - v[j];
                        if (cur < minv[j]) {
                            minv[j] = cur;
                            way[j] = j0;
                        }
                        if (minv[j] < delta) {
                            delta = minv[j];
                            j1 = j;
                        }
                    }
                }

                for (int j = 0; j <= n; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minv[j] -= delta;
                    }
                }
                j0 = j1;
            } while (p[j0] != 0);

            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        // Extract assignments from p[]: p[j] is the candidate index for job j (1-indexed)
        int[] candidateMatch = new int[dim];
        Arrays.fill(candidateMatch, -1);
        for (int j = 1; j <= n; j++) {
            if (p[j] > 0) {
                candidateMatch[p[j] - 1] = j - 1;
            }
        }

        boolean[] jobAssigned = new boolean[numJobs];
        double totalScore = 0.0;

        for (int i = 0; i < numCandidates; i++) {
            int j = candidateMatch[i];
            if (j >= 0 && j < numJobs) {
                MatchResult mr = WeightedMatcher.computeMatch(candidates.get(i), jobs.get(j));
                result.assignments.add(mr);
                jobAssigned[j] = true;
                totalScore += mr.getTotalScore();
            } else {
                result.unassignedCandidates.add(candidates.get(i));
            }
        }

        for (int j = 0; j < numJobs; j++) {
            if (!jobAssigned[j]) {
                result.unfilledJobs.add(jobs.get(j));
            }
        }

        result.totalScore = totalScore;
        result.averageScore = result.assignments.isEmpty() ? 0.0 : (totalScore / result.assignments.size());
        result.totalSkillGap = (result.assignments.size() * 100.0) - totalScore;

        return result;
    }
}
