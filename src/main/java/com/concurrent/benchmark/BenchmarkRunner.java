package com.concurrent.benchmark;

import com.concurrent.datastructures.ConcurrentSet;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class BenchmarkRunner {

    private final Supplier<ConcurrentSet> dsFactory;
    private final int totalElements;
    private final int prefillPercentage;
    private final int containsPercent;
    private final int insertPercent;
    private final int numThreads;
    private final int durationSeconds;
    private final int numRuns;

    // Constructor for single run (backward compatible)
    public BenchmarkRunner(
            ConcurrentSet dataStructure,
            int totalElements,
            int prefillPercentage,
            int containsPercent,
            int insertPercent,
            int numThreads,
            int durationSeconds
    ) {
        this(() -> dataStructure, totalElements, prefillPercentage,
             containsPercent, insertPercent, numThreads, durationSeconds, 1);
    }

    // Constructor with factory for multiple runs
    public BenchmarkRunner(
            Supplier<ConcurrentSet> dsFactory,
            int totalElements,
            int prefillPercentage,
            int containsPercent,
            int insertPercent,
            int numThreads,
            int durationSeconds,
            int numRuns
    ) {
        this.dsFactory = dsFactory;
        this.totalElements = totalElements;
        this.prefillPercentage = prefillPercentage;
        this.containsPercent = containsPercent;
        this.insertPercent = insertPercent;
        this.numThreads = numThreads;
        this.durationSeconds = durationSeconds;
        this.numRuns = numRuns;
    }

    public AggregateMetrics runMultiple() throws InterruptedException {
        double[] throughputs = new double[numRuns];
        long[] totalOps = new long[numRuns];
        double[] durations = new double[numRuns];

        for (int i = 0; i < numRuns; i++) {
            ConcurrentSet ds = dsFactory.get();
            PerformanceMetrics result = runSingle(ds);
            
            throughputs[i] = result.getThroughput();
            totalOps[i] = result.getTotalOps();
            durations[i] = result.getSeconds();
            
            System.out.printf("  Run %d: %.2f ops/sec%n", i + 1, result.getThroughput());
            
            Thread.sleep(500);
            System.gc();
        }

        double avgThroughput = average(throughputs);
        double stdDev = standardDeviation(throughputs, avgThroughput);
        double minThroughput = min(throughputs);
        double maxThroughput = max(throughputs);

        return new AggregateMetrics(
            numRuns, avgThroughput, stdDev, minThroughput, maxThroughput,
            throughputs, totalOps, durations
        );
    }

    public PerformanceMetrics run() throws InterruptedException {
        return runSingle(dsFactory.get());
    }

    private PerformanceMetrics runSingle(ConcurrentSet dataStructure) throws InterruptedException {
        // 1) Prefill
        int prefillCount = (totalElements * prefillPercentage) / 100;
        for (int i = 0; i < prefillCount; i++) {
            dataStructure.insert(i);
        }

        // 2) Setup
        AtomicLong opCount = new AtomicLong(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        long durationNanos = durationSeconds * 1_000_000_000L;

        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    endLatch.countDown();
                    return;
                }

                long endTime = System.nanoTime() + durationNanos;
                long localOps = 0;

                while (System.nanoTime() < endTime) {
                    int key = rnd.nextInt(totalElements);
                    int op = rnd.nextInt(100);

                    if (op < containsPercent) {
                        dataStructure.contains(key);
                    } else if (op < containsPercent + insertPercent) {
                        dataStructure.insert(key);
                    } else {
                        dataStructure.remove(key);
                    }
                    localOps++;
                }
                
                opCount.addAndGet(localOps);
                endLatch.countDown();
            });
            threads[i].start();
        }

        long start = System.nanoTime();
        startLatch.countDown();
        endLatch.await();
        long end = System.nanoTime();

        long totalOps = opCount.get();
        double actualSeconds = (end - start) / 1_000_000_000.0;
        double throughput = totalOps / actualSeconds;

        return new PerformanceMetrics(totalOps, actualSeconds, throughput);
    }

    private double average(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    private double standardDeviation(double[] values, double mean) {
        double sumSquares = 0;
        for (double v : values) {
            sumSquares += (v - mean) * (v - mean);
        }
        return Math.sqrt(sumSquares / values.length);
    }

    private double min(double[] values) {
        double m = values[0];
        for (double v : values) if (v < m) m = v;
        return m;
    }

    private double max(double[] values) {
        double m = values[0];
        for (double v : values) if (v > m) m = v;
        return m;
    }

    // Aggregate metrics class for multiple runs
    public static class AggregateMetrics {
        private final int numRuns;
        private final double avgThroughput;
        private final double stdDev;
        private final double minThroughput;
        private final double maxThroughput;
        private final double[] allThroughputs;
        private final long[] allTotalOps;
        private final double[] allDurations;

        public AggregateMetrics(int numRuns, double avgThroughput, double stdDev,
                                double minThroughput, double maxThroughput,
                                double[] allThroughputs, long[] allTotalOps, double[] allDurations) {
            this.numRuns = numRuns;
            this.avgThroughput = avgThroughput;
            this.stdDev = stdDev;
            this.minThroughput = minThroughput;
            this.maxThroughput = maxThroughput;
            this.allThroughputs = allThroughputs;
            this.allTotalOps = allTotalOps;
            this.allDurations = allDurations;
        }

        public int getNumRuns() { return numRuns; }
        public double getAvgThroughput() { return avgThroughput; }
        public double getStdDev() { return stdDev; }
        public double getMinThroughput() { return minThroughput; }
        public double getMaxThroughput() { return maxThroughput; }
        public double[] getAllThroughputs() { return allThroughputs; }
        public long[] getAllTotalOps() { return allTotalOps; }
        public double[] getAllDurations() { return allDurations; }

        @Override
        public String toString() {
            return String.format(
                "Runs=%d, Avg=%.2f ops/sec, StdDev=%.2f, Min=%.2f, Max=%.2f",
                numRuns, avgThroughput, stdDev, minThroughput, maxThroughput
            );
        }
    }
}