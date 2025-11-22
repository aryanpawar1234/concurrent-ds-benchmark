# Concurrent Data Structures Benchmark

A comprehensive performance analysis of concurrent data structures in Java, comparing tree-based and hash-based implementations under various workload conditions.

## 📋 Overview

This project implements and benchmarks five concurrent data structures:

| Data Structure | Type | Synchronization Strategy |
|----------------|------|--------------------------|
| **Concurrent BST** | Tree | Fine-grained hand-over-hand locking |
| **Concurrent AVL Tree** | Tree | Optimistic reads, fine-grained writes |
| **Concurrent Treap** | Tree | Fine-grained locking with random priorities |
| **Striped HashSet** | Hash | Lock striping (fixed locks) |
| **Refinable HashSet** | Hash | Lock striping + dynamic resizing |

## 🎯 Objectives

- Implement thread-safe `contains`, `insert`, and `remove` operations
- Evaluate performance with **1 million nodes** and **50% prefill**
- Test scalability with thread counts: 1, 2, 4, 6, 8, 10, 12, 14, 16
- Analyze behavior under different workload distributions
- Compare tree-based vs hash-based structure performance

## 📊 Workload Configurations

| Workload | Contains % | Insert % | Delete % | Description |
|----------|------------|----------|----------|-------------|
| 100C-0I-0D | 100 | 0 | 0 | Read-only |
| 90C-9I-1D | 90 | 9 | 1 | Read-heavy |
| 50C-25I-25D | 50 | 25 | 25 | Mixed |
| 30C-35I-35D | 30 | 35 | 35 | Write-heavy |
| 0C-50I-50D | 0 | 50 | 50 | Write-only |

## 🏆 Key Results

### Peak Performance

| Rank | Data Structure | Peak Throughput | Best Workload | Threads |
|------|----------------|-----------------|---------------|---------|
| 1 | Refinable HashSet | 4.57M ops/sec | 100C-0I-0D | 16 |
| 2 | AVL Tree | 3.91M ops/sec | 100C-0I-0D | 1 |
| 3 | Treap | 3.88M ops/sec | 100C-0I-0D | 1 |
| 4 | BST | 2.03M ops/sec | 100C-0I-0D | 16 |
| 5 | Striped HashSet | 714K ops/sec | 100C-0I-0D | 2 |

### Scalability (16 threads / 1 thread)

| Data Structure | Scalability Factor |
|----------------|-------------------|
| Refinable HashSet | 8.5x |
| BST | 2.3x |
| Striped HashSet | 10.1x |
| AVL Tree | 0.67x (negative) |
| Treap | 0.68x (negative) |

## 🏗️ Project Structure

```
concurrent-ds-benchmark/
├── results/                            # Benchmark output CSV files
│   ├── AVL.csv
│   ├── BST.csv
│   ├── BST_100C-0I-0D.csv
│   ├── Refinable.csv
│   ├── Striped.csv
│   └── Treap.csv
├── scripts/
│   ├── GraphGenerator.py               # Python script for visualizations
│   └── perf_wrapper.sh                 # Performance monitoring script
├── src/main/java/com/concurrent/
│   ├── benchmark/
│   │   ├── BenchmarkRunner.java        # Benchmark execution logic
│   │   ├── Main.java                   # Entry point
│   │   ├── PerformanceMetrics.java     # Results container
│   │   └── Workload.java               # Workload configuration
│   ├── datastructures/
│   │   ├── ConcurrentSet.java          # Common interface
│   │   ├── ConcurrentAVL.java          # AVL Tree
│   │   ├── ConcurrentBST.java          # Binary Search Tree
│   │   ├── ConcurrentTreap.java        # Treap
│   │   ├── RefinableHashSet.java       # Refinable HashSet
│   │   └── StripedHashSet.java         # Striped HashSet
│   └── util/
│       └── CSVWriterUtil.java          # CSV output utility
├── target/                             # Compiled classes
├── .gitignore
├── LICENSE
├── pom.xml
├── README.md
├── ReportTemplate.md
├── run_cloud.sh                        # Cloud execution script
├── run_local.sh                        # Local execution script
└── setup_project.sh                    # Project setup script
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build

```bash
mvn clean compile
```

### Run Benchmark

```bash
mvn exec:java -Dexec.mainClass="com.concurrent.benchmark.Main"
```

Or with custom JVM options:

```bash
java -cp target/classes -Xmx8g -Xms4g -XX:+UseG1GC com.concurrent.benchmark.Main
```

### Run Specific Data Structure

Modify `Main.java` to select specific data structures:

```java
String[] dsNames = {"BST", "AVL", "Treap", "Striped", "Refinable"};
```

## ⚙️ Configuration

Edit `Main.java` to customize benchmark parameters:

```java
private static final int TOTAL_ELEMENTS = 1_000_000;
private static final int PREFILL_PERCENT = 50;
private static final int DURATION_SECONDS = 10;
private static final int NUM_RUNS = 3;
private static final int[] THREAD_COUNTS = {1, 2, 4, 6, 8, 10, 12, 14, 16};
```

## 📈 Results

Results are saved to `results/` directory as CSV files:

```
DataStructure,Workload,Threads,Run,TotalOps,Seconds,ThroughputOpsPerSec
AVL,100C-0I-0D,1,1,39122818,9.999946,3912302.79
AVL,100C-0I-0D,2,1,26309200,9.999829,2630964.96
...
```

## 🔍 Key Findings

1. **Refinable HashSet** shows best scalability (8.5x at 16 threads) due to lock striping and dynamic resizing

2. **AVL & Treap** achieve highest single-thread throughput (~3.9M ops/sec) but show negative scaling due to root contention

3. **BST with fine-grained locking** scales well (2.3x) using hand-over-hand locking

4. **Read-heavy workloads** consistently outperform write-heavy workloads across all structures

5. **Tree structures** suffer from root contention, limiting multi-threaded performance

## 📖 Implementation Highlights

### Hand-over-Hand Locking (BST)
```java
// Lock parent → Lock child → Unlock parent → Move down
curr.lock();
parent.unlock();
parent = curr;
curr = next;
```

### Lock Striping (Refinable HashSet)
```java
private ReentrantLock getLock(int bucketIndex) {
    return locks[bucketIndex % numLocks];
}
```

## 📝 License

This project is for educational purposes as part of High Performance Computing coursework.

## 👤 Author

**Aryan Pawar**  
Roll Number: SE22UARI195