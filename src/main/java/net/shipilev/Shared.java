package net.shipilev;

public class Shared {

    public static final int SLICES = 1000000;
    public static final int ITERS = 100;
    public static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static double calculatePi(final int sliceNr) {
        final int from = sliceNr * ITERS;
        final int to = from + ITERS;
        final int c = (to << 1) + 1;
        double acc = 0;
        for (int a = 4 - ((from & 1) << 3), b = (from << 1) + 1; b < c; a = -a, b += 2) {
            acc += ((double) a) / b;
        }
        return acc;
    }

}
