#!/bin/bash

echo "== Creating directory structure =="

mkdir -p src/main/java/com/concurrent/datastructures
mkdir -p src/main/java/com/concurrent/benchmark
mkdir -p src/main/java/com/concurrent/util
mkdir -p scripts results graphs perf_results

echo "== Creating Java source files =="

touch src/main/java/com/concurrent/datastructures/ConcurrentSet.java
touch src/main/java/com/concurrent/datastructures/ConcurrentBST.java
touch src/main/java/com/concurrent/datastructures/ConcurrentAVL.java
touch src/main/java/com/concurrent/datastructures/ConcurrentTreap.java
touch src/main/java/com/concurrent/datastructures/StripedHashSet.java
touch src/main/java/com/concurrent/datastructures/RefinableHashSet.java

touch src/main/java/com/concurrent/benchmark/Workload.java
touch src/main/java/com/concurrent/benchmark/Runner.java
touch src/main/java/com/concurrent/benchmark/Benchmark.java

touch src/main/java/com/concurrent/util/CSVWriterUtil.java

echo "== Creating helper + scripts =="

touch scripts/GraphGenerator.py
touch scripts/perf_wrapper.sh
touch run_cloud.sh
touch run_local.sh

echo "== Creating documentation files =="

touch README.md
touch LICENSE
touch ReportTemplate.md
touch .gitignore

echo "== Project setup completed successfully =="
