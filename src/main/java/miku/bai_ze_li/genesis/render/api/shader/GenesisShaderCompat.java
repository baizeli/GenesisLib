package miku.bai_ze_li.genesis.render.api.shader;

import net.minecraftforge.fml.ModList;

public final class GenesisShaderCompat {
    public static boolean IRIS_SETUP = ModList.get().isLoaded("oculus");

    private GenesisShaderCompat() {
    }

    public static boolean shouldDeferWorldEffects() {
        if (!IRIS_SETUP) {
            return false;
        }

        try {
            net.irisshaders.iris.config.IrisConfig irisConfig = net.irisshaders.iris.Iris.getIrisConfig();
            if (!irisConfig.areShadersEnabled()) {
                return false;
            }
            return net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse() || irisConfig.areShadersEnabled();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
