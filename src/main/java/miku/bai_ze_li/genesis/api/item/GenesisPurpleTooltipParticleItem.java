package miku.bai_ze_li.genesis.api.item;

import miku.bai_ze_li.genesis.api.annotation.GenesisTooltipParticles;
import net.minecraft.world.item.Item;

@GenesisTooltipParticles(preset = GenesisTooltipParticles.Preset.PURPLE)
public class GenesisPurpleTooltipParticleItem extends Item {
    public GenesisPurpleTooltipParticleItem(Properties properties) {
        super(properties);
    }
}
