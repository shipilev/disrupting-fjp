package net.shipilev;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
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
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ForkJoin {

     /*
      The fork-join task below is used as "just" the Callable.
     */

    static class PiForkJoinTask extends RecursiveTask<Double> {
        private final int slices;

        public PiForkJoinTask(int slices) {
            this.slices = slices;
        }

        @Override
        protected Double compute() {
            double acc = 0D;
            for (int s = 0; s < slices; s++) {
                acc += Shared.calculatePi(s);
            }
            return acc;
        }
    }

    @GenerateMicroBenchmark
    public double run() throws InterruptedException {
        List<PiForkJoinTask> tasks = new ArrayList<PiForkJoinTask>();
        for (int i = 0; i < Shared.THREADS; i++) {
            PiForkJoinTask task = new PiForkJoinTask(Shared.SLICES / Shared.THREADS);
            task.fork();
            tasks.add(task);
        }

        double acc = 0D;
        for (PiForkJoinTask task : tasks) {
            acc += task.join();
        }

        return acc;
    }

}
