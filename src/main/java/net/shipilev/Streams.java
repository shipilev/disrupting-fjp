package net.shipilev;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Warmup(iterations = 3)
@Measurement(iterations = 10)
@Fork(5)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Streams {

    @Benchmark
    public double run() {
        return IntStream.range(0, Shared.SLICES)
                    .parallel()
                    .mapToDouble(Shared::calculatePi)
                    .reduce(0, Double::sum);
    }

}
