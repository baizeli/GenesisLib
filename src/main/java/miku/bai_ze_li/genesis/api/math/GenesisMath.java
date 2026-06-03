package miku.bai_ze_li.genesis.api.math;

import net.minecraft.util.Mth;

public final class GenesisMath {
    private GenesisMath() {
    }

    public static float inverseLerp(float value, float min, float max) {
        if (min == max) {
            return 0.0F;
        }
        return Mth.clamp((value - min) / (max - min), 0.0F, 1.0F);
    }
}
