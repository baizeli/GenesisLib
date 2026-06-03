package miku.bai_ze_li.genesis.api;

import net.minecraft.resources.ResourceLocation;

public final class GenesisResources {
    private GenesisResources() {
    }

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
