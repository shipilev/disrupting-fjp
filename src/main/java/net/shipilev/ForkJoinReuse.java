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

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@Fork(1)
@BenchmarkMode(Mode.SingleShotTime)
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

    @Benchmark
    public double run() throws InterruptedException {
        final int stride = Shared.THREADS * 100;
        final PiForkJoinTask[] tasks = new PiForkJoinTask[stride];
        for (int i = 0; i < stride; i++) {
            PiForkJoinTask task = new PiForkJoinTask();
            task.slice = i;
            task.fork();
            tasks[i] = task;
        }
        double acc = 0;
        final int s1 = Shared.SLICES / stride;
        for (int i = 1; i < s1; i++) {
            for (PiForkJoinTask task : tasks) {
                acc += task.join();
                task.reinitialize();
                task.slice += stride;
                task.fork();
            }
        }
        for (PiForkJoinTask task : tasks) {
            acc += task.join();
        }
        final int s2 = Shared.SLICES % stride;
        for (int i = 0; i < s2; i++) {
            final PiForkJoinTask task = tasks[i];
            task.reinitialize();
            task.slice += stride;
            task.fork();
        }
        for (int i = 0; i < s2; i++) {
            acc += tasks[i].join();
        }
        return acc;
    }

}
