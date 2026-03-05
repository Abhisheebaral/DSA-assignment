package question;

import java.util.*;

/**
 * Question 1a — Telecommunications Signal Repeater Placement
 * ============================================================
 * Problem: Given customer home coordinates on a 2D grid, find the maximum
 * number of homes that lie on the same straight line. This determines the
 * optimal placement of a single signal repeater to maximize coverage.
 *
 * Approach:
 *   For each point P_i, compute the slope from P_i to every other point P_j.
 *   Points sharing the same slope from P_i are collinear with P_i.
 *   Use a HashMap to count points per slope. Track the global maximum.
 *
 *   To avoid floating-point errors, slopes are stored as reduced fractions
 *   "dy/dx" using the GCD (Greatest Common Divisor).
 *
 * Time Complexity : O(n^2) — for each of n points, we check n-1 others
 * Space Complexity: O(n)   — slope map per anchor point
 */
public class MaxPointsOnLine {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Q1a: Maximum Customer Homes on One Signal Line  ");
        System.out.println("=================================================");
        System.out.println();

        // Example 1: All three on the same diagonal
        int[][] locations1 = {{1, 1}, {2, 2}, {3, 3}};
        System.out.println("Example 1 — Ideal Repeater Placement:");
        System.out.println("  Input    : [[1,1], [2,2], [3,3]]");
        System.out.println("  Expected : 3 (all lie on y = x diagonal)");
        int result1 = maxPoints(locations1);
        System.out.println("  Output   : " + result1);
        System.out.println("  Status   : " + (result1 == 3 ? "PASS" : "FAIL"));
        System.out.println();

        // Example 2: 4 out of 6 points are collinear
        int[][] locations2 = {{1, 1}, {3, 2}, {5, 3}, {4, 1}, {2, 3}, {1, 4}};
        System.out.println("Example 2 — Complex Repeater Placement:");
        System.out.println("  Input    : [[1,1], [3,2], [5,3], [4,1], [2,3], [1,4]]");
        System.out.println("  Expected : 4 ([1,4]->[2,3]->[3,2]->[4,1] are collinear)");
        int result2 = maxPoints(locations2);
        System.out.println("  Output   : " + result2);
        System.out.println("  Status   : " + (result2 == 4 ? "PASS" : "FAIL"));
        System.out.println();

        // Edge cases
        System.out.println("Edge Case Tests:");

        int[][] single = {{5, 5}};
        System.out.println("  Single point  -> Output: " + maxPoints(single) + " (expected 1)");

        int[][] duplicate = {{1, 1}, {1, 1}, {2, 2}};
        System.out.println("  With duplicate-> Output: " + maxPoints(duplicate) + " (expected 3)");

        int[][] vertical = {{1, 1}, {1, 3}, {1, 5}, {2, 2}};
        System.out.println("  Vertical line -> Output: " + maxPoints(vertical) + " (expected 3)");

        System.out.println();
        System.out.println("Algorithm Notes:");
        System.out.println("  - Slopes stored as reduced fraction strings to avoid float errors");
        System.out.println("  - GCD used to normalize dx and dy");
        System.out.println("  - Vertical lines stored as INF/0 slope representation");
        System.out.println("  - Time Complexity: O(n^2)");
        System.out.println("  - Space Complexity: O(n)");
    }

    /**
     * Returns the maximum number of customer locations lying on one straight line.
     *
     * For each anchor point i, we compute the slope to every other point j.
     * Slopes are normalized using GCD to canonical fraction form (dy/dx).
     * The maximum frequency in the slope map + 1 (for the anchor itself) gives
     * the max collinear count from anchor i.
     */
    public static int maxPoints(int[][] points) {
        int n = points.length;
        if (n == 1) return 1;
        if (n == 2) return 2;

        int maxCount = 2;

        for (int i = 0; i < n; i++) {
            Map<String, Integer> slopeMap = new HashMap<>();
            int duplicate = 1; // Count anchor point itself

            for (int j = i + 1; j < n; j++) {
                int dx = points[j][0] - points[i][0];
                int dy = points[j][1] - points[i][1];

                // Handle exact duplicate points
                if (dx == 0 && dy == 0) {
                    duplicate++;
                    continue;
                }

                // Reduce slope by GCD for canonical form
                int g = gcd(Math.abs(dx), Math.abs(dy));
                dx /= g;
                dy /= g;

                // Normalize sign: keep dx positive (or dy positive if dx==0)
                if (dx < 0) { dx = -dx; dy = -dy; }
                if (dx == 0) dy = Math.abs(dy); // vertical line

                String slope = dy + "/" + dx;
                slopeMap.put(slope, slopeMap.getOrDefault(slope, 0) + 1);
            }

            // Max from this anchor = most common slope count + anchor duplicates
            int localMax = duplicate;
            for (int count : slopeMap.values()) {
                localMax = Math.max(localMax, count + duplicate);
            }
            maxCount = Math.max(maxCount, localMax);
        }

        return maxCount;
    }

    /**
     * Computes Greatest Common Divisor using Euclidean algorithm.
     * Used to reduce slope fractions to canonical form.
     */
    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}