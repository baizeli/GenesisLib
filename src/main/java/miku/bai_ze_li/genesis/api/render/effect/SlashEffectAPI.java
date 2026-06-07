package miku.bai_ze_li.genesis.api.render.effect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SlashEffectAPI {
    public static final int DEFAULT_COLOR = 0xFF4AA6FF;

    @OnlyIn(Dist.CLIENT)
    public static void spawnOnEntity(LivingEntity attacker, Entity target) {
        spawnOnEntity(attacker, target, DEFAULT_COLOR);
    }

    @OnlyIn(Dist.CLIENT)
    public static void spawnOnEntity(LivingEntity attacker, Entity target, int argbColor) {
        Vec3 targetCenter = new Vec3(
                target.getX(),
                target.getY() + target.getBbHeight() / 2.0F,
                target.getZ()
        );

        float yaw = attacker.getYRot();
        float pitch = attacker.getXRot();

        SlashEffectManager.add(new BaiZeLiSlashEffect(targetCenter, yaw, pitch, argbColor));
    }

    @OnlyIn(Dist.CLIENT)
    public static void spawnForward(LivingEntity player, double distance) {
        spawnForward(player, distance, DEFAULT_COLOR);
    }

    @OnlyIn(Dist.CLIENT)
    public static void spawnForward(LivingEntity player, double distance, int argbColor) {
        Vec3 center = player.getEyePosition().add(player.getLookAngle().scale(distance));
        SlashEffectManager.add(new BaiZeLiSlashEffect(center, player.getYRot(), player.getXRot(), argbColor));
    }
}
