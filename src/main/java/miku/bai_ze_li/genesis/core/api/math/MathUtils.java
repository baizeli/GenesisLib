package miku.bai_ze_li.genesis.core.api.math;

public class MathUtils {
    public static final double torad = 0.017453292519943;
    public static double[] SIN_TABLE = new double[65536];

    static {
        for (int i = 0; i < 65536; ++i) {
            SIN_TABLE[i] = Math.sin(i / 65536D * 2 * Math.PI);
        }
    }

    public static double sin(double d) {
        return SIN_TABLE[(int) ((float) d * 10430.378F) & 65535];
    }
}
