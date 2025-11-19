#!/bin/bash

echo "=== Starting Concurrent Data Structures Benchmark ==="
echo "Start time: $(date)"

mvn -q clean compile
echo "Build done."

mkdir -p results
mkdir -p perf_results
mkdir -p graphs

echo "Running benchmark..."

java -cp target/classes com.concurrent.Main

echo "Benchmarks complete."
echo "Generating graphs..."

python3 scripts/GraphGenerator.py

echo "All graphs saved in graphs/"

echo "Download results with:"
echo "gcloud compute scp --recurse concurrent-benchmark-india:~/concurrent-ds-benchmark/results ./results --zone=asia-south1-a"
echo "gcloud compute scp --recurse concurrent-benchmark-india:~/concurrent-ds-benchmark/graphs ./graphs --zone=asia-south1-a"

echo "=== Benchmark Complete ==="
echo "End time: $(date)"
