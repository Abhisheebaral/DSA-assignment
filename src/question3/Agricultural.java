package question3;

/**
 * Question 3 - Agricultural Commodity Trading
 * Module: ST5008CEM Programming For Developers
 *
 * Problem: Given an array of daily commodity prices and an integer max_trades (k),
 * find the maximum profit achievable using at most k buy-sell transactions.
 * You must sell before buying again (no simultaneous holdings).
 *
 * Approach: Dynamic Programming
 *
 *   State: dp[t][d] = maximum profit using at most t transactions up to day d.
 *
 *   Recurrence:
 *     dp[t][d] = max(
 *       dp[t][d-1],                         // do nothing on day d
 *       max over j in [0..d-1] of:
 *           dp[t-1][j] + price[d] - price[j] // sell on day d, bought on day j
 *     )
 *
 *   Optimisation: maintain a running variable `maxSoFar` = max(dp[t-1][j] - price[j])
 *   so the inner loop collapses and the overall complexity is O(k * n).
 *
 *   Special case: if 2*k >= n, we can make every profitable move, so greedily
 *   sum all positive consecutive differences.
 *
 * Time Complexity : O(k * n)
 * Space Complexity: O(k * n) — can be reduced to O(n) with rolling array
 */
public class Agricultural{

    /**
     * Returns the maximum profit using at most maxTrades buy-sell transactions.
     *
     * @param dailyPrices array of commodity prices per day (in NPR per quintal)
     * @param maxTrades   maximum number of transactions allowed
     * @return maximum achievable profit (NPR)
     */
    public static int maxProfit(int[] dailyPrices, int maxTrades) {
        int n = dailyPrices.length;
        if (n <= 1 || maxTrades <= 0) return 0;

        // Special case: k is large enough to capture every profitable day
        if (2 * maxTrades >= n) {
            int profit = 0;
            for (int i = 1; i < n; i++) {
                if (dailyPrices[i] > dailyPrices[i - 1]) {
                    profit += dailyPrices[i] - dailyPrices[i - 1];
                }
            }
            return profit;
        }

        // DP table: dp[t][d] = max profit with at most t trades up to day d
        int[][] dp = new int[maxTrades + 1][n];

        for (int t = 1; t <= maxTrades; t++) {
            // maxSoFar = best value of dp[t-1][j] - price[j] seen so far
            int maxSoFar = -dailyPrices[0];

            for (int d = 1; d < n; d++) {
                // Option 1: don't trade on day d
                dp[t][d] = dp[t][d - 1];

                // Option 2: sell on day d using the best previous buy
                dp[t][d] = Math.max(dp[t][d], dailyPrices[d] + maxSoFar);

                // Update maxSoFar with day d as a potential buy day for future sells
                maxSoFar = Math.max(maxSoFar, dp[t - 1][d] - dailyPrices[d]);
            }
        }

        return dp[maxTrades][n - 1];
    }

    // -----------------------------------------------------------------------
    // Utility: reconstruct a sample trade sequence for display purposes
    // (for small k, trace back through the DP table)
    // -----------------------------------------------------------------------
    private static void printTradeDetails(int[] prices, int k) {
        int n = prices.length;
        if (2 * k >= n) {
            System.out.println("  (Unlimited-mode: capturing every positive day)");
            return;
        }
        int[][] dp = new int[k + 1][n];
        int[][] maxSoFarArr = new int[k + 1][n]; // for reconstruction

        for (int t = 1; t <= k; t++) {
            int msf = -prices[0];
            for (int d = 1; d < n; d++) {
                dp[t][d] = dp[t][d - 1];
                if (prices[d] + msf > dp[t][d]) {
                    dp[t][d] = prices[d] + msf;
                }
                msf = Math.max(msf, dp[t - 1][d] - prices[d]);
                maxSoFarArr[t][d] = msf;
            }
        }

        // Print DP table (small datasets only)
        System.out.println("  DP Table (rows = trades 0.." + k + ", cols = days):");
        System.out.print("  Trades\\Day | ");
        for (int d = 0; d < n; d++) System.out.printf("Day%-3d ", d + 1);
        System.out.println();
        for (int t = 0; t <= k; t++) {
            System.out.printf("  Trade %-5d |", t);
            for (int d = 0; d < n; d++) System.out.printf(" %-6d", dp[t][d]);
            System.out.println();
        }
    }

    // -----------------------------------------------------------------------
    // Test Driver
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  Q3 — Agricultural Commodity Trading (Max k Transactions)");
        System.out.println("==========================================================\n");

        // Example 1: max_trades=2, prices=[2000,4000,1000]
        int[] prices1 = {2000, 4000, 1000};
        int k1 = 2;
        int result1 = maxProfit(prices1, k1);
        System.out.println("Example 1:");
        System.out.println("  daily_prices : [2000, 4000, 1000] NPR/quintal");
        System.out.println("  max_trades   : " + k1);
        System.out.println("  Output       : " + result1 + " NPR");
        System.out.println("  Expected     : 2000 NPR");
        System.out.println("  Explanation  : Buy Day1@2000, Sell Day2@4000, profit=2000");
        System.out.println("  Status       : " + (result1 == 2000 ? "PASS ✓" : "FAIL ✗"));
        System.out.println();
        printTradeDetails(prices1, k1);

        System.out.println();

        // Example 2: classic test — prices=[3,2,6,5,0,3], k=2 → expected 7
        int[] prices2 = {3, 2, 6, 5, 0, 3};
        int k2 = 2;
        int result2 = maxProfit(prices2, k2);
        System.out.println("Example 2:");
        System.out.println("  daily_prices : [3, 2, 6, 5, 0, 3] NPR/quintal");
        System.out.println("  max_trades   : " + k2);
        System.out.println("  Output       : " + result2 + " NPR");
        System.out.println("  Expected     : 7 NPR");
        System.out.println("  Explanation  : Buy@2 Sell@6 (+4), Buy@0 Sell@3 (+3) = 7");
        System.out.println("  Status       : " + (result2 == 7 ? "PASS ✓" : "FAIL ✗"));
        System.out.println();
        printTradeDetails(prices2, k2);

        System.out.println();

        // Example 3: k=1 → just best single trade
        int[] prices3 = {100, 180, 260, 310, 40, 535, 695};
        int k3 = 1;
        int result3 = maxProfit(prices3, k3);
        System.out.println("Example 3 (k=1, single best trade):");
        System.out.println("  daily_prices : [100,180,260,310,40,535,695]");
        System.out.println("  max_trades   : " + k3);
        System.out.println("  Output       : " + result3 + " NPR");
        System.out.println("  Expected     : 655 NPR  (Buy@40, Sell@695)");
        System.out.println("  Status       : " + (result3 == 655 ? "PASS ✓" : "FAIL ✗"));

        System.out.println();

        // Edge case: no profitable trade
        int[] prices4 = {5, 4, 3, 2, 1};
        int k4 = 2;
        int result4 = maxProfit(prices4, k4);
        System.out.println("Edge Case (descending prices — no profit possible):");
        System.out.println("  daily_prices : [5,4,3,2,1]");
        System.out.println("  max_trades   : " + k4);
        System.out.println("  Output       : " + result4 + " NPR");
        System.out.println("  Expected     : 0 NPR");
        System.out.println("  Status       : " + (result4 == 0 ? "PASS ✓" : "FAIL ✗"));

        System.out.println("\n----------------------------------------------------------");
        System.out.println("Algorithm : Dynamic Programming — O(k*n) time | O(k*n) space");
    }
}