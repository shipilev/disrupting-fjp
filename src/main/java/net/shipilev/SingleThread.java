package net.shipilev;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3)
@Measurement(iterations = 10)
@Fork(5)
@BenchmarkMode(Mode.SingleShotTime)
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
