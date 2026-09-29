package dsa;

import java.util.*;

/**
 * Levenshtein Distance Algorithm (Dynamic Programming)
 * Used to normalize raw skill strings from résumés against a canonical skill vocabulary.
 * Handles typographical errors, variations in capitalization, and common abbreviations.
 *
 * Time Complexity: O(M * N) where M and N are lengths of the two strings.
 * Space Complexity: O(M * N) for the 2D DP table.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class LevenshteinDistance {

    // Canonical dictionary of industry-standard tech skills
    public static final List<String> CANONICAL_SKILLS = Arrays.asList(
        "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "SQL",
        "PostgreSQL", "MySQL", "MongoDB", "Redis", "HTML", "CSS", "React",
        "Angular", "Vue", "Node.js", "Git", "Docker", "Kubernetes", "AWS",
        "Linux", "DSA", "Algorithms", "Machine Learning", "Data Analysis",
        "Statistics", "REST API", "Cybersecurity", "Spring Boot"
    );

    // Common abbreviations and aliases mapping directly to canonical skills
    private static final Map<String, String> ABBREVIATION_MAP = new HashMap<>();
    static {
        ABBREVIATION_MAP.put("js", "JavaScript");
        ABBREVIATION_MAP.put("ts", "TypeScript");
        ABBREVIATION_MAP.put("py", "Python");
        ABBREVIATION_MAP.put("cpp", "C++");
        ABBREVIATION_MAP.put("cplusplus", "C++");
        ABBREVIATION_MAP.put("csharp", "C#");
        ABBREVIATION_MAP.put("postgres", "PostgreSQL");
        ABBREVIATION_MAP.put("k8s", "Kubernetes");
        ABBREVIATION_MAP.put("ml", "Machine Learning");
        ABBREVIATION_MAP.put("algo", "Algorithms");
        ABBREVIATION_MAP.put("dsa", "DSA");
        ABBREVIATION_MAP.put("reactjs", "React");
        ABBREVIATION_MAP.put("nodejs", "Node.js");
    }

    /**
     * Computes the minimum edit distance between two strings using Dynamic Programming.
     * Allowed operations: Insertion (cost 1), Deletion (cost 1), Substitution (cost 1).
     */
    public static int compute(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;

        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();

        int m = s1.length();
        int n = s2.length();

        // dp[i][j] stores the edit distance between s1[0..i-1] and s2[0..j-1]
        int[][] dp = new int[m + 1][n + 1];

        // Base cases: transforming empty string to target string
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        // DP Table Fill
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    dp[i - 1][j] + 1,       // Deletion from s1
                    Math.min(
                        dp[i][j - 1] + 1,   // Insertion into s1
                        dp[i - 1][j - 1] + cost // Substitution
                    )
                );
            }
        }

        return dp[m][n];
    }

    /**
     * Computes Damerau-Levenshtein distance supporting 4 operations:
     * Insertion, Deletion, Substitution, and Adjacent Transposition.
     * Useful for typing mistakes like "teh" -> "the" or "jsavscript" -> "javascript".
     * Time Complexity: O(M * N)
     * Space Complexity: O(M * N)
     */
    public static int computeDamerauLevenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;

        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();

        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    dp[i - 1][j] + 1,
                    Math.min(
                        dp[i][j - 1] + 1,
                        dp[i - 1][j - 1] + cost
                    )
                );

                // Adjacent character transposition check
                if (i > 1 && j > 1 && s1.charAt(i - 1) == s2.charAt(j - 2) && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
                }
            }
        }

        return dp[m][n];
    }

    /**
     * Result of normalizing a raw skill name.
     */
    public static class NormalizationResult {
        public String rawSkill;
        public String canonicalSkill;
        public int editDistance;
        public boolean wasNormalized;
        public String method; // "EXACT", "ABBREVIATION", "LEVENSHTEIN", or "UNMODIFIED"

        public NormalizationResult(String raw, String canonical, int dist, boolean normalized, String method) {
            this.rawSkill = raw;
            this.canonicalSkill = canonical;
            this.editDistance = dist;
            this.wasNormalized = normalized;
            this.method = method;
        }
    }

    /**
     * Normalizes a raw skill input string to the best matching canonical skill.
     * Returns NormalizationResult detailing the match and edit distance.
     */
    public static NormalizationResult normalize(String rawSkill) {
        if (rawSkill == null || rawSkill.trim().isEmpty()) {
            return new NormalizationResult("", "", 0, false, "EMPTY");
        }

        String cleaned = rawSkill.trim();
        String lower = cleaned.toLowerCase();

        // 1. Direct case-insensitive match against canonical skills
        for (String canonical : CANONICAL_SKILLS) {
            if (canonical.equalsIgnoreCase(cleaned)) {
                return new NormalizationResult(cleaned, canonical, 0, !canonical.equals(cleaned), "EXACT");
            }
        }

        // 2. Direct match against common abbreviations
        if (ABBREVIATION_MAP.containsKey(lower)) {
            String canonical = ABBREVIATION_MAP.get(lower);
            return new NormalizationResult(cleaned, canonical, 0, true, "ABBREVIATION");
        }

        // 3. Find closest canonical skill using Levenshtein Distance (DP)
        int minDistance = Integer.MAX_VALUE;
        String bestCanonical = null;

        for (String canonical : CANONICAL_SKILLS) {
            int dist = compute(lower, canonical.toLowerCase());
            if (dist < minDistance) {
                minDistance = dist;
                bestCanonical = canonical;
            }
        }

        // Threshold rule: If edit distance <= 2 (or <= 3 for long words), accept canonical mapping
        int threshold = (bestCanonical != null && bestCanonical.length() > 6) ? 3 : 2;
        if (bestCanonical != null && minDistance <= threshold) {
            return new NormalizationResult(cleaned, bestCanonical, minDistance, true, "LEVENSHTEIN");
        }

        // 4. Fallback: Keep cleaned raw skill formatted with first letter capitalized
        String fallback = cleaned.substring(0, 1).toUpperCase() + (cleaned.length() > 1 ? cleaned.substring(1) : "");
        return new NormalizationResult(cleaned, fallback, 0, false, "UNMODIFIED");
    }

    /**
     * Normalizes a collection of raw skills and eliminates duplicates using a Set.
     */
    public static List<String> normalizeList(List<String> rawSkills) {
        Set<String> normalizedSet = new LinkedHashSet<>();
        if (rawSkills != null) {
            for (String raw : rawSkills) {
                NormalizationResult res = normalize(raw);
                if (!res.canonicalSkill.isEmpty()) {
                    normalizedSet.add(res.canonicalSkill);
                }
            }
        }
        return new ArrayList<>(normalizedSet);
    }
}
