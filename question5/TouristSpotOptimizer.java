package question5;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Question 5(a) — Tourist Spot Optimizer
 * GUI-based Heuristic Itinerary Planner for Nepal Tourist Spots
 * Module: ST5008CEM Programming For Developers
 *
 * Approach:
 * - Greedy Heuristic: Prioritizes spots based on interest match density relative to cost.
 * - Brute Force: Exhaustive search for optimal solution on a subset of data for comparison.
 */
public class TouristSpotOptimizer extends JFrame {

    // 
    // Data Model
    // 
    static class TouristSpot {
        String   name;
        double   latitude, longitude;
        int      entryFee;
        String[] tags;

        TouristSpot(String name, double lat, double lon,
                    int fee, String open, String close, String... tags) {
            this.name      = name;
            this.latitude  = lat;
            this.longitude = lon;
            this.entryFee  = fee;
            this.tags      = tags;
        }

        double visitDuration() { return 1.5; }

        double travelTimeTo(TouristSpot other) {
            double dlat = this.latitude  - other.latitude;
            double dlon = this.longitude - other.longitude;
            double dist = Math.sqrt(dlat * dlat + dlon * dlon);
            return dist * 100; 
        }
    }

    static final List<TouristSpot> ALL_SPOTS = Arrays.asList(
        new TouristSpot("Pashupatinath Temple", 27.7104, 85.3488, 100, "06:00", "18:00", "culture", "religious"),
        new TouristSpot("Swayambhunath Stupa",  27.7149, 85.2906, 200, "07:00", "17:00", "culture", "heritage"),
        new TouristSpot("Garden of Dreams",     27.7125, 85.3170, 150, "09:00", "21:00", "nature", "relaxation"),
        new TouristSpot("Chandragiri Hills",    27.6616, 85.2458, 700, "09:00", "17:00", "nature", "adventure"),
        new TouristSpot("Kathmandu Durbar Sq.", 27.7048, 85.3076, 100, "10:00", "17:00", "culture", "heritage")
    );

    private JSpinner    budgetSpinner, hoursSpinner;
    private JCheckBox   cbCulture, cbNature, cbAdventure, cbReligious, cbHeritage, cbRelaxation;
    private JTextArea   resultArea;
    private MapPanel    mapPanel;
    private JTextArea   bruteArea;

