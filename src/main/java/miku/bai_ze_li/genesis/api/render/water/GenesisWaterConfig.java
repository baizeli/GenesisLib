package miku.bai_ze_li.genesis.api.render.water;

import net.minecraftforge.common.ForgeConfigSpec;

public final class GenesisWaterConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.EnumValue<GenesisWaterQuality> QUALITY;
    public static final ForgeConfigSpec.DoubleValue ALPHA;
    public static final ForgeConfigSpec.DoubleValue REFRACTION_STRENGTH;
    public static final ForgeConfigSpec.DoubleValue EDGE_WOBBLE_STRENGTH;
    public static final ForgeConfigSpec.DoubleValue WAVE_STRENGTH;
    public static final ForgeConfigSpec.DoubleValue WAVE_SCALE;
    public static final ForgeConfigSpec.DoubleValue FLOW_SPEED;
    public static final ForgeConfigSpec.DoubleValue DISTANCE_FADE_START;
    public static final ForgeConfigSpec.DoubleValue DISTANCE_FADE_END;
    public static final ForgeConfigSpec.DoubleValue EDGE_FADE_STRENGTH;
    public static final ForgeConfigSpec.BooleanValue AFFECT_VANILLA_WATER;
    public static final ForgeConfigSpec.BooleanValue AFFECT_CUSTOM_FLUIDS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("water_refraction");

        ENABLED = builder.define("enable", true);
        QUALITY = builder.defineEnum("quality", GenesisWaterQuality.MEDIUM);
        ALPHA = builder.defineInRange("alpha", 0.55D, 0.05D, 0.95D);
        REFRACTION_STRENGTH = builder.defineInRange("refractionStrength", 0.006D, 0.0D, 0.05D);
        EDGE_WOBBLE_STRENGTH = builder.defineInRange("edgeWobbleStrength", 0.012D, 0.0D, 0.08D);
        WAVE_STRENGTH = builder.defineInRange("waveStrength", 0.45D, 0.0D, 2.0D);
        WAVE_SCALE = builder.defineInRange("waveScale", 0.055D, 0.005D, 0.5D);
        FLOW_SPEED = builder.defineInRange("flowSpeed", 1.0D, 0.0D, 5.0D);
        DISTANCE_FADE_START = builder.defineInRange("distanceFadeStart", 8.0D, 0.0D, 256.0D);
        DISTANCE_FADE_END = builder.defineInRange("distanceFadeEnd", 64.0D, 1.0D, 512.0D);
        EDGE_FADE_STRENGTH = builder.defineInRange("edgeFadeStrength", 0.35D, 0.0D, 1.0D);
        AFFECT_VANILLA_WATER = builder.define("affectVanillaWater", false);
        AFFECT_CUSTOM_FLUIDS = builder.define("affectCustomFluids", true);

        builder.pop();
        SPEC = builder.build();
    }

    private GenesisWaterConfig() {
    }
}
