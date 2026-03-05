package question;

import java.util.*;

/**
 * Question 1b — Digital Marketing Keyword Segmentation (Word Break II)
 * =====================================================================
 * Problem: Given a continuous user search query string and a marketing
 * keywords dictionary, find ALL possible ways to segment the query into
 * valid dictionary words (keywords can be reused).
 *
 * Approach: Backtracking + Memoization (Top-Down Dynamic Programming)
 *   - At each position 'start', try every prefix query[start..end]
 *   - If the prefix is a valid keyword, recurse on the remainder
 *   - Cache results at each start index to avoid recomputation
 *   - Base case: when start == query.length(), we found a valid segmentation
 *
 * Why not plain BFS/DFS without memo?
 *   Without memoization, overlapping subproblems are recomputed repeatedly.
 *   For a query of length n with k dictionary words, naive DFS is O(2^n).
 *   Memoization reduces this significantly by caching per-index results.
 *
 * Time Complexity : O(n^2 * 2^n) worst case (exponential output size)
 * Space Complexity: O(n * 2^n) for storing all results
 */
public class WordBreakII {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Q1b: Marketing Keyword Query Segmentation        ");
        System.out.println("=================================================");
        System.out.println();

        // Example 1
        String query1 = "nepaltrekkingguide";
        List<String> dict1 = Arrays.asList("nepal", "trekking", "guide", "nepaltrekking");
        List<String> result1 = wordBreak(query1, dict1);
        System.out.println("Example 1 — Basic Keyword Segmentation:");
        System.out.println("  Query      : \"" + query1 + "\"");
        System.out.println("  Dictionary : " + dict1);
        System.out.println("  Output     : " + result1);
        System.out.println("  Expected   : [nepal trekking guide, nepaltrekking guide]");
        System.out.println("  Status     : " + (result1.size() == 2 ? "PASS" : "FAIL"));
        System.out.println();

        // Example 2
        String query2 = "visitkathmandunepal";
        List<String> dict2 = Arrays.asList("visit", "kathmandu", "nepal", "visitkathmandu", "kathmandunepal");
        List<String> result2 = wordBreak(query2, dict2);
        System.out.println("Example 2 — Complex Keyword Combinations:");
        System.out.println("  Query      : \"" + query2 + "\"");
        System.out.println("  Dictionary : " + dict2);
        System.out.println("  Output     : " + result2);
        System.out.println("  Expected   : [visit kathmandu nepal, visitkathmandu nepal, visit kathmandunepal]");
        System.out.println("  Status     : " + (result2.size() == 3 ? "PASS" : "FAIL"));
        System.out.println();

        // Example 3 — no valid segmentation
        String query3 = "everesthikingtrail";
        List<String> dict3 = Arrays.asList("everest", "hiking", "trek");
        List<String> result3 = wordBreak(query3, dict3);
        System.out.println("Example 3 — No Valid Segmentation:");
        System.out.println("  Query      : \"" + query3 + "\"");
        System.out.println("  Dictionary : " + dict3);
        System.out.println("  Output     : " + result3);
        System.out.println("  Expected   : []  ('trail' not in dictionary)");
        System.out.println("  Status     : " + (result3.isEmpty() ? "PASS" : "FAIL"));
        System.out.println();

        // Additional test — keyword reuse
        String query4 = "kathmandukathmandunepal";
        List<String> dict4 = Arrays.asList("kathmandu", "nepal");
        List<String> result4 = wordBreak(query4, dict4);
        System.out.println("Example 4 — Keyword Reuse:");
        System.out.println("  Query      : \"" + query4 + "\"");
        System.out.println("  Dictionary : " + dict4);
        System.out.println("  Output     : " + result4);
        System.out.println("  Expected   : [kathmandu kathmandu nepal]");
        System.out.println("  Status     : " + (result4.size() == 1 ? "PASS" : "FAIL"));
        System.out.println();

        System.out.println("Algorithm Notes:");
        System.out.println("  - Backtracking explores all valid prefixes at each position");
        System.out.println("  - Memoization caches results per start index to avoid recomputation");
        System.out.println("  - HashSet used for O(1) dictionary lookups");
        System.out.println("  - Time Complexity : O(n^2 * 2^n) worst case");
        System.out.println("  - Space Complexity: O(n * 2^n)");
    }

    /**
     * Returns all valid keyword sentence segmentations of the user query.
     * Uses a HashSet for O(1) keyword lookups and a memo map to cache
     * results at each start index in the query.
     */
    public static List<String> wordBreak(String query, List<String> dictionary) {
        Set<String> wordSet = new HashSet<>(dictionary);
        Map<Integer, List<String>> memo = new HashMap<>();
        return backtrack(query, 0, wordSet, memo);
    }

    /**
     * Recursive backtracking with memoization.
     *
     * At each 'start' position, tries all substrings query[start..end].
     * If that substring is a valid keyword, recurses on query[end..].
     * Combines the keyword with each sub-result to form complete sentences.
     *
     * @param query  The full user search query
     * @param start  Current start index in the query
     * @param words  Set of valid marketing keywords
     * @param memo   Cache mapping start index -> list of valid sentences
     * @return       All valid sentences starting from position 'start'
     */
    private static List<String> backtrack(String query, int start,
                                           Set<String> words,
                                           Map<Integer, List<String>> memo) {
        // Return cached result if already computed
        if (memo.containsKey(start)) return memo.get(start);

        List<String> results = new ArrayList<>();

        // Base case: consumed entire query — valid segmentation found
        if (start == query.length()) {
            results.add("");
            return results;
        }

        // Try every possible end position from start
        for (int end = start + 1; end <= query.length(); end++) {
            String word = query.substring(start, end);

            if (words.contains(word)) {
                // Word is valid — recurse on the remainder
                List<String> subResults = backtrack(query, end, words, memo);
                for (String sub : subResults) {
                    // Build sentence: prepend current word to sub-result
                    String sentence = sub.isEmpty() ? word : word + " " + sub;
                    results.add(sentence);
                }
            }
        }

        // Cache and return results for this start index
        memo.put(start, results);
        return results;
    }
}