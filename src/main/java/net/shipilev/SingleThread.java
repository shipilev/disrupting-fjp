package net.shipilev;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SingleThread {

    @Benchmark
    public double run() {
        double acc = 0;
        for (int s = 0; s < Shared.SLICES; s++) {
            acc += Shared.calculatePi(s);
        }
        return acc;
    }

}
