package net.shipilev;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ForkJoinReuse {

     /*
      The fork-join task below is used as "just" the Callable,
      and "reuses" the submitted tasks.
     */

    static class PiForkJoinTask extends RecursiveTask<Double> {
        private int slice;

        @Override
        protected Double compute() {
            return Shared.calculatePi(slice);
        }
    }

    @GenerateMicroBenchmark
    public double run() throws InterruptedException {
        List<PiForkJoinTask> tasks = new ArrayList<>();

        int stride = Shared.THREADS * 100;
        for (int i = 0; i < stride; i++) {
            PiForkJoinTask task = new PiForkJoinTask();
            task.slice = i;
            task.fork();
            tasks.add(task);
        }

        double acc = 0D;
        int s = stride;
        while (s < Shared.SLICES) {
            for (PiForkJoinTask task : tasks) {
                acc += task.join();
                task.reinitialize();
                task.slice = s;
                task.fork();
            }
            s += stride;
        }

        for (PiForkJoinTask task : tasks) {
            acc += task.join();
        }

        return acc;
    }

}
