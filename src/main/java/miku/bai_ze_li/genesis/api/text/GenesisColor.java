package miku.bai_ze_li.genesis.api.text;

import java.util.Arrays;
import java.util.List;

public final class GenesisColor {
    public static final List<Integer> NULL = List.of(0xFFFFFF);
    public static final List<Integer> THUNDER_THEME = Arrays.asList(0x00FF00, 0x00FF7F, 0x00FFFF, 0x007FFF, 0x0000FF, 0x3F00FF, 0x7F00FF, 0xFF00FF);
    public static final List<Integer> HOLY_THEME = Arrays.asList(0xFFC000, 0xFFC000, 0xFFFFFF, 0xFFC000);
    public static final List<Integer> BLOOD_THEME = Arrays.asList(0xFF0000, 0xFF4500, 0xFF8C00, 0xFFD700, 0xFF0000);
    public static final List<Integer> STELLAR_THEME = Arrays.asList(0xFF0000, 0xFF7F00, 0xFFFF00, 0x7FFF00, 0x00FF00, 0x00FF7F, 0x00FFFF, 0x007FFF, 0x0000FF, 0x3F00FF, 0x7F00FF, 0xFF00FF, 0xFF007F, 0xFF0000);
    public static final List<Integer> PASTEL_CANDY_THEME = Arrays.asList(0xFFC6E6, 0xFFDAB1, 0xFDF6D6, 0xCB00FF, 0x9BF0FF, 0xBDB6FF, 0xFFC0FF, 0xFFC6E6);
    public static final List<Integer> FIRE_THEME = BLOOD_THEME;
    public static final List<Integer> ICE_THEME = Arrays.asList(0x00FFFF, 0x00BFFF, 0x1E90FF, 0x87CEFA, 0xADD8E6, 0x00FFFF);
    public static final List<Integer> NATURE_THEME = Arrays.asList(0x32CD32, 0x228B22, 0x7DFF00, 0xADF0AF, 0x32CD32);
    public static final List<Integer> PURPLE_DREAM = Arrays.asList(0x6A0DAD, 0xA66CDA, 0xC99CEB, 0xE8B4CB, 0xFFDAB5, 0x6A0DAD);
    public static final List<Integer> RAINBOW = STELLAR_THEME;

    private GenesisColor() {
    }
}
