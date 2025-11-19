package com.concurrent.benchmark;

import com.concurrent.datastructures.*;
import com.concurrent.util.CSVWriterUtil;

public class Main {

    private static final int TOTAL_ELEMENTS = 1_000_000;
    private static final int PREFILL_PERCENT = 50;
    private static final int DURATION_SECONDS = 10;

    private static final int[] THREADS = {1, 2, 4, 6, 8, 10, 12, 14, 16};

    private static final Workload[] WORKLOADS = {
            new Workload(100, 0, 0),
            new Workload(90, 9, 1),
            new Workload(50, 25, 25),
            new Workload(30, 35, 35),
            new Workload(0, 50, 50)
    };

    private static ConcurrentSet getDS(String name) {
        switch (name) {
            case "BST": return new ConcurrentBST();
            case "AVL": return new ConcurrentAVL();
            case "Treap": return new ConcurrentTreap();
            case "Striped": return new StripedHashSet(1024);
            case "Refinable": return new RefinableHashSet(1024);
            default: throw new IllegalArgumentException("Unknown DS: " + name);
        }
    }

    public static void main(String[] args) throws Exception {

        String[] structures = {"BST", "AVL", "Treap", "Striped", "Refinable"};

        for (String dsName : structures) {
            for (Workload wl : WORKLOADS) {

                String csvFile = "results/" + dsName + "_" + wl + ".csv";
                CSVWriterUtil csv = new CSVWriterUtil(csvFile);
                csv.writeHeader("Threads,TotalOps,Seconds,ThroughputOpsPerSec");

                for (int t : THREADS) {

                    System.out.println("Running " + dsName + " / " + wl + " / " + t + " threads");

                    BenchmarkRunner runner =
                            new BenchmarkRunner(
                                    getDS(dsName),
                                    TOTAL_ELEMENTS,
                                    PREFILL_PERCENT,
                                    wl.containsPercent,
                                    wl.insertPercent,
                                    t,
                                    DURATION_SECONDS
                            );

                    PerformanceMetrics metrics = runner.run();

                    csv.writeRow(String.format("%d,%d,%.6f,%.2f",
                            t,
                            metrics.getTotalOps(),
                            metrics.getSeconds(),
                            metrics.getThroughput()
                    ));
                }
            }
        }

        System.out.println("=== FULL BENCHMARK COMPLETE (225 runs, 1 run each) ===");
    }
}
