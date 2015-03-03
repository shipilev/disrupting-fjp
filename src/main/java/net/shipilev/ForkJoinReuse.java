package net.shipilev;

import org.openjdk.jmh.annotations.Benchmark;

import java.util.concurrent.RecursiveTask;

public class ForkJoinReuse extends Workload {

     /*
      The fork-join task below is used as "just" the Callable,
      and "reuses" the submitted tasks.
     */

    class PiForkJoinTask extends RecursiveTask<Double> {
        private int slice;

        @Override
        protected Double compute() {
            return doCalculatePi(slice);
        }
    }

    @Benchmark
    public double run() throws InterruptedException {
        final int stride = getThreads() * 100;
        final PiForkJoinTask[] tasks = new PiForkJoinTask[stride];
        for (int i = 0; i < stride; i++) {
            PiForkJoinTask task = new PiForkJoinTask();
            task.slice = i;
            task.fork();
            tasks[i] = task;
        }
        double acc = 0;
        final int s1 = getSlices() / stride;
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
        final int s2 = getSlices() % stride;
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
