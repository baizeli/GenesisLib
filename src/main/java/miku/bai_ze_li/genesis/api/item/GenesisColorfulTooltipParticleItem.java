package miku.bai_ze_li.genesis.api.item;

import miku.bai_ze_li.genesis.api.annotation.GenesisTooltipParticles;
import net.minecraft.world.item.Item;

@GenesisTooltipParticles(preset = GenesisTooltipParticles.Preset.COLORFUL)
public class GenesisColorfulTooltipParticleItem extends Item {
    public GenesisColorfulTooltipParticleItem(Properties properties) {
        super(properties);
    }
}
