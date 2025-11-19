#!/bin/bash

# Usage:
# ./perf_wrapper.sh <DataStructureName> <WorkloadName> <Threads>

DS_NAME=$1
WORKLOAD=$2
THREADS=$3

OUTPUT_DIR="perf_results"
mkdir -p $OUTPUT_DIR

OUT_FILE="${OUTPUT_DIR}/${DS_NAME}_${WORKLOAD}_${THREADS}.txt"

echo "Running perf for $DS_NAME $WORKLOAD Threads=$THREADS"
echo "Output → $OUT_FILE"

sudo perf stat \
   -e cache-misses,cycles,branches,branch-misses \
   java -cp target/classes com.concurrent.Main \
   --ds $DS_NAME --workload $WORKLOAD --threads $THREADS \
   1> /dev/null \
   2> "$OUT_FILE"

echo "Done."
