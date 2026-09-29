package dsa;

import model.Candidate;

import java.util.*;

/**
 * Module 3: Advanced Dynamic Programming - Bitmask DP Team Optimizer
 * Computes the MATHEMATICALLY EXACT minimum candidate team covering target skills.
 *
 * State Representation:
 * - A bitmask of length K represents the covered skill subset (bit i = 1 if skill i is covered).
 * - dp[mask] = minimum number of candidates required to achieve skill coverage 'mask'.
 *
 * Recurrence:
 * dp[mask | candMask] = min(dp[mask | candMask], dp[mask] + 1)
 *
 * Complexity:
 * - Time Complexity: O(2^K * N) where K is number of required skills, N is candidate pool size.
 * - Space Complexity: O(2^K) for DP state and parent tracking tables.
 *
 * Intractability Note:
 * When K > 18, 2^K exceeds 262,144 states. This proves why Minimum Set Cover is NP-Hard
 * and why Greedy Approximation (Module 5) is required for large universes.
 *
 * Course: DSA-3 (25CS2103E) - Talent Engine
 */
public class BitmaskTeamOptimizer {

    public static class BitmaskResult {
        public boolean exactFound;
        public int optimalTeamSize;
        public List<Candidate> selectedTeam;
        public List<String> coveredSkills;
        public List<String> uncoveredSkills;
        public int totalStatesEvaluated;
        public long executionTimeNanos;
        public String algorithmName = "Exact Bitmask Dynamic Programming";
        public String complexity = "O(2^K * N)";

        public BitmaskResult() {
            this.selectedTeam = new ArrayList<>();
            this.coveredSkills = new ArrayList<>();
            this.uncoveredSkills = new ArrayList<>();
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"exactFound\":").append(exactFound).append(",");
            sb.append("\"optimalTeamSize\":").append(optimalTeamSize).append(",");
            sb.append("\"totalStatesEvaluated\":").append(totalStatesEvaluated).append(",");
            sb.append("\"executionTimeMs\":").append(Math.round(executionTimeNanos / 10000.0) / 100.0).append(",");
            sb.append("\"algorithm\":\"").append(algorithmName).append("\",");
            sb.append("\"complexity\":\"").append(complexity).append("\",");

            sb.append("\"selectedTeam\":[");
            for (int i = 0; i < selectedTeam.size(); i++) {
                sb.append(selectedTeam.get(i).toJson());
                if (i < selectedTeam.size() - 1) sb.append(",");
            }
            sb.append("],");

            sb.append("\"coveredSkills\":[");
            for (int i = 0; i < coveredSkills.size(); i++) {
                sb.append("\"").append(escape(coveredSkills.get(i))).append("\"");
                if (i < coveredSkills.size() - 1) sb.append(",");
            }
            sb.append("],");

            sb.append("\"uncoveredSkills\":[");
            for (int i = 0; i < uncoveredSkills.size(); i++) {
                sb.append("\"").append(escape(uncoveredSkills.get(i))).append("\"");
                if (i < uncoveredSkills.size() - 1) sb.append(",");
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
     * Solves minimum set cover exactly using Bitmask DP for K <= 18 skills.
     */
    public static BitmaskResult optimizeTeamExact(List<Candidate> candidates, List<String> targetSkills) {
        BitmaskResult result = new BitmaskResult();
        long startTime = System.nanoTime();

        if (targetSkills == null || targetSkills.isEmpty() || candidates == null || candidates.isEmpty()) {
            result.executionTimeNanos = System.nanoTime() - startTime;
            return result;
        }

        // Filter and deduplicate skills
        List<String> distinctSkills = new ArrayList<>(new LinkedHashSet<>(targetSkills));
        int k = distinctSkills.size();

        // Safety cap: K <= 18 (2^18 = 262,144 states)
        if (k > 18) {
            distinctSkills = distinctSkills.subList(0, 18);
            k = 18;
        }

        Map<String, Integer> skillIndexMap = new HashMap<>();
        for (int i = 0; i < k; i++) {
            skillIndexMap.put(distinctSkills.get(i).toLowerCase(), i);
        }

        // Compute bitmask for each candidate
        int n = candidates.size();
        int[] candMasks = new int[n];
        for (int i = 0; i < n; i++) {
            int mask = 0;
            List<String> cSkills = candidates.get(i).getSkills();
            if (cSkills != null) {
                for (String s : cSkills) {
                    Integer idx = skillIndexMap.get(s.toLowerCase());
                    if (idx != null) {
                        mask |= (1 << idx);
                    }
                }
            }
            candMasks[i] = mask;
        }

        int targetMask = (1 << k) - 1;
        int maxStates = 1 << k;
        int[] dp = new int[maxStates];
        int[] parentMask = new int[maxStates];
        int[] parentCand = new int[maxStates];

        Arrays.fill(dp, Integer.MAX_VALUE / 2);
        Arrays.fill(parentMask, -1);
        Arrays.fill(parentCand, -1);

        dp[0] = 0;
        int statesVisited = 0;

        for (int mask = 0; mask < maxStates; mask++) {
            if (dp[mask] >= Integer.MAX_VALUE / 2) continue;
            statesVisited++;

            for (int i = 0; i < n; i++) {
                int nextMask = mask | candMasks[i];
                if (dp[mask] + 1 < dp[nextMask]) {
                    dp[nextMask] = dp[mask] + 1;
                    parentMask[nextMask] = mask;
                    parentCand[nextMask] = i;
                }
            }
        }

        // Check if full targetMask is reached, or pick highest reachable mask
        int bestMask = targetMask;
        if (dp[targetMask] >= Integer.MAX_VALUE / 2) {
            int maxBits = -1;
            bestMask = 0;
            for (int m = 0; m < maxStates; m++) {
                if (dp[m] < Integer.MAX_VALUE / 2) {
                    int bitCount = Integer.bitCount(m);
                    if (bitCount > maxBits) {
                        maxBits = bitCount;
                        bestMask = m;
                    }
                }
            }
            result.exactFound = false;
        } else {
            result.exactFound = true;
        }

        result.optimalTeamSize = dp[bestMask];
        result.totalStatesEvaluated = statesVisited;

        // Reconstruct selected candidates via parent pointers
        int curr = bestMask;
        while (curr > 0 && parentCand[curr] != -1) {
            int candIdx = parentCand[curr];
            result.selectedTeam.add(candidates.get(candIdx));
            curr = parentMask[curr];
        }
        Collections.reverse(result.selectedTeam);

        // Track covered vs uncovered skills
        for (int i = 0; i < k; i++) {
            if ((bestMask & (1 << i)) != 0) {
                result.coveredSkills.add(distinctSkills.get(i));
            } else {
                result.uncoveredSkills.add(distinctSkills.get(i));
            }
        }

        result.executionTimeNanos = System.nanoTime() - startTime;
        return result;
    }
}
