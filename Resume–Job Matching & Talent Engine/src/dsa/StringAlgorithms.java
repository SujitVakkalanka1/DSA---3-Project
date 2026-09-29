package dsa;

import java.util.*;

/**
 * Module 2: String Algorithms
 * Real-world string processing for résumé text parsing, skill phrase searching,
 * and repeated terminology extraction.
 *
 * Implements:
 * 1. Knuth-Morris-Pratt (KMP) Algorithm with Prefix Function (pi-table): O(N + M)
 * 2. Rabin-Karp Algorithm with Polynomial Rolling Hash: O(N + M) average
 * 3. Z-Algorithm with Z-array: O(N + M)
 * 4. Suffix Array & Kasai's Longest Common Prefix (LCP): O(N log N)
 *
 * Course: DSA-3 (25CS2103E) - Talent Engine
 */
public class StringAlgorithms {

    // =========================================================================
    // 1. KNUTH-MORRIS-PRATT (KMP) ALGORITHM
    // =========================================================================

    /**
     * Computes the KMP Prefix Function (pi table / failure table).
     * pi[i] is the length of the longest proper prefix of pattern[0..i]
     * that is also a suffix of pattern[0..i].
     * Time Complexity: O(M) where M = pattern.length()
     */
    public static int[] computePrefixFunction(String pattern) {
        int m = pattern.length();
        int[] pi = new int[m];
        int j = 0;

        for (int i = 1; i < m; i++) {
            while (j > 0 && Character.toLowerCase(pattern.charAt(i)) != Character.toLowerCase(pattern.charAt(j))) {
                j = pi[j - 1];
            }
            if (Character.toLowerCase(pattern.charAt(i)) == Character.toLowerCase(pattern.charAt(j))) {
                j++;
            }
            pi[i] = j;
        }
        return pi;
    }

    /**
     * KMP Search Result structure detailing match locations and shift efficiency.
     */
    public static class KmpResult {
        public String pattern;
        public String text;
        public List<Integer> matchIndices;
        public int comparisons;
        public int[] piTable;

        public KmpResult(String pattern, String text, List<Integer> matches, int comparisons, int[] pi) {
            this.pattern = pattern;
            this.text = text;
            this.matchIndices = matches;
            this.comparisons = comparisons;
            this.piTable = pi;
        }
    }

    /**
     * Finds all occurrences of pattern in text using KMP in O(N + M) time.
     * Avoids redundant character comparisons by shifting pattern according to pi[j-1].
     */
    public static KmpResult kmpSearch(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return new KmpResult(pattern, text, matches, 0, new int[0]);
        }

        int[] pi = computePrefixFunction(pattern);
        int n = text.length();
        int m = pattern.length();
        int j = 0;
        int comparisons = 0;

        for (int i = 0; i < n; i++) {
            comparisons++;
            while (j > 0 && Character.toLowerCase(text.charAt(i)) != Character.toLowerCase(pattern.charAt(j))) {
                j = pi[j - 1];
                comparisons++;
            }
            if (Character.toLowerCase(text.charAt(i)) == Character.toLowerCase(pattern.charAt(j))) {
                j++;
            }
            if (j == m) {
                matches.add(i - m + 1);
                j = pi[j - 1]; // Continue search for next occurrence
            }
        }

