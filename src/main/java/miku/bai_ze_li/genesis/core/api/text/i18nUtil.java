package miku.bai_ze_li.genesis.core.api.text;

import net.minecraft.network.chat.Component;

public class i18nUtil {
    public static Component translatableContainerName(String modId, String name) {
        return Component.translatable("container." + modId + "." + name);
    }
}