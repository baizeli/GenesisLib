package miku.bai_ze_li.genesis.core.api;

import net.minecraft.resources.ResourceLocation;

public final class GenesisIds {
    private GenesisIds() {
    }

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
