package miku.bai_ze_li.genesis.api.render.tooltip;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public final class TooltipBackgroundRenderContext {
    private static final List<Function<ItemStack, Style>> RESOLVERS = new CopyOnWriteArrayList<>();
    private static final List<String> TRANSPARENT_NAMESPACES = new CopyOnWriteArrayList<>();

    private TooltipBackgroundRenderContext() {
    }

    public static void registerResolver(Function<ItemStack, Style> resolver) {
        RESOLVERS.add(resolver);
    }

    public static void registerTransparentNamespace(String namespace) {
        TRANSPARENT_NAMESPACES.add(namespace);
    }

    public static Style resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Style.NONE;
        }

        for (Function<ItemStack, Style> resolver : RESOLVERS) {
            Style style = resolver.apply(stack);
            if (style != null && style != Style.NONE) {
                return style;
            }
        }

        Item item = stack.getItem();
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
        if (registryName != null && TRANSPARENT_NAMESPACES.contains(registryName.getNamespace())) {
            return Style.TRANSPARENT;
        }

        return Style.NONE;
    }

    public enum Style {
        NONE(0),
        TRANSPARENT(0),
        CHAOS(14),
        CELESTIAL_SOURCE(9);

        private final int cosmicType;

        Style(int cosmicType) {
            this.cosmicType = cosmicType;
        }

        public boolean drawsCosmic() {
            return this == CHAOS || this == CELESTIAL_SOURCE;
        }

        public int cosmicType() {
            return cosmicType;
        }
    }
}
