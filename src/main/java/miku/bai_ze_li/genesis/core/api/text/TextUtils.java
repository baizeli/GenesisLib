package miku.bai_ze_li.genesis.core.api.text;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;

public class TextUtils {
    private static final ChatFormatting[] fabulousness;
    private static final ChatFormatting[] sanic;

    public TextUtils() {
    }

    public static String makeFabulous(String input) {
        return ludicrousFormatting(input, fabulousness, 80.0, 1, 1);
    }

    public static String makeGreen(String input) {
        return ludicrousFormatting(input, new ChatFormatting[]{ChatFormatting.DARK_GREEN}, 80.0, 1, 1);
    }

    public static String makeSANIC(String input) {
        return ludicrousFormatting(input, sanic, 50.0, 2, 1);
    }

    public static String ludicrousFormatting(String input, ChatFormatting[] colours, double delay, int step, int posstep) {
        StringBuilder sb = new StringBuilder(input.length() * 3);
        if (delay <= 0.0) {
            delay = 0.001;
        }

        int offset = (int)Math.floor((double) Util.getMillis() / delay) % colours.length;

        for(int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            int col = (i * posstep + colours.length - offset) % colours.length;
            sb.append(colours[col].toString());
            sb.append(c);
        }

        return sb.toString();
    }

    static {
        fabulousness = new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE};
        sanic = new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.BLUE, ChatFormatting.BLUE, ChatFormatting.BLUE, ChatFormatting.WHITE, ChatFormatting.BLUE, ChatFormatting.WHITE, ChatFormatting.WHITE, ChatFormatting.BLUE, ChatFormatting.WHITE, ChatFormatting.WHITE, ChatFormatting.BLUE, ChatFormatting.RED, ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY, ChatFormatting.GRAY};
    }
}

