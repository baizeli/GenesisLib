package miku.bai_ze_li.genesis.api.render.tooltip.background;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import miku.bai_ze_li.genesis.api.annotation.GenesisTooltipBackground;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class GenesisTooltipBackgroundRegistry {
    private static final Map<Item, GenesisTooltipBackgroundStyle> DIRECT_STYLES = new ConcurrentHashMap<>();
    private static final List<Function<ItemStack, GenesisTooltipBackgroundStyle>> RESOLVERS = new CopyOnWriteArrayList<>();
    private static volatile GenesisTooltipBackgroundStyle globalStyle = null;

    private GenesisTooltipBackgroundRegistry() {
    }

    public static void setGlobalStyle(GenesisTooltipBackgroundStyle style) {
        globalStyle = style;
    }

    public static void disableGlobalStyle() {
        globalStyle = null;
    }

    public static void register(ItemLike item, GenesisTooltipBackgroundStyle style) {
        register(item.asItem(), style);
    }

    public static void register(Item item, GenesisTooltipBackgroundStyle style) {
        if (style == null) {
            DIRECT_STYLES.remove(item);
        } else {
            DIRECT_STYLES.put(item, style);
        }
    }

    public static void registerResolver(Function<ItemStack, GenesisTooltipBackgroundStyle> resolver) {
        RESOLVERS.add(resolver);
    }

    public static GenesisTooltipBackgroundStyle resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        if (stack.getItem() instanceof GlassTooltipBackgroundItem item) {
            GenesisTooltipBackgroundStyle style = item.getTooltipBackgroundStyle(stack);
            if (style != null) {
                return style;
            }
        }

        GenesisTooltipBackgroundStyle direct = DIRECT_STYLES.get(stack.getItem());
        if (direct != null) {
            return direct;
        }

        for (Function<ItemStack, GenesisTooltipBackgroundStyle> resolver : RESOLVERS) {
            GenesisTooltipBackgroundStyle style = resolver.apply(stack);
            if (style != null) {
                return style;
            }
        }

        GenesisTooltipBackground annotation = stack.getItem().getClass().getAnnotation(GenesisTooltipBackground.class);
        if (annotation != null) {
            return annotation.preset().style();
        }

        return globalStyle;
    }
}