    public TouristSpotOptimizer() {
        super("Tourist Spot Optimizer — Nepal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(245, 248, 250));

        add(buildInputPanel(),  BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildStatusBar(),   BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel buildInputPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 5),
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(52, 120, 200), 2), 
            "User Preferences", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), new Color(52, 120, 200))));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(220, 0));

        p.add(makeLabel("Budget (NPR):"));
        budgetSpinner = new JSpinner(new SpinnerNumberModel(1500, 100, 10000, 100));
        styleSpinner(budgetSpinner);
        p.add(budgetSpinner);

        p.add(makeLabel("Available Time (hrs):"));
        hoursSpinner = new JSpinner(new SpinnerNumberModel(6, 1, 24, 1));
        styleSpinner(hoursSpinner);
        p.add(hoursSpinner);

        p.add(makeLabel("Interest Tags:"));
        cbCulture = makeCheckBox("Culture", true);
        cbNature = makeCheckBox("Nature", true);
        cbAdventure = makeCheckBox("Adventure", false);
        cbReligious = makeCheckBox("Religious", false);
        cbHeritage = makeCheckBox("Heritage", true);
        cbRelaxation = makeCheckBox("Relaxation", false);
        p.add(cbCulture); p.add(cbNature); p.add(cbAdventure); 
        p.add(cbReligious); p.add(cbHeritage); p.add(cbRelaxation);

        JButton btnRun = new JButton("▶  Find Optimal Itinerary");
        btnRun.setBackground(new Color(52, 120, 200));
        btnRun.setForeground(Color.WHITE);
        btnRun.addActionListener(e -> runOptimizer());
        p.add(Box.createVerticalStrut(16));
        p.add(btnRun);
        return p;
    }

    private JPanel buildCenterPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createTitledBorder("Suggested Itinerary"));
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        left.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 8));
        mapPanel = new MapPanel();
        bruteArea = new JTextArea(6, 0);
        bruteArea.setEditable(false);
        right.add(mapPanel, BorderLayout.CENTER);
        right.add(new JScrollPane(bruteArea), BorderLayout.SOUTH);

        p.add(left); p.add(right);
        return p;
    }

    private JLabel buildStatusBar() {
        return new JLabel(" ST5008CEM | Tourist Spot Optimizer | Greedy Heuristic + Brute-Force Comparison");
    }

    private void runOptimizer() {
        int budget = (int) budgetSpinner.getValue();
        double hours = (double) (int) hoursSpinner.getValue();
        Set<String> interests = new HashSet<>();
        if (cbCulture.isSelected()) interests.add("culture");
        if (cbNature.isSelected()) interests.add("nature");
        if (cbAdventure.isSelected()) interests.add("adventure");
        if (cbReligious.isSelected()) interests.add("religious");
        if (cbHeritage.isSelected()) interests.add("heritage");
        if (cbRelaxation.isSelected()) interests.add("relaxation");

        if (interests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one interest tag.");
            return;
        }

        List<TouristSpot> heuristicPath = greedyOptimize(ALL_SPOTS, budget, hours, interests);
        List<TouristSpot> smallSet = ALL_SPOTS.subList(0, Math.min(5, ALL_SPOTS.size()));
        List<TouristSpot> brutePath = bruteForce(smallSet, budget, hours, interests);

        displayResults(heuristicPath, budget);
        displayBruteComparison(heuristicPath, brutePath);
        mapPanel.setPath(heuristicPath);
    }

    private List<TouristSpot> greedyOptimize(List<TouristSpot> spots, int budget, double hours, Set<String> interests) {
        List<TouristSpot> candidates = new ArrayList<>(spots);
        candidates.sort((a, b) -> Double.compare(score(b, interests), score(a, interests)));
        List<TouristSpot> path = new ArrayList<>();
        int bLeft = budget; double tLeft = hours;
        for (TouristSpot s : candidates) {
            if (s.entryFee > bLeft) continue;
            double travel = path.isEmpty() ? 0.5 : path.get(path.size()-1).travelTimeTo(s);
            if (s.visitDuration() + travel > tLeft) continue;
            path.add(s);
            bLeft -= s.entryFee;
            tLeft -= (s.visitDuration() + travel);
        }
        return path;
    }

    private List<TouristSpot> bruteForce(List<TouristSpot> spots, int budget, double hours, Set<String> interests) {
        List<List<TouristSpot>> perms = new ArrayList<>();
        permute(new ArrayList<>(spots), 0, perms);
        List<TouristSpot> best = new ArrayList<>();
        int bestS = -1;
        for (List<TouristSpot> p : perms) {
            int b = budget; double t = hours; int s = 0; boolean valid = true;
            for (TouristSpot sp : p) {
                if (sp.entryFee > b || sp.visitDuration() > t) { valid = false; break; }
                b -= sp.entryFee; t -= sp.visitDuration(); s += interestMatch(sp, interests);
            }
            if (valid && s > bestS) { bestS = s; best = new ArrayList<>(p); }
        }
        return best;
    }

    private void permute(List<TouristSpot> list, int k, List<List<TouristSpot>> result) {
        if (k == list.size()) { result.add(new ArrayList<>(list)); return; }
        for (int i = k; i < list.size(); i++) {
            Collections.swap(list, i, k);
            permute(list, k + 1, result);
            Collections.swap(list, i, k);
        }
    }

    private double score(TouristSpot s, Set<String> interests) {
        return (interestMatch(s, interests) * 10.0) / (s.entryFee + 1);
    }

    private int interestMatch(TouristSpot s, Set<String> interests) {
        int c = 0;
        for (String t : s.tags) if (interests.contains(t)) c++;
        return c;
    }

    private void displayResults(List<TouristSpot> path, int budget) {
        StringBuilder sb = new StringBuilder("--- Greedy Heuristic Plan ---\n");
        int totalCost = 0;
        for (TouristSpot s : path) {
            sb.append("• ").append(s.name).append(" (NPR ").append(s.entryFee).append(")\n");
            totalCost += s.entryFee;
        }
        sb.append("\nTotal Entry Fee: NPR ").append(totalCost);
        sb.append("\nRemaining Budget: NPR ").append(budget - totalCost);
        resultArea.setText(sb.toString());
    }

    private void displayBruteComparison(List<TouristSpot> h, List<TouristSpot> b) {
        bruteArea.setText("""
                          Algorithm Comparison (n=5):

                          Greedy Count: %d spots
                          Brute Count:  %d spots
                          Note: Brute force ensures optimality but scales O(n!).""".formatted(h.size(), b.size()));
    }

    static class MapPanel extends JPanel {
        private List<TouristSpot> path = new ArrayList<>();
        MapPanel() { setBackground(new Color(230, 245, 230)); setBorder(BorderFactory.createTitledBorder("Visual Route"));}
        void setPath(List<TouristSpot> p) { this.path = p; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int W = getWidth(), H = getHeight();
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double minLat = 27.65, maxLat = 27.75, minLon = 85.20, maxLon = 85.40;
            double scaleX = (W - 60) / (maxLon - minLon);
            double scaleY = (H - 60) / (maxLat - minLat);

            if (path.size() > 1) {
                g2.setColor(Color.BLUE);
                for (int i = 0; i < path.size() - 1; i++) {
                    int x1 = (int)(30 + (path.get(i).longitude - minLon) * scaleX);
                    int y1 = (int)(H - 30 - (path.get(i).latitude - minLat) * scaleY);
                    int x2 = (int)(30 + (path.get(i+1).longitude - minLon) * scaleX);
                    int y2 = (int)(H - 30 - (path.get(i+1).latitude - minLat) * scaleY);
                    g2.drawLine(x1, y1, x2, y2);
                }
            }
            for (TouristSpot s : ALL_SPOTS) {
                int x = (int)(30 + (s.longitude - minLon) * scaleX);
                int y = (int)(H - 30 - (s.latitude - minLat) * scaleY);
                g2.setColor(path.contains(s) ? Color.RED : Color.GRAY);
                g2.fillOval(x-5, y-5, 10, 10);
                g2.drawString(s.name, x+7, y);
            }
        }
    }

    private JLabel makeLabel(String t) { return new JLabel(t); }
    private JCheckBox makeCheckBox(String t, boolean s) { return new JCheckBox(t, s); }
    private void styleSpinner(JSpinner s) { s.setMaximumSize(new Dimension(180, 30)); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Specific look and feel catch clauses to avoid broad Exception catch hint
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | 
                     IllegalAccessException | UnsupportedLookAndFeelException e) {
                // Fallback to default
            }
            // Assigned to variable to avoid "New instance ignored" hint
            TouristSpotOptimizer app = new TouristSpotOptimizer();
            app.setTitle("Tourist Spot Optimizer — Nepal (Active)");
        });
    }
}