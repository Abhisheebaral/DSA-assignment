package question2;

/**
 * Question 2 — Hydropower Plant Cascade Efficiency
 * ==================================================
 * Problem: Given a binary tree where each node represents a hydropower plant
 * with a net power value (positive = generation, negative = environmental cost),
 * find the maximum total power from any connected path in the tree.
 * The path does NOT need to pass through the root.
 *
 * 
 */
public class HydropowerMaxPathSum {

    static class TreeNode {
        int val;
        TreeNode left, right;

        TreeNode(int val) { this.val = val; }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    // Global maximum updated during DFS traversal
    private static int globalMax;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Q2: Hydropower Plant Cascade — Max Path Sum     ");
        System.out.println("=================================================");
        System.out.println();

        // Example 1: Tree = [1, 2, 3]
        //         1
        //        / \
        //       2   3
        TreeNode root1 = new TreeNode(1,
            new TreeNode(2),
            new TreeNode(3));
        System.out.println("Example 1:");
        System.out.println("  Tree  : root=1, left=2 (upper tributary), right=3 (lower tributary)");
        System.out.println("  Structure:");
        System.out.println("        1");
        System.out.println("       / \\");
        System.out.println("      2   3");
        int result1 = maxPathSum(root1);
        System.out.println("  Optimal path : 2 -> 1 -> 3  (2 + 1 + 3 = 6)");
        System.out.println("  Output       : " + result1);
        System.out.println("  Expected     : 6");
        System.out.println("  Status       : " + (result1 == 6 ? "PASS" : "FAIL"));
        System.out.println();

        // Example 2: Tree = [-10, 9, 20, null, null, 15, 7]
        //          -10
        //          /  \
        //         9   20
        //            /  \
        //           15   7
        TreeNode root2 = new TreeNode(-10,
            new TreeNode(9),
            new TreeNode(20,
                new TreeNode(15),
                new TreeNode(7)));
        System.out.println("Example 2:");
        System.out.println("  Tree  : root=-10, left=9, right=20 with children 15 and 7");
        System.out.println("  Structure:");
        System.out.println("       -10");
        System.out.println("       /  \\");
        System.out.println("      9   20");
        System.out.println("         /  \\");
        System.out.println("        15   7");
        int result2 = maxPathSum(root2);
        System.out.println("  Optimal path : 15 -> 20 -> 7  (15 + 20 + 7 = 42)");
        System.out.println("  Note         : -10 and 9 are excluded (negative / not on best path)");
        System.out.println("  Output       : " + result2);
        System.out.println("  Expected     : 42");
        System.out.println("  Status       : " + (result2 == 42 ? "PASS" : "FAIL"));
        System.out.println();

        // Edge case: all negative values
        TreeNode root3 = new TreeNode(-3,
            new TreeNode(-1),
            new TreeNode(-2));
        System.out.println("Edge Case — All Negative Values:");
        System.out.println("  Tree  : [-3, -1, -2]");
        int result3 = maxPathSum(root3);
        System.out.println("  Output   : " + result3 + "  (best single node chosen)");
        System.out.println("  Expected : -1");
        System.out.println("  Status   : " + (result3 == -1 ? "PASS" : "FAIL"));
        System.out.println();

        // Edge case: single node
        TreeNode root4 = new TreeNode(42);
        System.out.println("Edge Case — Single Node:");
        System.out.println("  Tree  : [42]");
        int result4 = maxPathSum(root4);
        System.out.println("  Output   : " + result4);
        System.out.println("  Expected : 42");
        System.out.println("  Status   : " + (result4 == 42 ? "PASS" : "FAIL"));
        System.out.println();

        System.out.println("Algorithm Notes:");
        System.out.println("  - Post-order DFS visits children before parent");
        System.out.println("  - Negative child gains are clamped to 0 (ignored)");
        System.out.println("  - globalMax tracks the best complete path across all nodes");
        System.out.println("  - Each node returns its best single-arm value to its parent");
        System.out.println("  - Time Complexity : O(n)");
        System.out.println("  - Space Complexity: O(h) — h = height of tree");
    }

    /**
     * Entry point: initializes globalMax and starts DFS.
     * Returns the maximum path sum found in the tree.
     */
    public static int maxPathSum(TreeNode root) {
        globalMax = Integer.MIN_VALUE;
        dfs(root);
        return globalMax;
    }

    /**
     * Post-order DFS helper.
     *
     * For each node:
     *   - Compute left and right gains (clamp to 0 if negative)
     *   - Update globalMax with the path that passes through this node
     *   - Return the best single-direction gain to the parent
     *
     * @param node Current tree node
     * @return Maximum gain this node can contribute to a parent path
     */
    private static int dfs(TreeNode node) {
        if (node == null) return 0;

        // Recursively get best gains from left and right subtrees
        // Use max(gain, 0) to ignore negative subtrees
        int leftGain  = Math.max(dfs(node.left),  0);
        int rightGain = Math.max(dfs(node.right), 0);

        // Best complete path going through this node (left arm + node + right arm)
        int pathThroughNode = node.val + leftGain + rightGain;

        // Update global maximum with this complete path
        globalMax = Math.max(globalMax, pathThroughNode);

        // Return the best single-arm contribution to this node's parent
        // (A path cannot branch when extending to a parent)
        return node.val + Math.max(leftGain, rightGain);
    }
}