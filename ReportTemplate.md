# Concurrent Data Structures Performance Report

## Student Details
**Name:**  
**Roll Number:**  
**Course:** Concurrent Programming  

---

# 1. Introduction
This report evaluates the performance of five concurrent data structures:

1. Concurrent Binary Search Tree  
2. Concurrent AVL Tree  
3. Concurrent Treap  
4. Concurrent Striped HashSet  
5. Concurrent Refinable HashSet  

All benchmarks were executed on a cloud VM with:

- 8 vCPUs  
- 100GB disk  
- Java 17  
- Ubuntu 22.04 LTS  

The goal is to measure throughput and cache efficiency under varying workloads and thread counts.

---

# 2. Benchmarking Methodology

### **Parameters**
- Total elements: **1,000,000**
- Prefill: **50%**
- Duration per run: **10 seconds**
- Thread counts:  
  `1, 2, 4, 6, 8, 10, 12, 14, 16`
- Number of runs per config: **3**
- Workloads:
  - **100C-0I-0D**
  - **90C-9I-1D**
  - **50C-25I-25D**
  - **30C-35I-35D**
  - **0C-50I-50D**

### **Metrics Recorded**
- Total operations  
- Duration  
- Throughput (ops/sec)  
- Cache misses per 1000 ops (via `perf stat -e cache-misses,cycles,branches`)  

---

# 3. Results

Graphs generated automatically via `GraphGenerator.py`.

## **3.1 Throughput Graphs**

*(Insert PNGs here)*

For each structure and workload:



