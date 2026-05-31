package miku.bai_ze_li.genesis.render.api.particle;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class CrescentBladeParticle extends Particle {
    private static Supplier<? extends ParticleOptions> bloodDripHangParticle;

    public final float radius;
    Vec3 lastVelocity;

    public static void setBloodDripHangParticle(Supplier<? extends ParticleOptions> particle) {
        bloodDripHangParticle = particle;
    }

    public CrescentBladeParticle(ClientLevel level, double x, double y, double z, Vec3 dir, float radius) {
        super(level, x, y, z, dir.x, dir.y, dir.z);
        this.radius = radius;
        this.setLifetime(500);  // 寿命，可调
        this.setAlpha(1f);
        this.setColor(1f, 1f, 1f);  // 白色，可做 shader 高光
        this.hasPhysics = false;
        this.lastVelocity= new Vec3(0.0f, 0.0f, 0.0f);
    }

    public float getRadius() {
        return radius;
    }

    public Vec3 getLastVelocity() {
        return lastVelocity;
    }

    @Override
    public void tick() {
        super.tick();
        double speed = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        if (speed < 0.1 && this.lastVelocity.equals(Vec3.ZERO)) {
            this.lastVelocity = new Vec3(this.xd, this.yd, this.zd);
        }

    }

    @Override
    public void render(VertexConsumer buffer, net.minecraft.client.Camera cam, float partialTick) {

    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    // 获取粒子在屏幕上的 2D 位置（用于 mask）
    public Vec3 getScreenPos() {
        return new Vec3(this.x, this.y, this.z);
    }
    @OnlyIn(Dist.CLIENT)
    public static class BloodDripTwistParticle extends CrescentBladeParticle {
        private final float rotationSpeed;
        private float currentRotation = 0.0f;

        public BloodDripTwistParticle(ClientLevel level, double x, double y, double z, Vec3 dir, float radius) {
            super(level, x, y, z, dir, radius);
            this.setLifetime(500);

            // 设置速度参数
            this.xd = dir.x;
            this.yd = dir.y;
            this.zd = dir.z;

            // 设置旋转速度（基于速度大小）
            float speed = (float) dir.length();
            this.rotationSpeed = speed * 20.0f; // 旋转速度与运动速度成正比

            // 禁用物理和重力
            this.hasPhysics = false;
            this.gravity = 0.0f; // 设置重力为0
            this.lifetime=150;
        }

        @Override
        public void tick() {
            // 保存旧位置（用于渲染插值）
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;

            // 应用速度更新位置
            this.x += this.xd;
            this.y += this.yd;
            this.z += this.zd;

            // 更新旋转角度
            this.currentRotation += this.rotationSpeed;
            if (this.currentRotation >= 360.0f) {
                this.currentRotation -= 360.0f;
            }

            // 添加一些阻力效果，使粒子逐渐减速（可选）
            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;
            // 记录速度几乎为0时的最后速度向量
            double speed = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
            if (speed < 0.1 && this.lastVelocity.equals(Vec3.ZERO)) {

                this.lastVelocity = new Vec3(this.xd, this.yd, this.zd);
            }

            // 在移动过程中生成子粒子的逻辑
            if (this.level.random.nextInt(100) < 30) {
                // 根据当前运动方向计算生成位置
                double angle = this.level.random.nextDouble() * 2 * Math.PI;
                double distance = this.level.random.nextDouble() * 0.5;

                // 计算平面上的偏移坐标（基于当前旋转角度）
                double cosAngle = Math.cos(angle + this.currentRotation * 0.0174533f);
                double sinAngle = Math.sin(angle + this.currentRotation * 0.0174533f);
                double offsetX = cosAngle * distance;
                double offsetZ = sinAngle * distance;

                addBloodDripParticle(this.x + offsetX, this.y, this.z + offsetZ, 0, 0, 0);
            }

            // 粒子生命周期管理
            if (this.age++ >= this.lifetime) {
                // 在粒子消失前生成一些尾迹粒子
                for (int i = 0; i < 5; i++) {
                    double trailX = this.x + (this.level.random.nextDouble() - 0.5) * 0.5;
                    double trailY = this.y + (this.level.random.nextDouble() - 0.5) * 0.5;
                    double trailZ = this.z + (this.level.random.nextDouble() - 0.5) * 0.5;
                    double trailSpeed = 0.02 + this.level.random.nextDouble() * 0.03;

                    addBloodDripParticle(trailX, trailY, trailZ, this.xd * trailSpeed, this.yd * trailSpeed, this.zd * trailSpeed);
                }
                this.remove();
            }
        }

        private void addBloodDripParticle(double x, double y, double z, double dx, double dy, double dz) {
            if (bloodDripHangParticle != null) {
                this.level.addParticle(bloodDripHangParticle.get(), x, y, z, dx, dy, dz);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class BloodDripTwistProvider implements net.minecraft.client.particle.ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public BloodDripTwistProvider(SpriteSet sprites) {
            this.spriteSet = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            Vec3 direction = new Vec3(dx, dy, dz);
            return new BloodDripTwistParticle(level, x, y, z, direction, 0.5f); // 默认半径0.5
        }
    }




    @OnlyIn(Dist.CLIENT)
    public static class Provider implements net.minecraft.client.particle.ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprites) {
            this.spriteSet = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            Vec3 direction = new Vec3(dx, dy, dz);
            return new CrescentBladeParticle(level, x, y, z, direction, 0.5f); // 默认半径0.5
        }
    }
}
