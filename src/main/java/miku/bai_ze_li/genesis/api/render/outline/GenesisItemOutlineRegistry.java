package miku.bai_ze_li.genesis.api.render.outline;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class GenesisItemOutlineRegistry {
    private static final Map<Item, GenesisEffect> DIRECT_EFFECTS = new ConcurrentHashMap<>();
    private static volatile Map<ResourceLocation, GenesisEffect> DATA_EFFECTS = Map.of();

    private GenesisItemOutlineRegistry() {
    }

    public static void register(ItemLike item, GenesisEffect effect) {
        register(item.asItem(), effect);
    }

    public static void register(Item item, GenesisEffect effect) {
        if (effect != null) {
            DIRECT_EFFECTS.put(item, effect);
        }
    }

    public static void reloadFromConfigs(Collection<GenesisItemOutlineConfig> configs) {
        Map<ResourceLocation, GenesisEffect> loaded = new LinkedHashMap<>();
        for (GenesisItemOutlineConfig config : configs) {
            GenesisEffect effect = config.toEffect();
            if (config.getID() != null && effect != null) {
                loaded.put(config.getID(), effect);
            }
        }
        DATA_EFFECTS = Map.copyOf(loaded);
    }

    @Nullable
    public static GenesisEffect resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return resolve(stack.getItem());
    }

    @Nullable
    public static GenesisEffect resolve(Item item) {
        GenesisEffect effect = DIRECT_EFFECTS.get(item);
        if (effect != null) {
            return effect;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return itemId == null ? null : DATA_EFFECTS.get(itemId);
    }
}
