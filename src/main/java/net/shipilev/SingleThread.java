package net.shipilev;

import org.openjdk.jmh.annotations.Benchmark;

public class SingleThread extends Workload {

    @Benchmark
    public double run() {
        double acc = 0;
        for (int s = 0; s < getSlices(); s++) {
            acc += doCalculatePi(s);
        }
        return acc;
    }

}
