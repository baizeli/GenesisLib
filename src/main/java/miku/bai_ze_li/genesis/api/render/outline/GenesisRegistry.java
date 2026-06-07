package miku.bai_ze_li.genesis.api.render.outline;

import net.minecraft.world.item.Item;

public final class GenesisRegistry {
    private GenesisRegistry() {
    }

    public static GenesisEffect getTargetEffect(Item item) {
        return GenesisItemOutlineRegistry.resolve(item);
    }
}
