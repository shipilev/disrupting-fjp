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

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ForkJoinRecursive {

     /*
      The fork-join task below recursively divides the work on slices,
      with some sensible decomposition threshold.
     */

    static class PiForkJoinTask extends RecursiveTask<Double> {
        private final int slices;

        public PiForkJoinTask(int slices) {
            this.slices = slices;
        }

        @Override
        protected Double compute() {
            if (slices < 10000) {
                double acc = 0D;
                for (int s = 0; s < slices; s++) {
                    acc += Shared.calculatePi(s);
                }
                return acc;
            }

            int lslices = slices / 2;
            int rslices = slices - lslices;
            PiForkJoinTask t1 = new PiForkJoinTask(lslices);
            PiForkJoinTask t2 = new PiForkJoinTask(rslices);

            ForkJoinTask.invokeAll(t1, t2);

            return t1.join() + t2.join();
        }
    }

    @GenerateMicroBenchmark
    public double run() throws InterruptedException {
        return new PiForkJoinTask(Shared.SLICES).invoke();
    }

}
