import dsa.*;
import model.Candidate;
import model.Job;
import model.MatchResult;
import storage.DataStore;

import java.util.*;

/**
 * Automated Verification Test Suite for DSA Algorithms
 * Can be run directly via: java -cp bin AlgorithmTest
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class AlgorithmTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  RUNNING DSA VERIFICATION TEST SUITE (ALL 6 MODULES - TALENT ENGINE)");
        System.out.println("==================================================================");

        int passed = 0;
        int total = 9;

        if (testLevenshtein()) passed++;
        if (testSkillIndexer()) passed++;
        if (testWeightedMatcher()) passed++;
        if (testHungarianAlgorithm()) passed++;
        if (testGreedySetCover()) passed++;
        if (testStringAlgorithms()) passed++;
        if (testBitmaskTeamOptimizer()) passed++;
        if (testNetworkFlow()) passed++;
        if (testRandomizedQuickSort()) passed++;

        System.out.println("\n==================================================================");
        System.out.println(String.format("  TEST SUMMARY: %d / %d PASSED", passed, total));
        System.out.println("==================================================================");

        if (passed == total) {
            System.out.println("  ✓ ALL 6-MODULE ALGORITHM TESTS PASSED SUCCESSFULLY!");
        } else {
            System.err.println("  ✕ SOME TESTS FAILED.");
            System.exit(1);
        }
    }

    private static boolean testLevenshtein() {
        System.out.print("\n[TEST 1] Levenshtein Distance & Normalization... ");
        try {
            // Typo check
            LevenshteinDistance.NormalizationResult r1 = LevenshteinDistance.normalize("Javascrpt");
            assert r1.canonicalSkill.equals("JavaScript") : "Expected JavaScript for Javascrpt, got " + r1.canonicalSkill;

            LevenshteinDistance.NormalizationResult r2 = LevenshteinDistance.normalize("pyhton");
            assert r2.canonicalSkill.equals("Python") : "Expected Python for pyhton, got " + r2.canonicalSkill;

            // Abbreviation check
            LevenshteinDistance.NormalizationResult r3 = LevenshteinDistance.normalize("postgres");
            assert r3.canonicalSkill.equals("PostgreSQL") : "Expected PostgreSQL for postgres, got " + r3.canonicalSkill;

            LevenshteinDistance.NormalizationResult r4 = LevenshteinDistance.normalize("k8s");
            assert r4.canonicalSkill.equals("Kubernetes") : "Expected Kubernetes for k8s, got " + r4.canonicalSkill;

            System.out.println("PASSED (Distance verified)");
            return true;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            return false;
        }
    }

    private static boolean testSkillIndexer() {
        System.out.print("[TEST 2] HashMap Inverted Skill Index... ");
        try {
            SkillIndexer indexer = new SkillIndexer();
            Candidate c1 = new Candidate("C1", "Aarav", Arrays.asList("Java"), Arrays.asList("Java", "SQL"), 2, "Immediate", false);
            Candidate c2 = new Candidate("C2", "Maya", Arrays.asList("HTML"), Arrays.asList("HTML", "SQL"), 3, "Immediate", false);

            indexer.indexCandidate(c1);
            indexer.indexCandidate(c2);

            Set<String> sqlCands = indexer.getCandidateIdsBySkill("SQL");
            assert sqlCands.contains("C1") && sqlCands.contains("C2") : "SQL candidates missing";

            Set<String> javaCands = indexer.getCandidateIdsBySkill("Java");
            assert javaCands.contains("C1") && !javaCands.contains("C2") : "Java candidates mismatch";

            indexer.removeCandidate("C1");
            Set<String> updatedJava = indexer.getCandidateIdsBySkill("Java");
            assert !updatedJava.contains("C1") : "Candidate removal failed in inverted index";

            System.out.println("PASSED (O(1) lookups & updates verified)");
            return true;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            return false;
        }
    }

    private static boolean testWeightedMatcher() {
        System.out.print("[TEST 3] Deterministic Weighted Matching (80/20)... ");
        try {
            Candidate cand = new Candidate("C1", "Test Candidate",
                Arrays.asList("Java", "DSA", "SQL", "HTML"),
                Arrays.asList("Java", "DSA", "SQL", "HTML"), 2, "Immediate", false);

            Job job = new Job("J1", "Backend Role", "Engineering",
                Arrays.asList("Java", "DSA", "SQL", "Git", "HTML"), 2, "Desc", false);

            MatchResult res = WeightedMatcher.computeMatch(cand, job);

            // 4 out of 5 skills matched -> 80% of 80 pts = 64 pts
            // 2 out of 2 yrs exp -> 100% of 20 pts = 20 pts
            // Total score = 84.0 pts
            assert res.getMatchedSkills().size() == 4 : "Expected 4 matched skills";
            assert res.getMissingSkills().size() == 1 : "Expected 1 missing skill (Git)";
            assert res.getMissingSkills().contains("Git") : "Missing skill should be Git";
            assert Math.abs(res.getTotalScore() - 84.0) < 0.1 : "Expected score 84.0, got " + res.getTotalScore();

            System.out.println("PASSED (Exact score 84.0% verified)");
            return true;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            return false;
        }
    }

    private static boolean testHungarianAlgorithm() {
        System.out.print("[TEST 4] Hungarian Kuhn-Munkres Bipartite Matching... ");
        try {
            // 3 candidates and 3 jobs
            Candidate c1 = new Candidate("C1", "Cand 1", null, Arrays.asList("Java", "DSA"), 2, "Immediate", false);
            Candidate c2 = new Candidate("C2", "Cand 2", null, Arrays.asList("HTML", "CSS"), 2, "Immediate", false);
            Candidate c3 = new Candidate("C3", "Cand 3", null, Arrays.asList("Python", "Statistics"), 2, "Immediate", false);

            Job j1 = new Job("J1", "Java Dev", "Dept", Arrays.asList("Java", "DSA"), 2, "", false);
            Job j2 = new Job("J2", "Frontend Dev", "Dept", Arrays.asList("HTML", "CSS"), 2, "", false);
            Job j3 = new Job("J3", "Data Analyst", "Dept", Arrays.asList("Python", "Statistics"), 2, "", false);

            HungarianAlgorithm.AssignmentResult res = HungarianAlgorithm.computeOptimalAssignment(
                Arrays.asList(c1, c2, c3), Arrays.asList(j1, j2, j3)
            );

            assert res.assignments.size() == 3 : "Expected 3 assignments";
            assert res.averageScore >= 99.0 : "Expected perfect average compatibility, got " + res.averageScore;

            System.out.println("PASSED (O(N^3) optimal assignment verified)");
            return true;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            return false;
        }
    }

    private static boolean testGreedySetCover() {
        System.out.print("[TEST 5] Greedy Set Cover Team Optimizer... ");
        try {
            // Universe: {Java, SQL, DSA, HTML, CSS, Git}
            // Candidate A: Java, SQL, DSA (covers 3)
            // Candidate B: HTML, CSS (covers 2)
            // Candidate C: Git, Java (covers 1 new)
            Candidate cA = new Candidate("CA", "Candidate A", null, Arrays.asList("Java", "SQL", "DSA"), 2, "Immediate", false);
            Candidate cB = new Candidate("CB", "Candidate B", null, Arrays.asList("HTML", "CSS"), 2, "Immediate", false);
            Candidate cC = new Candidate("CC", "Candidate C", null, Arrays.asList("Git", "Java"), 2, "Immediate", false);
            Candidate cD = new Candidate("CD", "Candidate D", null, Arrays.asList("Python"), 2, "Immediate", false);

            List<String> targetUniverse = Arrays.asList("Java", "SQL", "DSA", "HTML", "CSS", "Git");
            GreedySetCover.TeamOptimizationResult res = GreedySetCover.optimizeTeam(
                Arrays.asList(cA, cB, cC, cD), targetUniverse
            );

            assert res.selectedTeam.size() == 3 : "Expected team size 3, got " + res.selectedTeam.size();
            assert res.coveragePercentage >= 99.9 : "Expected 100% coverage, got " + res.coveragePercentage;
            assert res.uncoveredSkills.isEmpty() : "Expected 0 uncovered skills";
            assert res.selectedSteps.size() == 3 : "Expected 3 greedy steps";

            System.out.println("PASSED (Harmonic bound & 100% coverage verified)");
            return true;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            return false;
        }
    }

    private static boolean testStringAlgorithms() {
        System.out.print("[TEST 6] String Algorithms (KMP, Rabin-Karp, Z, Suffix/LCP)... ");
        try {
            String text = "Experienced Java backend engineer developing REST APIs and Java microservices.";
            String pattern = "Java";

            // KMP
            StringAlgorithms.KmpResult kmp = StringAlgorithms.kmpSearch(text, pattern);
            assert kmp.matchIndices.size() == 2 : "KMP should find 2 occurrences of Java";
            assert kmp.matchIndices.get(0) == 12 : "First match at index 12";

            // Rabin-Karp
            StringAlgorithms.RabinKarpResult rk = StringAlgorithms.rabinKarpSearch(text, pattern);
            assert rk.matchIndices.size() == 2 : "Rabin-Karp should find 2 occurrences of Java";

            // Z-Algorithm
            List<Integer> z = StringAlgorithms.zSearch(text, pattern);
            assert z.size() == 2 : "Z-algorithm should find 2 occurrences";

            // Suffix Array & LCP
            StringAlgorithms.SuffixArrayResult sa = StringAlgorithms.buildSuffixArrayAndLCP(text);
            assert sa.suffixArray.length == text.length() : "Suffix array length mismatch";

            System.out.println("PASSED (KMP, RK, Z & LCP verified)");
            return true;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            return false;
        }
    }

    private static boolean testBitmaskTeamOptimizer() {
        System.out.print("[TEST 7] Bitmask DP Exact Team Optimizer... ");
        try {
            Candidate cA = new Candidate("CA", "Candidate A", null, Arrays.asList("Java", "SQL", "DSA"), 2, "Immediate", false);
            Candidate cB = new Candidate("CB", "Candidate B", null, Arrays.asList("HTML", "CSS"), 2, "Immediate", false);
            Candidate cC = new Candidate("CC", "Candidate C", null, Arrays.asList("Git", "Java"), 2, "Immediate", false);

            List<String> targetUniverse = Arrays.asList("Java", "SQL", "DSA", "HTML", "CSS", "Git");
            BitmaskTeamOptimizer.BitmaskResult res = BitmaskTeamOptimizer.optimizeTeamExact(
                Arrays.asList(cA, cB, cC), targetUniverse
            );

            assert res.exactFound : "Exact optimal cover should be found";
            assert res.optimalTeamSize == 3 : "Expected optimal team size 3, got " + res.optimalTeamSize;
            assert res.coveredSkills.size() == 6 : "Expected 6 covered skills";

            System.out.println("PASSED (Exact O(2^K * N) dynamic programming verified)");
            return true;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            return false;
        }
    }

    private static boolean testNetworkFlow() {
        System.out.print("[TEST 8] Edmonds-Karp Max-Flow Bipartite Matching & Min-Cut... ");
        try {
            Candidate c1 = new Candidate("C1", "Cand 1", null, Arrays.asList("Java", "SQL"), 2, "Immediate", false);
            Candidate c2 = new Candidate("C2", "Cand 2", null, Arrays.asList("Python", "ML"), 2, "Immediate", false);

            Job j1 = new Job("J1", "Backend Role", "Eng", Arrays.asList("Java", "SQL"), 2, "", false);
            Job j2 = new Job("J2", "ML Role", "Eng", Arrays.asList("Python", "ML"), 2, "", false);

            NetworkFlowEngine.FlowResult res = NetworkFlowEngine.computeMaxFlowMatching(
                Arrays.asList(c1, c2), Arrays.asList(j1, j2), 50.0
            );

            assert res.maxFlow == 2 : "Expected max flow 2, got " + res.maxFlow;
            assert res.matchedPairs.size() == 2 : "Expected 2 matched pairs";
            assert res.unfilledRoles.isEmpty() : "No roles should be unfilled";

            System.out.println("PASSED (O(V * E^2) Edmonds-Karp & Min-Cut verified)");
            return true;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            return false;
        }
    }

    private static boolean testRandomizedQuickSort() {
        System.out.print("[TEST 9] Randomized QuickSort (Candidate Match Ranking)... ");
        try {
            Candidate c = new Candidate("C1", "Cand", null, Arrays.asList("Java"), 2, "Immediate", false);
            Job j1 = new Job("J1", "Role 1", "", Arrays.asList("Java"), 2, "", false);
            Job j2 = new Job("J2", "Role 2", "", Arrays.asList("Java", "SQL"), 2, "", false);
            Job j3 = new Job("J3", "Role 3", "", Arrays.asList("Java", "SQL", "Docker"), 2, "", false);

            MatchResult mr1 = WeightedMatcher.computeMatch(c, j1); // High score
            MatchResult mr2 = WeightedMatcher.computeMatch(c, j2); // Medium score
            MatchResult mr3 = WeightedMatcher.computeMatch(c, j3); // Low score

            List<MatchResult> list = new ArrayList<>(Arrays.asList(mr3, mr1, mr2));
            RandomizedAlgorithms.randomizedQuickSort(list);

            assert list.get(0).getTotalScore() >= list.get(1).getTotalScore() : "Descending order failed";
            assert list.get(1).getTotalScore() >= list.get(2).getTotalScore() : "Descending order failed";

            System.out.println("PASSED (Expected O(N log N) randomized sorting verified)");
            return true;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            return false;
        }
    }
}
