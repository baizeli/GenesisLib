package miku.bai_ze_li.genesis.api.damage;

import miku.bai_ze_li.genesis.internal.GenesisServices;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public final class GenesisDamageApi {
    private GenesisDamageApi() {
    }

    public static void applyPolicy(LivingEvent event, DamageSource source, GenesisDamagePolicy policy) {
        GenesisServices.damage().applyPolicy(new GenesisDamageContext(event, source), policy);
    }

    public static void makeTrueDamage(LivingEvent event, DamageSource source) {
        applyPolicy(event, source, GenesisDamagePolicy.TRUE_DAMAGE);
    }

    public static void bypassAll(DamageSource source) {
        GenesisServices.damage().setBypassAll(source, true);
    }

    public static void bypassArmor(DamageSource source) {
        GenesisServices.damage().setBypassArmor(source, true);
    }

    public static void setUncancelable(LivingEvent event, boolean value) {
        GenesisServices.damage().setUncancelable(event, value);
    }

    public static void lockMinimumDamage(LivingEvent event, boolean value) {
        GenesisServices.damage().setMinimumDamageLocked(event, value);
    }

    public static void forceDeath(LivingDeathEvent event) {
        GenesisServices.damage().forceDeath(event);
    }
}
