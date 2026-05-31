package miku.bai_ze_li.genesis.render.api.text;

import miku.bai_ze_li.genesis.render.api.RainbowEffectHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public final class GenesisTextEffects {
    private GenesisTextEffects() {
    }

    public static MutableComponent blueGradient(Component component) {
        return blueGradient(component.getString());
    }

    public static MutableComponent blueGradient(String text) {
        return gradient(text, RainbowEffectHelper.BLUE, 3.0F, 1, 0.03F, 1.0F);
    }

    public static MutableComponent rainbowGradient(Component component) {
        return rainbowGradient(component.getString());
    }

    public static MutableComponent rainbowGradient(String text) {
        return gradient(text, RainbowEffectHelper.DEFAULT_RAINBOW, 2.0F, 1, 0.05F, 2.0F);
    }

    public static MutableComponent gradient(String text, List<Integer> colors, float speed, int direction, float charSpacing, float gradientSpan) {
        return RainbowEffectHelper.createCustomGradientText(text, colors, speed, direction, charSpacing, gradientSpan);
    }
}
