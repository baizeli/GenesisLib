package miku.bai_ze_li.genesis.api.math;

public final class GenesisFastMath {
    public static final double TO_RADIANS = 0.017453292519943;
    private static final double[] SIN_TABLE = new double[65536];

    static {
        for (int i = 0; i < SIN_TABLE.length; i++) {
            SIN_TABLE[i] = Math.sin(i / 65536.0D * 2.0D * Math.PI);
        }
    }

    private GenesisFastMath() {
    }

    public static double sin(double value) {
        return SIN_TABLE[(int) ((float) value * 10430.378F) & 65535];
    }
}
