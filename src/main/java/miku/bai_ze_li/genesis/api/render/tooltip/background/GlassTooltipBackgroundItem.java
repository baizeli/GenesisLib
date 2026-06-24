package miku.bai_ze_li.genesis.api.render.tooltip.background;

import net.minecraft.world.item.ItemStack;

public interface GlassTooltipBackgroundItem {
    default GenesisTooltipBackgroundStyle getTooltipBackgroundStyle(ItemStack stack) {
        return GenesisTooltipBackgroundStyle.DEFAULT_GLASS;
    }
}
