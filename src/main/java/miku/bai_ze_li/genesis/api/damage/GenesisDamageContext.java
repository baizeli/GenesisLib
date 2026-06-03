package miku.bai_ze_li.genesis.api.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;

public record GenesisDamageContext(LivingEvent event, DamageSource source) {
}
