import os
import pandas as pd
import matplotlib.pyplot as plt

RESULTS_DIR = "results"
GRAPH_DIR = "graphs"

if not os.path.exists(GRAPH_DIR):
    os.makedirs(GRAPH_DIR)

# Iterate over all CSV files in results/
for filename in os.listdir(RESULTS_DIR):
    if not filename.endswith(".csv"):
        continue

    file_path = os.path.join(RESULTS_DIR, filename)
    print(f"Processing {file_path}")

    # Extract metadata from file name
    # Example: ConcurrentAVL_90C-9I-1D.csv
    parts = filename.replace(".csv", "").split("_")
    ds_name = parts[0]
    workload = parts[1]

    # Load CSV
    df = pd.read_csv(file_path)

    # Compute average throughput for each thread count
    avg_df = df.groupby("threads")["throughput_ops_per_sec"].mean().reset_index()

    # PLOT
    plt.figure(figsize=(10, 6))
    plt.plot(
        avg_df["threads"],
        avg_df["throughput_ops_per_sec"],
        marker="o",
        linewidth=2
    )

    plt.title(f"{ds_name} — {workload}")
    plt.xlabel("Thread Count")
    plt.ylabel("Throughput (ops/sec)")
    plt.grid(True)

    # save
    out_path = os.path.join(GRAPH_DIR, f"{ds_name}_{workload}.png")
    plt.savefig(out_path)
    plt.close()

    print(f"Saved graph to {out_path}")

print("Graph generation completed.")
