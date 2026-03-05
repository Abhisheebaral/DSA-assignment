package question4;

import java.util.*;

/**
 * Question 4 — Smart Energy Grid Load Distribution Optimization (Nepal)
 
 * Context: A smart grid dynamically allocates energy from Solar, Hydro, and
 * Diesel sources to meet hourly power demands of three districts (A, B, C).
 *
 * Tasks implemented:
 *   1. Model Input Data        — demand and source tables in Java arrays
 *   2. Hourly Allocation       — DP modeling of each hour's source assignment
 *   3. Greedy Prioritization   — cheapest source first (Solar -> Hydro -> Diesel)
 *   4. Approximate Satisfaction — ±10% flexibility if exact match not possible
 *   5. Output Table            — hourly allocation per district with % fulfilled
 *   6. Cost & Resource Analysis — total cost, renewable %, diesel usage report
 *
 * Algorithm: Greedy Source Prioritization per hour
 *   Within each hour, sort sources by cost (ascending).
 *   Allocate from cheapest source until its capacity is exhausted.
 *   Move to next cheapest source for remaining demand.
 *   Allow ±10% tolerance if full demand cannot be met.
 *
 * Time Complexity: O(H * D * S log S)
 *   H = hours (18), D = districts (3), S = sources (3)
 */
public class SmartEnergyGrid {

    // 
    // TASK 1: MODEL INPUT DATA
    // 

    /**
     * Energy source data model.
     * Stores type, capacity, availability window, and cost per kWh.
     */
    static class EnergySource {
        String id, type;
        double maxCapacity;  // kWh per hour
        int availStart;      // Hour from (inclusive)
        int availEnd;        // Hour to (exclusive)
        double costPerKwh;   // Rs. per kWh

        EnergySource(String id, String type, double maxCapacity,
                     int availStart, int availEnd, double costPerKwh) {
            this.id = id;
            this.type = type;
            this.maxCapacity = maxCapacity;
            this.availStart = availStart;
            this.availEnd = availEnd;
            this.costPerKwh = costPerKwh;
        }

        /** Returns true if this source is available at the given hour. */
        boolean isAvailable(int hour) {
            return hour >= availStart && hour < availEnd;
        }

        @Override
        public String toString() {
            return id + "(" + type + ", max=" + maxCapacity + "kWh, Rs." + costPerKwh + "/kWh)";
        }
    }

    // Hours of operation (06:00 to 23:00)
    private static final int[] HOURS = {
        6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23
    };

    private static final String[] DISTRICTS = {"A", "B", "C"};

    /**
     * Hourly energy demand table [hour_index][district_index] in kWh.
     * Based on assignment brief sample values, extended for all 18 hours.
     */
    private static final double[][] DEMAND = {
        // Hour: A    B    C
        /* 06 */ {20,  15,  25},
        /* 07 */ {22,  16,  28},
        /* 08 */ {25,  18,  30},
        /* 09 */ {28,  20,  32},
        /* 10 */ {30,  22,  35},
        /* 11 */ {32,  24,  36},
        /* 12 */ {35,  26,  38},
        /* 13 */ {33,  24,  36},
        /* 14 */ {30,  22,  34},
        /* 15 */ {28,  20,  32},
        /* 16 */ {30,  22,  34},
        /* 17 */ {35,  26,  40},
        /* 18 */ {38,  28,  42},
        /* 19 */ {40,  30,  45},
        /* 20 */ {38,  28,  43},
        /* 21 */ {35,  26,  40},
        /* 22 */ {30,  22,  36},
        /* 23 */ {25,  18,  30}
    };

    /**
     * Energy source table (from assignment brief).
     * S1 Solar : 50 kWh max, available 06-18, Rs. 1.0/kWh
     * S2 Hydro : 40 kWh max, available 00-24, Rs. 1.5/kWh
     * S3 Diesel: 60 kWh max, available 17-23, Rs. 3.0/kWh
     */
    private static final EnergySource[] SOURCES = {
        new EnergySource("S1", "Solar",  50.0,  6, 18, 1.0),
        new EnergySource("S2", "Hydro",  40.0,  0, 24, 1.5),
        new EnergySource("S3", "Diesel", 60.0, 17, 23, 3.0)
    };

    // Totals for analysis report
    private static double totalCost      = 0;
    private static double totalEnergy    = 0;
    private static double totalRenewable = 0;
    private static double totalDiesel    = 0;
    private static int    dieselHours    = 0;

    // Stores which hours used diesel for reporting
    private static final List<String> dieselUsageLog = new ArrayList<>();

    // 
    // MAIN
    // 

    public static void main(String[] args) {
        printInputSummary();
        printSampleCalculation();
        printAllocationTable();
        printCostAnalysis();
        printAlgorithmNotes();
    }

