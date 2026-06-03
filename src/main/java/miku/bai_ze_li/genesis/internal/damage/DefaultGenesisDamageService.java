package miku.bai_ze_li.genesis.internal.damage;

import miku.bai_ze_li.genesis.api.damage.GenesisDamageContext;
import miku.bai_ze_li.genesis.api.damage.GenesisDamageFlag;
import miku.bai_ze_li.genesis.api.damage.GenesisDamagePolicy;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public final class DefaultGenesisDamageService implements GenesisDamageService {
    @Override
    public void applyPolicy(GenesisDamageContext context, GenesisDamagePolicy policy) {
        if (context == null || policy == null || policy == GenesisDamagePolicy.NONE) {
            return;
        }
        if (policy.has(GenesisDamageFlag.BYPASS_ALL)) {
            setBypassAll(context.source(), true);
        } else if (policy.has(GenesisDamageFlag.BYPASS_ARMOR)) {
            setBypassArmor(context.source(), true);
        }
        if (policy.has(GenesisDamageFlag.UNCANCELABLE)) {
            setUncancelable(context.event(), true);
        }
        if (policy.has(GenesisDamageFlag.MINIMUM_DAMAGE_LOCKED)) {
            setMinimumDamageLocked(context.event(), true);
        }
        if (policy.has(GenesisDamageFlag.FORCE_DEATH) && context.event() instanceof LivingDeathEvent deathEvent) {
            forceDeath(deathEvent);
        }
    }

    @Override
    public void setBypassArmor(DamageSource source, boolean value) {
        GenesisDamageKernel.setBypassArmor(source, value);
    }

    @Override
    public void setBypassAll(DamageSource source, boolean value) {
        GenesisDamageKernel.setBypassAll(source, value);
    }

    @Override
    public void setUncancelable(LivingEvent event, boolean value) {
        GenesisDamageKernel.setUncancelable(event, value);
    }

    @Override
    public void setMinimumDamageLocked(LivingEvent event, boolean value) {
        GenesisDamageKernel.setMinimumDamageLocked(event, value);
    }

    @Override
    public void forceDeath(LivingDeathEvent event) {
        GenesisDamageKernel.forceDeath(event);
    }
}
