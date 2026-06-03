package miku.bai_ze_li.genesis.api.item;

import miku.bai_ze_li.genesis.api.annotation.GenesisTooltipParticles;
import net.minecraft.world.item.Item;

@GenesisTooltipParticles(preset = GenesisTooltipParticles.Preset.GOLD)
public class GenesisGoldTooltipParticleItem extends Item {
    public GenesisGoldTooltipParticleItem(Properties properties) {
        super(properties);
    }
}
