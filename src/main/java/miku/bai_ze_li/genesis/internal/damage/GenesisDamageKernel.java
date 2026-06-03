package miku.bai_ze_li.genesis.internal.damage;

import miku.bai_ze_li.genesis.internal.access.GenesisDamageSourceAccess;
import miku.bai_ze_li.genesis.internal.access.GenesisLivingEventAccess;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public final class GenesisDamageKernel {
    private GenesisDamageKernel() {
    }

    public static void setBypassArmor(DamageSource source, boolean value) {
        if (source instanceof GenesisDamageSourceAccess access) {
            access.genesis$setBypassArmor(value);
        }
    }

    public static void setBypassAll(DamageSource source, boolean value) {
        if (source instanceof GenesisDamageSourceAccess access) {
            access.genesis$setBypassAll(value);
        }
    }

    public static boolean hasBypassAll(DamageSource source) {
        return source instanceof GenesisDamageSourceAccess access && access.genesis$hasBypassAll();
    }

    public static boolean hasBypassArmor(DamageSource source) {
        return source instanceof GenesisDamageSourceAccess access && access.genesis$hasBypassArmor();
    }

    public static void setUncancelable(LivingEvent event, boolean value) {
        if (event instanceof GenesisLivingEventAccess access) {
            access.genesis$setUncancelable(value);
        }
    }

    public static void setMinimumDamageLocked(LivingEvent event, boolean value) {
        if (event instanceof GenesisLivingEventAccess access) {
            access.genesis$setMinimumDamageLocked(value);
        }
    }

    public static void forceDeath(LivingDeathEvent event) {
        setUncancelable(event, true);
        event.getEntity().setHealth(0.0F);
    }
}
