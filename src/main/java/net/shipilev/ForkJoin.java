package net.shipilev;

import org.openjdk.jmh.annotations.Benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class ForkJoin extends Workload {

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
                acc += doCalculatePi(s);
            }
            return acc;
        }
    }

    @Benchmark
    public double run() throws InterruptedException {
        final List<PiForkJoinTask> tasks = new ArrayList<PiForkJoinTask>();
        final int ts = getThreads();
        final int slicePerThread = getSlices() / ts;
        for (int i = 0; i < ts; i++) {
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
