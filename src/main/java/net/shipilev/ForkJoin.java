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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ForkJoin {

     /*
      The fork-join task below is used as "just" the Callable.
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
            double acc = 0;
            for (int s = from; s < to; s++) {
                acc += Shared.calculatePi(s);
            }
            return acc;
        }
    }

    @Benchmark
    public double run() throws InterruptedException {
        final List<PiForkJoinTask> tasks = new ArrayList<PiForkJoinTask>();
        final int slicePerThread = Shared.SLICES / Shared.THREADS;
        for (int i = 0; i < Shared.THREADS; i++) {
            PiForkJoinTask task = new PiForkJoinTask(i * slicePerThread, (i + 1) * slicePerThread);
            task.fork();
            tasks.add(task);
        }
        double acc = 0;
        for (PiForkJoinTask task : tasks) {
            acc += task.join();
        }
        return acc;
    }

}
