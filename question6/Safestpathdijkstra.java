package question6;

import java.util.*;

/**
 * Question 6 — Emergency Supply Logistics
 * Part A — Safest Path using Modified Dijkstra with -log(p) weight transformation.
 *
 * Key idea:
 *   Maximizing product(p(e)) == Minimizing sum(-log(p(e)))
 *   So we set w(e) = -log(p(e)) and run standard Dijkstra.
 *   Final safety probability = e^(-dist[v])
 *
 * Time Complexity: O((V + E) log V)
 */
public class Safestpathdijkstra {

    private static final String[] NODES = {"KTM", "JA", "JB", "PH", "BS"};
    private static final int KTM = 0, JA = 1, JB = 2, PH = 3, BS = 4;
    private static final int N = NODES.length;
    private static final double INF = Double.MAX_VALUE;

    private static final double[][] prob = new double[N][N];

    public static void main(String[] args) {
        initializeGraph();

        double[] dist   = new double[N];
        int[]    parent = new int[N];
        runDijkstra(KTM, dist, parent);

        System.out.println("Emergency Logistics — Safest Path from KTM");
        System.out.println("Transform: w(e) = -log(p(e)) | Maximizing product(p) == Minimizing sum(-log(p))");
        System.out.println();

        System.out.println("Safest Paths from KTM:");
        System.out.println("  Destination | Safest Path              | Safety Probability");
        System.out.println("  ------------|--------------------------|-------------------");
        for (int v = 0; v < N; v++) {
            if (v == KTM) continue;
            List<Integer> path = getPath(KTM, v, parent);
            double safety = Math.exp(-dist[v]);
            System.out.printf("  %-11s | %-24s | %.4f (%.2f%%)%n",
                NODES[v], formatPath(path), safety, safety * 100);
        }

        System.out.println();
        // Highlight KTM -> PH (medical supplies)
        List<Integer> phPath = getPath(KTM, PH, parent);
        double phSafety = Math.exp(-dist[PH]);
        System.out.println("Priority Route — Patan Hospital (Medical Supplies):");
        System.out.println("  Safest Path : " + formatPath(phPath));
        System.out.printf("  Safety      : %.4f (%.2f%% chance of safe delivery)%n", phSafety, phSafety * 100);
    }

    /**
     * Initializes the safety probability matrix from the assignment data.
     */
    private static void initializeGraph() {
        prob[KTM][JA] = 0.90; prob[KTM][JB] = 0.80;
        prob[JA][KTM] = 0.90; prob[JA][PH]  = 0.95; prob[JA][BS] = 0.70;
        prob[JB][KTM] = 0.80; prob[JB][JA]  = 0.60; prob[JB][BS] = 0.90;
        prob[PH][JA]  = 0.95; prob[PH][BS]  = 0.85;
        prob[BS][JA]  = 0.70; prob[BS][JB]  = 0.90; prob[BS][PH] = 0.85;
    }

    /**
     * Modified Dijkstra using w(e) = -log(p(e)).
     * RELAX: if dist[u] + (-log(p(u,v))) < dist[v], update dist[v].
     */
    public static void runDijkstra(int source, double[] dist, int[] parent) {
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[source] = 0.0;
        parent[source] = source;

        // Min-heap: [accumulated -log(p), node]
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        pq.offer(new double[]{0.0, source});
        boolean[] visited = new boolean[N];

        while (!pq.isEmpty()) {
            double[] curr = pq.poll();
            int u = (int) curr[1];
            if (visited[u]) continue;
            visited[u] = true;

            for (int v = 0; v < N; v++) {
                if (prob[u][v] == 0) continue;
                double weight = -Math.log(prob[u][v]); // w(e) = -log(p(e))
                if (dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    parent[v] = u;
                    pq.offer(new double[]{dist[v], v});
                }
            }
        }
    }

    /**
     * Reconstructs path from source to target using parent array.
     */
    private static List<Integer> getPath(int source, int target, int[] parent) {
        List<Integer> path = new ArrayList<>();
        for (int v = target; v != source; v = parent[v]) {
            if (parent[v] == -1) return Collections.emptyList();
            path.add(v);
        }
        path.add(source);
        Collections.reverse(path);
        return path;
    }

    private static String formatPath(List<Integer> path) {
        if (path.isEmpty()) return "UNREACHABLE";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(NODES[path.get(i)]);
            if (i < path.size() - 1) sb.append(" -> ");
        }
        return sb.toString();
    }
}
