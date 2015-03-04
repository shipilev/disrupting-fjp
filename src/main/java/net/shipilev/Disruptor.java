package net.shipilev;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Disruptor extends Workload {

    private ExecutorService executor;
    private com.lmax.disruptor.dsl.Disruptor<PiJob> disruptor;
    private RingBuffer<PiJob> ringBuffer;
    private PiResultReclaimer result;

    public static class PiJob {
        public double result;
        public int sliceNr;
        public int partitionId;

        public void calculatePi() {
            result = doCalculatePi(sliceNr);
        }
    }

    public static class PiEventFac implements EventFactory<PiJob> {

        @Override
        public PiJob newInstance() {
            return new PiJob();
        }
    }

    public static class PiEventProcessor implements EventHandler<PiJob> {
        private final int partionId;

        public PiEventProcessor(final int partionId) {
            this.partionId = partionId;
        }

        @Override
        public void onEvent(final PiJob event, final long sequence, final boolean isEndOfBatch) throws Exception {
            if (partionId == event.partitionId) {
                event.calculatePi();
            }
        }
    }

    public static class PiResultReclaimer implements EventHandler<PiJob> {
        private double result;
        private long seq;
        private final int numSlice;
        private final CountDownLatch latch;

        public PiResultReclaimer(final int numSlice) {
            this.numSlice = numSlice;
            latch = new CountDownLatch(1);
        }

        @Override
        public void onEvent(final PiJob event, final long sequence, final boolean isEndOfBatch) throws Exception {
            result += event.result;
            if (++seq >= numSlice) {
                latch.countDown();
            }
        }

        public double get() throws InterruptedException {
            latch.await();
            return result;
        }
    }

    @Setup(Level.Iteration)
    public void setup() {
        executor = Executors.newCachedThreadPool();
        disruptor = new com.lmax.disruptor.dsl.Disruptor<>(new PiEventFac(), Integer.highestOneBit(getSlices()),
                executor, ProducerType.SINGLE, new SleepingWaitStrategy());
        final PiEventProcessor procs[] = new PiEventProcessor[getThreads()];
        result = new PiResultReclaimer(getSlices());
        for (int i = 0; i < procs.length; i++) {
            procs[i] = new PiEventProcessor(i);
        }
        disruptor.handleEventsWith(procs).then(result);
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        disruptor.shutdown();
        executor.shutdownNow();
    }

    @Benchmark
    public double run() throws InterruptedException {
        int ts = getThreads();
        int slices = getSlices();
        int partitionId = 0;
        for (int i = 0; i < slices; i++) {
            final long seq = ringBuffer.next();
            final PiJob piJob = ringBuffer.get(seq);
            piJob.sliceNr = i;
            piJob.result = 0;
            piJob.partitionId = partitionId;
            ringBuffer.publish(seq);
            partitionId = (partitionId == (ts - 1)) ? 0 : partitionId + 1;
        }
        return result.get();
    }

}