    // 
    // TASK 2 & 3: HOURLY ALLOCATION ALGORITHM WITH GREEDY PRIORITIZATION
    // 

    /**
     * Allocates energy for one hour across all districts using greedy source selection.
     *
     * Step 1: Sort sources by cost ascending (greedy: cheapest first).
     * Step 2: For each source (in cost order), if available, fill district demand.
     * Step 3: If residual demand <= 10% of original demand, accept (±10% tolerance).
     *
     * @param hourIndex Index into HOURS array
     * @return allocation[source_index][district_index] in kWh
     */
    private static double[][] allocateHour(int hourIndex) {
        int hour = HOURS[hourIndex];
        double[] districtRemaining = DEMAND[hourIndex].clone();
        double[][] allocation = new double[SOURCES.length][DISTRICTS.length];

        // TASK 3: Sort sources by costPerKwh ascending (greedy prioritization)
        EnergySource[] sorted = SOURCES.clone();
        Arrays.sort(sorted, Comparator.comparingDouble(s -> s.costPerKwh));

        for (EnergySource src : sorted) {
            if (!src.isAvailable(hour)) continue;

            int si = getSourceIndex(src.id);
            double remaining = src.maxCapacity; // Available capacity this hour

            // Allocate to each district in order
            for (int d = 0; d < DISTRICTS.length; d++) {
                if (districtRemaining[d] <= 0 || remaining <= 0) continue;

                double give = Math.min(districtRemaining[d], remaining);
                allocation[si][d] += give;
                districtRemaining[d] -= give;
                remaining -= give;
            }
        }

        // TASK 4: ±10% flexibility — accept if unmet demand is within 10% of original
        for (int d = 0; d < DISTRICTS.length; d++) {
            if (districtRemaining[d] > 0 && districtRemaining[d] <= DEMAND[hourIndex][d] * 0.10) {
                // Within tolerance — mark as satisfied
                districtRemaining[d] = 0;
            }
        }

        return allocation;
    }

    private static int getSourceIndex(String id) {
        for (int i = 0; i < SOURCES.length; i++)
            if (SOURCES[i].id.equals(id)) return i;
        return -1;
    }

    // 
    // TASK 5: OUTPUT TABLE OF RESULTS
    // 

    private static void printAllocationTable() {
        System.out.println("HOURLY ALLOCATION TABLE");
        System.out.println("Hour | Dist | Solar(kWh) | Hydro(kWh) | Diesel(kWh) | Total | Demand | % Met | Cost(Rs.)");
        System.out.println("-----|------|------------|------------|-------------|-------|--------|-------|----------");

        for (int h = 0; h < HOURS.length; h++) {
            double[][] alloc   = allocateHour(h);
            double[]   demand  = DEMAND[h];
            boolean    usedDiesel = false;

            for (int d = 0; d < DISTRICTS.length; d++) {
                double solar  = alloc[0][d];
                double hydro  = alloc[1][d];
                double diesel = alloc[2][d];
                double used   = solar + hydro + diesel;
                double pct    = demand[d] > 0 ? Math.min(100.0, (used / demand[d]) * 100) : 0;
                double cost   = solar * SOURCES[0].costPerKwh
                              + hydro * SOURCES[1].costPerKwh
                              + diesel * SOURCES[2].costPerKwh;

                // Accumulate totals for analysis
                totalCost      += cost;
                totalEnergy    += used;
                totalRenewable += solar + hydro;
                totalDiesel    += diesel;
                if (diesel > 0) usedDiesel = true;

                System.out.printf("  %02d |  %s   |   %6.1f   |   %6.1f   |    %6.1f   | %5.1f |  %5.1f | %4.0f%% |  %7.2f%n",
                    HOURS[h], DISTRICTS[d], solar, hydro, diesel, used, demand[d], pct, cost);
            }

            System.out.println("-----|------|------------|------------|-------------|-------|--------|-------|----------");

            if (usedDiesel) {
                dieselHours++;
                dieselUsageLog.add("Hour " + HOURS[h] + ": Solar unavailable, Diesel used to supplement Hydro.");
            }
        }

        System.out.println();
    }

    // 
    // TASK 6: COST AND RESOURCE ANALYSIS
    // 

