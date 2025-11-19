package com.concurrent.benchmark;

public class Workload {

    public final int containsPercent;
    public final int insertPercent;
    public final int deletePercent;

    public Workload(int containsPercent, int insertPercent, int deletePercent) {
        this.containsPercent = containsPercent;
        this.insertPercent = insertPercent;
        this.deletePercent = deletePercent;
    }

    @Override
    public String toString() {
        return containsPercent + "C-" + insertPercent + "I-" + deletePercent + "D";
    }
}
