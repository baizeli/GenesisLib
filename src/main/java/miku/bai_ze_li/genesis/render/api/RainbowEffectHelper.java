package miku.bai_ze_li.genesis.render.api;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RainbowEffectHelper {

    // 榛樿鏁堟灉鍙傛暟
    private static final float DEFAULT_SPEED = 1.0f;
    private static final int DEFAULT_DIRECTION = 1;
    private static final float DEFAULT_SATURATION = 1.0f;
    private static final float DEFAULT_BRIGHTNESS = 1.0f;
    private static final float DEFAULT_CHAR_SPACING = 0.05f;

    public static final List<Integer> DEFAULT_RAINBOW = Arrays.asList(
            0xFF0000, // 绾?
            0xFF7F00, // 姗?
            0xFFFF00, // 榛?
            0x7fff00, // 缁?1
            0x00ff00, // 缁?
            0x00ff7f, // 缁?1
            0x00ffff, // 闈?
            0x007fff, // 钃?1
            0x0000ff, // 钃?
            0x3f00ff, // 绱?1
            0x7f00ff, // 绱?
            0xff00ff, // 绮?
            0xff007f, // 绾?1
            0xFF0000  // 绾?
    );

    public static final List<Integer> DEFAULT_RAINBOW_PRO = Arrays.asList(
            0xFF0000, // 绾?
            0xFF7F00, // 姗?
            0xFF7F00,
            0xFFFF00, // 榛?
            0xFFFF00,
            0x7fff00, // 缁?1
            0x7fff00,
            0x00ff00, // 缁?
            0x00ff00,
            0x00ff7f, // 缁?1
            0x00ff7f,
            0x00ffff, // 闈?
            0x00ffff,
            0x007fff, // 钃?1
            0x007fff,
            0x0000ff, // 钃?
            0x0000ff,
            0x0000ff, // 绱?1
            0x0000ff,
            0x7f00ff, // 绱?
            0x7f00ff,
            0xff00ff, // 绮?
            0xff00ff,
            0xff007f, // 绾?1
            0xff007f,
            0xFF0000,  // 绾?
            0xFF0000
    );

    public static final List<Integer> BLUE = Arrays.asList(
            0x00ccff,
            0x002eff,
            0x002eff,
            0x00ffc2,
            0x00ffc2,
            0x00ccff
    );

    public static MutableComponent createCustomGradientText(
            String text,
            List<Integer> colors,
            float speed,
            int direction,
            float charSpacing,
            float gradientSpan
    ) {

        long time = getTimeBase();
        if (colors.size() < 2) {
            colors = new ArrayList<>(colors);
            colors.add(colors.get(0));
        }

        MutableComponent gradientText = Component.empty();
        int length = text.length();

        for (int i = 0; i < length; i++)
        {
            int index = (direction > 0) ? i : length - 1 - i;
            float position = ((time * speed) / 100f + i * charSpacing) % 1f;
            position = position * gradientSpan % 1f;
            int color = getGradientColor(colors, position);
            gradientText.append(Component.literal(String.valueOf(text.charAt(index))).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }

        return gradientText;
    }

    private static int getGradientColor(List<Integer> colors, float position) {
        int colorCount = colors.size();
        float segment = 1f / (colorCount - 1);

        int segmentIndex = (int) (position / segment);
        if (segmentIndex >= colorCount - 1) return colors.get(colorCount - 1);
        float segmentPos = (position % segment) / segment;
        int startColor = colors.get(segmentIndex);
        int endColor = colors.get(segmentIndex + 1);

        return interpolateColor(startColor, endColor, segmentPos);
    }

    private static int interpolateColor(int start, int end, float progress) {
        int startR = (start >> 16) & 0xFF;
        int startG = (start >> 8) & 0xFF;
        int startB = start & 0xFF;

        int endR = (end >> 16) & 0xFF;
        int endG = (end >> 8) & 0xFF;
        int endB = end & 0xFF;

        int r = (int) (startR + (endR - startR) * progress);
        int g = (int) (startG + (endG - startG) * progress);
        int b = (int) (startB + (endB - startB) * progress);

        return (r << 16) | (g << 8) | b;
    }

    public static MutableComponent createRainbowText(String text, float speed, int direction,
                                                     float saturation, float brightness, float charSpacing) {
        long time = getTimeBase();
        MutableComponent rainbowText = Component.empty();
        int length = text.length();

        for (int i = 0; i < length; i++)
        {
            int index = (direction > 0) ? i : length - 1 - i;
            float hue = ((time * speed) / 100f + i * charSpacing) % 1f;
            int rgb = HSBtoRGB(hue, saturation, brightness);
            rainbowText.append(Component.literal(String.valueOf(text.charAt(index))).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
        }

        return rainbowText;
    }

    public static MutableComponent createRainbowText(
            String text,
            float speed,
            int direction,
            float charSpacing
    ) {
        return createCustomGradientText(
                text,
                DEFAULT_RAINBOW,
                speed,
                direction,
                charSpacing,
                1.0f
        );
    }

    public static MutableComponent createRainbowText(String text) {
        return createRainbowText(text, DEFAULT_SPEED, DEFAULT_DIRECTION,
                DEFAULT_SATURATION, DEFAULT_BRIGHTNESS, DEFAULT_CHAR_SPACING);
    }

    private static long getTimeBase() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) return minecraft.level.getGameTime();
        return System.currentTimeMillis() / 50;
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - Mth.floor(hue)) * 6.0f;
            float f = h - Mth.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }
}
