package miku.bai_ze_li.genesis.core.damage;

import miku.bai_ze_li.genesis.GenesisLib;
import miku.bai_ze_li.genesis.core.api.damage.GenesisDamageTypeTags;
import miku.bai_ze_li.genesis.core.api.mixin.DamageSourceInterface;
import miku.bai_ze_li.genesis.core.api.mixin.LivingEventEC;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenesisLib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GenesisDamageBypassEvent {
    private GenesisDamageBypassEvent() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        applyAttackOrHurtFlags(event.getSource(), (LivingEventEC) event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        applyAttackOrHurtFlags(event.getSource(), (LivingEventEC) event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!isBypassesAll(event.getSource())) {
            return;
        }

        LivingEventEC ec = (LivingEventEC) event;
        ec.ironSpellGenesis$hackedUnCancelable(true);
        ec.ironSpellGenesis$hackedOnlyAmountUp(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeathHighest(LivingDeathEvent event) {
        applyBypassesAllDeathFlags(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeathLowest(LivingDeathEvent event) {
        applyBypassesAllDeathFlags(event);
    }

    private static void applyAttackOrHurtFlags(DamageSource source, LivingEventEC event) {
        if (isBypassesAll(source)) {
            ((DamageSourceInterface) source).ironSpellGenesis$setBypassAll(true);
            event.ironSpellGenesis$hackedUnCancelable(true);
            event.ironSpellGenesis$hackedOnlyAmountUp(true);
        } else if (isBypassesArmor(source)) {
            ((DamageSourceInterface) source).ironSpellGenesis$setBypassArmor(true);
        }
    }

    private static void applyBypassesAllDeathFlags(LivingDeathEvent event) {
        if (!isBypassesAll(event.getSource())) {
            return;
        }

        LivingEventEC ec = (LivingEventEC) event;
        ec.ironSpellGenesis$hackedUnCancelable(true);
        event.getEntity().setHealth(0F);
    }

    private static boolean isBypassesAll(DamageSource source) {
        return source.is(GenesisDamageTypeTags.BYPASSES_ALL);
    }

    private static boolean isBypassesArmor(DamageSource source) {
        return source.is(GenesisDamageTypeTags.BYPASSES_ARMOR);
    }
}