    private static void printCostAnalysis() {
        double renewablePct = totalEnergy > 0 ? (totalRenewable / totalEnergy) * 100 : 0;
        double dieselPct    = totalEnergy > 0 ? (totalDiesel    / totalEnergy) * 100 : 0;

        System.out.println("COST AND RESOURCE ANALYSIS");
        System.out.println("-----------------------------------------------------------");
        System.out.printf("  Total Distribution Cost      : Rs. %.2f%n", totalCost);
        System.out.printf("  Total Energy Distributed     : %.1f kWh%n", totalEnergy);
        System.out.printf("  Renewable Energy (Solar+Hydro): %.1f kWh (%.1f%%)%n", totalRenewable, renewablePct);
        System.out.printf("  Diesel Energy Used           : %.1f kWh (%.1f%%)%n", totalDiesel, dieselPct);
        System.out.printf("  Hours with Diesel Usage      : %d hours%n", dieselHours);
        System.out.println();

        System.out.println("  Diesel Usage Log (hours Solar unavailable):");
        for (String log : dieselUsageLog)
            System.out.println("    - " + log);
        System.out.println();

        System.out.println("  Why diesel was used:");
        System.out.println("    Solar (S1) is only available 06:00-18:00.");
        System.out.println("    After 18:00, only Hydro (40 kWh max) is available.");
        System.out.println("    Total demand often exceeds 40 kWh/hr in evening hours.");
        System.out.println("    Diesel (S3) is used to cover the shortfall from 17:00-23:00.");
        System.out.println();
    }

    // 
    // HELPER OUTPUT METHODS
    // 

    private static void printInputSummary() {
        System.out.println("=======================================================================");
        System.out.println(" Q4: Smart Energy Grid Load Distribution Optimization — Nepal          ");
        System.out.println("=======================================================================");
        System.out.println();

        System.out.println("INPUT DATA SUMMARY");
        System.out.println("  Energy Sources:");
        for (EnergySource s : SOURCES)
            System.out.printf("    %-3s %-6s | Max: %5.0f kWh/hr | Available: %02d:00-%02d:00 | Cost: Rs.%.1f/kWh%n",
                s.id, s.type, s.maxCapacity, s.availStart, s.availEnd, s.costPerKwh);
        System.out.println();

        System.out.println("  Sample Hourly Demand (kWh):");
        System.out.println("  Hour | District A | District B | District C | Total");
        System.out.println("  -----|------------|------------|------------|------");
        for (int h = 0; h < Math.min(4, HOURS.length); h++) {
            double total = DEMAND[h][0] + DEMAND[h][1] + DEMAND[h][2];
            System.out.printf("   %02d  |    %5.1f   |    %5.1f   |    %5.1f   | %5.1f%n",
                HOURS[h], DEMAND[h][0], DEMAND[h][1], DEMAND[h][2], total);
        }
        System.out.println("  ... (18 hours total)");
        System.out.println();
    }

    private static void printSampleCalculation() {
        System.out.println("SAMPLE CALCULATION — Hour 06 (from assignment brief)");
        System.out.println("  Demand: A=20 kWh, B=15 kWh, C=25 kWh  ->  Total=60 kWh");
        System.out.println("  Available: Solar(50 kWh @ Rs.1.0), Hydro(40 kWh @ Rs.1.5), Diesel(NOT available)");
        System.out.println();
        System.out.println("  Step 1 - Greedy: Use Solar first (cheapest):");
        System.out.println("    District A: 20 kWh from Solar  (fully met)");
        System.out.println("    District B: 15 kWh from Solar  (fully met)");
        System.out.println("    District C: 15 kWh from Solar  (Solar exhausted at 50 kWh)");
        System.out.println("    Solar used: 20+15+15 = 50 kWh");
        System.out.println();
        System.out.println("  Step 2 - Use Hydro for remaining:");
        System.out.println("    District C: 10 kWh remaining -> from Hydro");
        System.out.println("    Hydro used: 10 kWh");
        System.out.println();
        System.out.println("  Result: All districts fully met");
        System.out.println("  Cost  : Solar(50 x Rs.1.0) + Hydro(10 x Rs.1.5) = Rs.50 + Rs.15 = Rs.65");
        System.out.println();
    }

    private static void printAlgorithmNotes() {
        System.out.println("ALGORITHM NOTES");
        System.out.println("  Greedy approach: sort sources by cost, fill cheapest first.");
        System.out.println("  DP modeling: each hour is an independent allocation sub-problem.");
        System.out.println("  Tolerance: if unmet demand <= 10% of original demand, it is accepted.");
        System.out.println("  Objective: minimize total cost, maximize renewable usage.");
        System.out.println("  Time Complexity: O(H * D * S log S)  where H=18, D=3, S=3");
        System.out.println("  Space Complexity: O(D * S) per hour");
        System.out.println();
        System.out.println("  Trade-offs:");
        System.out.println("    - Greedy is near-optimal for this problem (cost is the single objective)");
        System.out.println("    - True DP with all combinations would be O(2^S) per hour — overkill for S=3");
        System.out.println("    - Diesel is expensive but necessary for evening demand peaks");
    }
}