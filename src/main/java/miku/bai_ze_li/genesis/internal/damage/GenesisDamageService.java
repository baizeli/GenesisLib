package miku.bai_ze_li.genesis.internal.damage;

import miku.bai_ze_li.genesis.api.damage.GenesisDamageContext;
import miku.bai_ze_li.genesis.api.damage.GenesisDamagePolicy;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public interface GenesisDamageService {
    void applyPolicy(GenesisDamageContext context, GenesisDamagePolicy policy);

    void setBypassArmor(DamageSource source, boolean value);

    void setBypassAll(DamageSource source, boolean value);

    void setUncancelable(LivingEvent event, boolean value);

    void setMinimumDamageLocked(LivingEvent event, boolean value);

    void forceDeath(LivingDeathEvent event);
}
