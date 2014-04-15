package net.shipilev;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Disruptor {

    private com.lmax.disruptor.dsl.Disruptor<PiJob> disruptor;
    private ExecutorService executor;
    private PiResultReclaimer res;
    private RingBuffer<PiJob> ringBuffer;

    public static class PiJob {
        public double result;
        public int sliceNr;
        public int partitionId;

        public void calculatePi() {
            result = Shared.calculatePi(sliceNr);
        }
    }

    public static class PiEventFac implements EventFactory<PiJob> {

        @Override
        public PiJob newInstance() {
            return new PiJob();
        }
    }

    public static class PiEventProcessor implements EventHandler<PiJob> {
        private int partionId;

        public PiEventProcessor(int partionId) {
            this.partionId = partionId;
        }

        @Override
        public void onEvent(PiJob event, long sequence, boolean isEndOfBatch) throws Exception {
            if (partionId == event.partitionId) {
                event.calculatePi();
            }
        }
    }

    public static class PiResultReclaimer implements EventHandler<PiJob> {
        double result;
        long seq;
        final int numSlice;
        final CountDownLatch latch;

        public PiResultReclaimer(int numSlice) {
            this.numSlice = numSlice;
            latch = new CountDownLatch(1);
        }

        @Override
        public void onEvent(PiJob event, long sequence, boolean isEndOfBatch) throws Exception {
            result += event.result;
            ++seq;

            if (seq >= numSlice) {
                latch.countDown();
            }
        }
    }

    @Setup(Level.Iteration)
    public void setup() {
        PiEventFac fac = new PiEventFac();
        executor = Executors.newCachedThreadPool();
        disruptor = new com.lmax.disruptor.dsl.Disruptor<PiJob>(fac, 16384, executor, ProducerType.SINGLE, new SleepingWaitStrategy());
        PiEventProcessor procs[] = new PiEventProcessor[Shared.THREADS];
        res = new PiResultReclaimer(Shared.SLICES);

        for (int i = 0; i < procs.length; i++) {
            procs[i] = new PiEventProcessor(i);
        }

        disruptor.handleEventsWith(procs).then(res);

        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        disruptor.shutdown();
        executor.shutdownNow();
    }

    @GenerateMicroBenchmark
    public double run() throws InterruptedException {
        int partitionId = 0;
        for (int i = 0; i < Shared.SLICES; i++) {
            final long seq = ringBuffer.next();
            final PiJob piJob = ringBuffer.get(seq);
            piJob.sliceNr = i;
            piJob.result = 0;
            piJob.partitionId = partitionId;
            ringBuffer.publish(seq);

            partitionId = (partitionId == (Shared.THREADS - 1)) ? 0 : partitionId + 1;
        }

        res.latch.await();
        return res.result;
    }

}
