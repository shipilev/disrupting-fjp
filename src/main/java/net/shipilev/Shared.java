package net.shipilev;

public class Shared {

    public static final int SLICES = 1000000;
    public static final int ITERS = 100;
    public static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static double calculatePi(int sliceNr) {
        double acc = 0.0;
        for (int i = sliceNr * ITERS; i <= ((sliceNr + 1) * ITERS - 1); i++) {
            acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1);
        }
        return acc;
    }

}
