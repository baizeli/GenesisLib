package miku.bai_ze_li.genesis.internal.damage;

import miku.bai_ze_li.genesis.GenesisLib;
import miku.bai_ze_li.genesis.api.damage.GenesisDamageApi;
import miku.bai_ze_li.genesis.api.damage.GenesisDamagePolicy;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenesisLib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DamageEventHandler {
    private DamageEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        applySourcePolicy(event, event.getSource());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        applySourcePolicy(event, event.getSource());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (DamagePolicyResolver.isBypassesAll(event.getSource())) {
            GenesisDamageApi.setUncancelable(event, true);
            GenesisDamageApi.lockMinimumDamage(event, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeathHighest(LivingDeathEvent event) {
        applyDeathPolicy(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeathLowest(LivingDeathEvent event) {
        applyDeathPolicy(event);
    }

    private static void applySourcePolicy(net.minecraftforge.event.entity.living.LivingEvent event, net.minecraft.world.damagesource.DamageSource source) {
        GenesisDamagePolicy policy = DamagePolicyResolver.fromSource(source);
        GenesisDamageApi.applyPolicy(event, source, policy);
    }

    private static void applyDeathPolicy(LivingDeathEvent event) {
        if (DamagePolicyResolver.isBypassesAll(event.getSource())) {
            GenesisDamageApi.forceDeath(event);
        }
    }
}
