package net.shipilev;

import org.openjdk.jmh.annotations.Benchmark;

import java.util.stream.IntStream;

public class Streams extends Workload {

    @Benchmark
    public double run() {
        return IntStream.range(0, getSlices())
                    .parallel()
                    .mapToDouble(this::doCalculatePi)
                    .reduce(0, Double::sum);
    }

}
