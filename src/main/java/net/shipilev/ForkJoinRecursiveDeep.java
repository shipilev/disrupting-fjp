package net.shipilev;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@Fork(1)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ForkJoinRecursiveDeep {

    /*
      The fork-join task below deeply recurses, up until the leaf
      contains a single slice.
     */

    static class PiForkJoinTask extends RecursiveTask<Double> {
        private final int from;
        private final int to;

        public PiForkJoinTask(final int from, final int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        protected Double compute() {
            final int slices = to - from;
            if (slices <= 1) {
                double acc = 0;
                for (int s = from; s < to; s++) {
                    acc += Shared.calculatePi(s);
                }
                return acc;
            }
            final int mid = from + slices / 2;
            PiForkJoinTask t1 = new PiForkJoinTask(from, mid);
            PiForkJoinTask t2 = new PiForkJoinTask(mid, to);
            ForkJoinTask.invokeAll(t1, t2);
            return t1.join() + t2.join();
        }
    }

    @Benchmark
    public double run() throws InterruptedException {
        return new PiForkJoinTask(0, Shared.SLICES).invoke();
    }

}
