package dsa;

import model.Candidate;
import model.Job;
import model.MatchResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 6: Randomized & Parallel Algorithms - Part 2: Parallel Stream Processing
 *
 * Implements parallel evaluations and analytics reduction using Java SE's
 * ForkJoinPool-backed Parallel Streams.
 *
 * Performance Characterization:
 * - Parallel execution time is WORKLOAD, DATA SIZE, AND HARDWARE/CPU-DEPENDENT.
 * - For small datasets (N < 100), thread dispatch and fork-join merge overhead
 *   can dominate execution time.
 * - For larger workloads or heavy text processing batches, parallel multi-core
 *   execution scales across available CPU cores.
 * - This module measures and reports empirical sequential vs parallel execution times.
 *
 * Course: DSA-3 (25CS2103E) - Talent Engine
 */
public class ParallelAnalytics {

    public static class BenchmarkResult {
        public int availableProcessors;
        public int pairsEvaluated;
        public double sequentialTimeMs;
        public double parallelTimeMs;
        public double speedupFactor;
        public String parallelModel = "Java ForkJoinPool Common Pool";
        public String note = "Parallel performance is workload, batch-size, and CPU-core dependent.";

        public String toJson() {
            return String.format(
                "{\"availableProcessors\":%d,\"pairsEvaluated\":%d,\"sequentialTimeMs\":%.3f,\"parallelTimeMs\":%.3f,\"speedupFactor\":%.2f,\"parallelModel\":\"%s\",\"note\":\"%s\"}",
                availableProcessors, pairsEvaluated, sequentialTimeMs, parallelTimeMs, speedupFactor, parallelModel, note
            );
        }
    }

    /**
     * Executes empirical benchmark comparing sequential vs parallel all-pairs compatibility evaluation.
     * Repeats evaluation iterations to yield measurable timings on multi-core systems.
     */
    public static BenchmarkResult runBenchmark(List<Candidate> candidates, List<Job> jobs) {
        BenchmarkResult res = new BenchmarkResult();
        res.availableProcessors = Runtime.getRuntime().availableProcessors();

        if (candidates == null || candidates.isEmpty() || jobs == null || jobs.isEmpty()) {
            res.pairsEvaluated = 0;
            res.sequentialTimeMs = 0.0;
            res.parallelTimeMs = 0.0;
            res.speedupFactor = 1.0;
            return res;
        }

        int iterations = 1500; // Workload simulation multiplier
        int totalPairs = candidates.size() * jobs.size() * iterations;
        res.pairsEvaluated = totalPairs;

        // 1. Sequential Stream Benchmark
        long seqStart = System.nanoTime();
        double seqSum = 0.0;
        for (int it = 0; it < iterations; it++) {
            for (Candidate c : candidates) {
                for (Job j : jobs) {
                    MatchResult mr = WeightedMatcher.computeMatch(c, j);
                    seqSum += mr.getTotalScore();
                }
            }
        }
        long seqDuration = System.nanoTime() - seqStart;
        res.sequentialTimeMs = seqDuration / 1_000_000.0;

        // 2. Parallel Stream Benchmark (ForkJoinPool)
        long parStart = System.nanoTime();
        double parSum = candidates.parallelStream().mapToDouble(c -> {
            double localSum = 0;
            for (int it = 0; it < iterations; it++) {
                for (Job j : jobs) {
                    localSum += WeightedMatcher.computeMatch(c, j).getTotalScore();
                }
            }
            return localSum;
        }).sum();
        long parDuration = System.nanoTime() - parStart;
        res.parallelTimeMs = parDuration / 1_000_000.0;

        res.speedupFactor = res.parallelTimeMs > 0 ? (res.sequentialTimeMs / res.parallelTimeMs) : 1.0;
        return res;
    }

    /**
     * Computes all-pairs compatibility matrix using parallel streams.
     */
    public static List<MatchResult> computeAllPairsParallel(List<Candidate> candidates, List<Job> jobs) {
        if (candidates == null || jobs == null) return Collections.emptyList();

        return candidates.parallelStream()
            .flatMap(c -> jobs.stream().map(j -> WeightedMatcher.computeMatch(c, j)))
            .collect(Collectors.toList());
    }
}
