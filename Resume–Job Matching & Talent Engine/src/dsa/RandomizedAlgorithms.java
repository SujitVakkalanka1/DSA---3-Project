package dsa;

import model.MatchResult;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Module 6: Randomized & Parallel Algorithms - Part 1: Randomized Algorithms
 *
 * Implements Randomized QuickSort for ranking candidate match results.
 *
 * Algorithmic Characteristics:
 * - Pivot Selection: Chosen uniformly at random from range [low, high].
 * - Expected Time Complexity: O(N log N)
 * - Worst-Case Time Complexity: O(N^2)
 * - Auxiliary Space Complexity: O(log N) average recursion stack
 *
 * Viva & Theoretical Rationale:
 * A deterministic QuickSort (e.g., picking the first or last element as pivot) degrades
 * to worst-case O(N^2) quadratic time on already sorted or reverse-sorted candidate rankings.
 * Selecting a random pivot ensures that consistently unbalanced partitions have an
 * infinitesimally low probability (~ 2^N / N!), guaranteeing expected O(N log N) performance
 * regardless of the initial ordering of candidate profiles.
 *
 * Course: DSA-3 (25CS2103E) - Talent Engine
 */
public class RandomizedAlgorithms {

    /**
     * Sorts a list of MatchResult objects in descending order of total score
     * using Randomized QuickSort.
     */
    public static void randomizedQuickSort(List<MatchResult> list) {
        if (list == null || list.size() <= 1) return;
        sort(list, 0, list.size() - 1);
    }

    private static void sort(List<MatchResult> list, int low, int high) {
        if (low < high) {
            int pIndex = randomizedPartition(list, low, high);
            sort(list, low, pIndex - 1);
            sort(list, pIndex + 1, high);
        }
    }

    private static int randomizedPartition(List<MatchResult> list, int low, int high) {
        // Pick random pivot index in [low, high]
        int randomPivotIdx = ThreadLocalRandom.current().nextInt(low, high + 1);
        swap(list, randomPivotIdx, high);
        return partition(list, low, high);
    }

    private static int partition(List<MatchResult> list, int low, int high) {
        double pivotScore = list.get(high).getTotalScore();
        int i = low - 1;

        // Descending order sort (highest compatibility first)
        for (int j = low; j < high; j++) {
            if (list.get(j).getTotalScore() >= pivotScore) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static void swap(List<MatchResult> list, int i, int j) {
        MatchResult temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
