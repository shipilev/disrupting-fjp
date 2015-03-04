package net.shipilev;

import org.openjdk.jmh.annotations.Benchmark;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ForkJoinRecursiveDeep extends Workload {

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
                    acc += doCalculatePi(s);
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
        return new PiForkJoinTask(0, getSlices()).invoke();
    }

}
