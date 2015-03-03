package net.shipilev;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(5)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Workload {

    @Param({"500", "1000", "5000", "10000", "50000"})
    public int slicesK;

    @Param({"10"})
    public int workMult;

    @Param("0")
    public int threads;

    int getThreads() {
        if (threads == 0) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            return threads;
        }
    }

    int getSlices() {
        return slicesK * 1000;
    }

    public double doCalculatePi(final int sliceNr) {
        final int from = sliceNr * workMult;
        final int to = from + workMult;
        final int c = (to << 1) + 1;
        double acc = 0;
        for (int a = 4 - ((from & 1) << 3), b = (from << 1) + 1; b < c; a = -a, b += 2) {
            acc += ((double) a) / b;
        }
        return acc;
    }

}
