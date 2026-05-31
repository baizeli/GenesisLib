package miku.bai_ze_li.genesis.render.api;

import java.util.Arrays;
import java.util.List;

public class ColorHelper {
    // 鍩虹棰滆壊鍒楄〃
    public static final List<Integer> NULL = List.of(16777215);

    // 闆烽渾涓婚 - 钃濈传鑹叉笎鍙?
    public static final List<Integer> THUNDER_THEME = Arrays.asList(
            65280, 65407, 65535, 32767, 255, 4129023, 8323327, 16711935
    );

    // 绁炲湥涓婚 - 閲戦粍鑹叉笎鍙?
    public static final List<Integer> HOLY_THEME = Arrays.asList(
            16764672, 16764672, 16777215, 16764672
    );

    // 椴滆涓婚 - 绾㈣壊娓愬彉
    public static final List<Integer> BLOOD_THEME = Arrays.asList(
            16711680, 16729344, 16747520, 16766720, 16711680
    );

    // 鏄熻景涓婚 - 鍏ㄩ鑹查殢鏈猴紙浣跨敤鐗规畩鏍囪锛屾覆鏌撴椂澶勭悊锛?
    public static final List<Integer> STELLAR_THEME = Arrays.asList(
            16711680, 16744192, 16776960, 8388352, 65280, 65407, 65535, 32767, 255, 4129023, 8323327, 16711935, 16711807, 16711680
    );

    // 鍏朵粬棰勮棰滆壊涓婚
    public static final List<Integer> PASTEL_CANDY_THEME = Arrays.asList(
            16763110, 16767153, 16645526, 13303743, 10221311, 12432127, 16762623, 16763110
    );

    public static final List<Integer> FIRE_THEME = Arrays.asList(
            16711680, 16729344, 16747520, 16766720, 16711680
    );

    public static final List<Integer> ICE_THEME = Arrays.asList(
            65535, 49151, 2003199, 8900346, 11393254, 65535
    );

    public static final List<Integer> NATURE_THEME = Arrays.asList(
            3329330, 2263842, 8190976, 11403055, 3329330
    );

    public static final List<Integer> PURPLE_DREAM = Arrays.asList(
            6948210, 10904524, 13211340, 15250635, 16764117, 6948210
    );

    public static final List<Integer> RAINBOW = Arrays.asList(
            16711680, 16744192, 16776960, 8388352, 65280, 65407, 65535, 32767, 255, 4129023, 8323327, 16711935, 16711807, 16711680
    );
}

