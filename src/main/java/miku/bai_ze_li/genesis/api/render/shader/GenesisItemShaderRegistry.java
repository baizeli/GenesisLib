package miku.bai_ze_li.genesis.api.render.shader;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public final class GenesisItemShaderRegistry {
    private static final Map<Item, GenesisItemShaderEffect> EFFECTS = new ConcurrentHashMap<>();
    private static volatile Map<net.minecraft.resources.ResourceLocation, GenesisItemShaderEffect> DATA_EFFECTS = Map.of();
    private static final List<Function<ItemStack, GenesisItemShaderEffect>> RESOLVERS = new CopyOnWriteArrayList<>();

    private GenesisItemShaderRegistry() {
    }

    public static void register(ItemLike item, GenesisItemShaderEffect effect) {
        register(item.asItem(), effect);
    }

    public static void register(Item item, GenesisItemShaderEffect effect) {
        EFFECTS.put(item, effect);
    }

    public static void reloadFromConfigs(Collection<GenesisItemShaderConfig> configs) {
        Map<net.minecraft.resources.ResourceLocation, GenesisItemShaderEffect> loaded = new LinkedHashMap<>();
        for (GenesisItemShaderConfig config : configs) {
            if (config.getID() != null) {
                loaded.put(config.getID(), config.toEffect());
            }
        }
        DATA_EFFECTS = Map.copyOf(loaded);
    }

    public static void registerResolver(Function<ItemStack, @Nullable GenesisItemShaderEffect> resolver) {
        RESOLVERS.add(resolver);
    }

    @Nullable
    public static GenesisItemShaderEffect resolve(ItemStack stack) {
        GenesisItemShaderEffect effect = EFFECTS.get(stack.getItem());
        if (effect != null) {
            return effect;
        }

        net.minecraft.resources.ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            effect = DATA_EFFECTS.get(itemId);
            if (effect != null) {
                return effect;
            }
        }

        for (Function<ItemStack, GenesisItemShaderEffect> resolver : RESOLVERS) {
            effect = resolver.apply(stack);
            if (effect != null) {
                return effect;
            }
        }

        return null;
    }
}
