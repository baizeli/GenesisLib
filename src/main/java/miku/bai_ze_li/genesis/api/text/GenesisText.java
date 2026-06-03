package miku.bai_ze_li.genesis.api.text;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

public final class GenesisText {
    private static final ChatFormatting[] FABULOUS = new ChatFormatting[]{
            ChatFormatting.RED,
            ChatFormatting.GOLD,
            ChatFormatting.YELLOW,
            ChatFormatting.GREEN,
            ChatFormatting.AQUA,
            ChatFormatting.BLUE,
            ChatFormatting.LIGHT_PURPLE
    };
    private static final ChatFormatting[] SANIC = new ChatFormatting[]{
            ChatFormatting.BLUE, ChatFormatting.BLUE, ChatFormatting.BLUE, ChatFormatting.BLUE,
            ChatFormatting.WHITE, ChatFormatting.BLUE, ChatFormatting.WHITE, ChatFormatting.WHITE,
            ChatFormatting.BLUE, ChatFormatting.WHITE, ChatFormatting.WHITE, ChatFormatting.BLUE,
            ChatFormatting.RED, ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.GRAY,
            ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY,
            ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY,
            ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY,
            ChatFormatting.GRAY, ChatFormatting.GRAY
    };

    private GenesisText() {
    }

    public static Component translatableContainerName(String modId, String name) {
        return Component.translatable("container." + modId + "." + name);
    }

    public static String makeFabulous(String input) {
        return animatedFormatting(input, FABULOUS, 80.0D, 1, 1);
    }

    public static String makeGreen(String input) {
        return animatedFormatting(input, new ChatFormatting[]{ChatFormatting.DARK_GREEN}, 80.0D, 1, 1);
    }

    public static String makeSanic(String input) {
        return animatedFormatting(input, SANIC, 50.0D, 2, 1);
    }

    public static String animatedFormatting(String input, ChatFormatting[] colors, double delay, int step, int positionStep) {
        StringBuilder builder = new StringBuilder(input.length() * 3);
        if (delay <= 0.0D) {
            delay = 0.001D;
        }

        int offset = (int) Math.floor(Util.getMillis() / delay) % colors.length;
        for (int i = 0; i < input.length(); i++) {
            int color = (i * positionStep + colors.length - offset) % colors.length;
            builder.append(colors[color]);
            builder.append(input.charAt(i));
        }
        return builder.toString();
    }
}
