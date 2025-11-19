package com.concurrent.benchmark;

public class PerformanceMetrics {

    private final long totalOps;
    private final double seconds;
    private final double throughput;

    public PerformanceMetrics(long totalOps, double seconds, double throughput) {
        this.totalOps = totalOps;
        this.seconds = seconds;
        this.throughput = throughput;
    }

    public long getTotalOps() {
        return totalOps;
    }

    public double getSeconds() {
        return seconds;
    }

    public double getThroughput() {
        return throughput;
    }
}
