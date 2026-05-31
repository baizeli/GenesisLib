package miku.bai_ze_li.genesis.core.api.nbt;

import net.minecraft.resources.ResourceLocation;

public record PersistentDataKey(String value) {
    public static PersistentDataKey of(String namespace, String path) {
        return new PersistentDataKey(ResourceLocation.fromNamespaceAndPath(namespace, path).toString());
    }
}
