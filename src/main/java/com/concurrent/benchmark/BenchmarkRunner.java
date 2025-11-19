package com.concurrent.benchmark;

import com.concurrent.datastructures.ConcurrentSet;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class BenchmarkRunner {

    private final ConcurrentSet dataStructure;
    private final int totalElements;
    private final int prefillPercentage;
    private final int containsPercent;
    private final int insertPercent;
    private final int numThreads;
    private final int durationSeconds;

    public BenchmarkRunner(
            ConcurrentSet dataStructure,
            int totalElements,
            int prefillPercentage,
            int containsPercent,
            int insertPercent,
          
            int numThreads,
            int durationSeconds
    ) {
        this.dataStructure = dataStructure;
        this.totalElements = totalElements;
        this.prefillPercentage = prefillPercentage;
        this.containsPercent = containsPercent;
        this.insertPercent = insertPercent;
        this.numThreads = numThreads;
        this.durationSeconds = durationSeconds;
    }

    public PerformanceMetrics run() throws InterruptedException {
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
        long endTime = System.nanoTime() + durationNanos;

        Runnable worker = () -> {
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                endLatch.countDown();
                return;
            }

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
                opCount.incrementAndGet();
            }

            endLatch.countDown();
        };

        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(worker);
            t.start();
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
}
