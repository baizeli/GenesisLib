package miku.bai_ze_li.genesis.api.render.tooltip;

import net.minecraft.world.item.ItemStack;

public interface ITooltipParticleItem
{
    TooltipParticleSystem.ParticleConfig getParticleConfig();

    //是否应该生成粒子
    default boolean shouldSpawnParticles(ItemStack stack) {return true;}

    //粒子生成频率
    default int getParticleSpawnRate() {return 5;}// 每5tick生成一次
}