        return new KmpResult(pattern, text, matches, comparisons, pi);
    }

    // =========================================================================
    // 2. RABIN-KARP ALGORITHM (POLYNOMIAL ROLLING HASH)
    // =========================================================================

    public static class RabinKarpResult {
        public String pattern;
        public List<Integer> matchIndices;
        public int hashCollisions;
        public long patternHash;
        public int comparisons;

        public RabinKarpResult(String pattern, List<Integer> matches, int collisions, long hash, int comparisons) {
            this.pattern = pattern;
            this.matchIndices = matches;
            this.hashCollisions = collisions;
            this.patternHash = hash;
            this.comparisons = comparisons;
        }
    }

    /**
     * Searches for pattern in text using Rabin-Karp polynomial rolling hash.
     * Base = 257, Modulo = 1_000_000_007.
     * Time Complexity: O(N + M) average, O(N * M) worst-case (rare with large prime modulus).
     */
    public static RabinKarpResult rabinKarpSearch(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return new RabinKarpResult(pattern, matches, 0, 0L, 0);
        }

        final long BASE = 257;
        final long MOD = 1_000_000_007L;
        int n = text.length();
        int m = pattern.length();

        long patternHash = 0;
        long textHash = 0;
        long highestPower = 1;

        for (int i = 0; i < m - 1; i++) {
            highestPower = (highestPower * BASE) % MOD;
        }

        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + Character.toLowerCase(pattern.charAt(i))) % MOD;
            textHash = (textHash * BASE + Character.toLowerCase(text.charAt(i))) % MOD;
        }

        int collisions = 0;
        int comparisons = 0;

        for (int i = 0; i <= n - m; i++) {
            if (patternHash == textHash) {
                // Verify characters upon hash match to handle spurious collisions
                boolean matched = true;
                for (int j = 0; j < m; j++) {
                    comparisons++;
                    if (Character.toLowerCase(text.charAt(i + j)) != Character.toLowerCase(pattern.charAt(j))) {
                        matched = false;
                        collisions++;
                        break;
                    }
                }
                if (matched) {
                    matches.add(i);
                }
            }

            // Rolling hash update for next window: O(1)
            if (i < n - m) {
                long leadingChar = (Character.toLowerCase(text.charAt(i)) * highestPower) % MOD;
                textHash = (textHash - leadingChar + MOD) % MOD;
                textHash = (textHash * BASE + Character.toLowerCase(text.charAt(i + m))) % MOD;
            }
        }

        return new RabinKarpResult(pattern, matches, collisions, patternHash, comparisons);
    }

    // =========================================================================
    // 3. Z-ALGORITHM
    // =========================================================================

    /**
     * Computes the Z-array for a string S where Z[i] is the length of the
     * longest substring starting from S[i] that is also a prefix of S.
     * Time Complexity: O(L) where L = S.length()
     */
    public static int[] computeZArray(String s) {
        int n = s.length();
        int[] z = new int[n];
        int l = 0, r = 0;

        for (int i = 1; i < n; i++) {
            if (i <= r) {
                z[i] = Math.min(r - i + 1, z[i - l]);
            }
            while (i + z[i] < n && Character.toLowerCase(s.charAt(z[i])) == Character.toLowerCase(s.charAt(i + z[i]))) {
                z[i]++;
            }
            if (i + z[i] - 1 > r) {
                l = i;
                r = i + z[i] - 1;
            }
        }
        return z;
    }

    /**
     * Searches pattern in text using Z-algorithm by forming concatenated string: Pattern + "$" + Text.
     * Time Complexity: O(N + M)
     */
    public static List<Integer> zSearch(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return matches;
        }

        String concat = pattern + "$" + text;
        int[] z = computeZArray(concat);
        int m = pattern.length();

        for (int i = m + 1; i < concat.length(); i++) {
            if (z[i] == m) {
                matches.add(i - m - 1);
            }
        }
        return matches;
    }

    // =========================================================================
    // 4. SUFFIX ARRAY & KASAI'S LCP (LONGEST COMMON PREFIX)
    // =========================================================================

    public static class SuffixArrayResult {
        public Integer[] suffixArray;
        public int[] lcpArray;
        public String longestRepeatedSubstring;

        public SuffixArrayResult(Integer[] sa, int[] lcp, String lrs) {
            this.suffixArray = sa;
            this.lcpArray = lcp;
            this.longestRepeatedSubstring = lrs;
        }
    }

    /**
     * Builds Suffix Array and computes Kasai's LCP array to detect repeated
     * terminology and common skill subphrases.
     * Suffix array construction: O(N log^2 N) or O(N log N) via sorting.
     * Kasai's LCP: O(N) linear time.
     */
    public static SuffixArrayResult buildSuffixArrayAndLCP(String text) {
        if (text == null || text.isEmpty()) {
            return new SuffixArrayResult(new Integer[0], new int[0], "");
        }

        int n = text.length();
        Integer[] sa = new Integer[n];
        for (int i = 0; i < n; i++) sa[i] = i;

        // Sort suffixes lexicographically
        Arrays.sort(sa, (a, b) -> text.substring(a).compareToIgnoreCase(text.substring(b)));

        // Kasai's Algorithm for LCP
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;

        int[] lcp = new int[n];
        int k = 0;
        int maxLcp = 0;
        int maxLcpIndex = -1;

        for (int i = 0; i < n; i++) {
            if (rank[i] == n - 1) {
                k = 0;
                continue;
            }
            int j = sa[rank[i] + 1];
            while (i + k < n && j + k < n && Character.toLowerCase(text.charAt(i + k)) == Character.toLowerCase(text.charAt(j + k))) {
                k++;
            }
            lcp[rank[i]] = k;
            if (k > maxLcp) {
                maxLcp = k;
                maxLcpIndex = i;
            }
            if (k > 0) k--;
        }

        String lrs = (maxLcpIndex != -1 && maxLcp > 1) ? text.substring(maxLcpIndex, maxLcpIndex + maxLcp).trim() : "";
        return new SuffixArrayResult(sa, lcp, lrs);
    }
}
