package miku.bai_ze_li.genesis.render.api.effect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SlashEffectAPI {

    @OnlyIn(Dist.CLIENT)
    public static void spawnOnEntity(LivingEntity attacker, Entity target) {
        Vec3 targetCenter = new Vec3(
                target.getX(),
                target.getY() + target.getBbHeight() / 2.0f,
                target.getZ()
        );

        float yaw = attacker.getYRot();
        float pitch = attacker.getXRot();

        SlashEffectManager.add(new BaiZeLiSlashEffect(targetCenter, yaw, pitch));
    }
    @OnlyIn(Dist.CLIENT)
    public static void spawnForward(LivingEntity player, double distance) {
        // 1. 从眼睛位置开始算
        Vec3 eyePos = player.getEyePosition();
        // 2. 获取视线方向向量
        Vec3 lookVec = player.getLookAngle();

        // 3. 沿着视线往前推 distance 的距离！(第一人称终于能看见了！)
        Vec3 center = eyePos.add(lookVec.scale(distance));

        float yaw = player.getYRot();
        float pitch = player.getXRot();

        SlashEffectManager.add(new BaiZeLiSlashEffect(center, yaw, pitch));
    }
}