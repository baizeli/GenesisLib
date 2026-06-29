package miku.bai_ze_li.genesis.api.render.liquid;

import net.minecraft.world.level.material.Fluid;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GenesisLiquidReflectionApi {
    private static final Map<Fluid, GenesisLiquidReflectionStyle> FLUIDS = new LinkedHashMap<>();

    private GenesisLiquidReflectionApi() {
    }

    public static void register(Fluid sourceFluid) {
        register(sourceFluid, GenesisLiquidReflectionStyle.DEFAULT);
    }

    public static void register(Fluid sourceFluid, GenesisLiquidReflectionStyle style) {
        if (sourceFluid != null && style != null) {
            FLUIDS.put(sourceFluid, style);
        }
    }

    static Map<Fluid, GenesisLiquidReflectionStyle> fluids() {
        return Collections.unmodifiableMap(FLUIDS);
    }
}
