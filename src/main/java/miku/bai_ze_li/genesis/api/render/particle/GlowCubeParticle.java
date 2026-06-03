package miku.bai_ze_li.genesis.api.render.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import miku.bai_ze_li.genesis.api.render.shader.GenesisShaderCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class GlowCubeParticle extends Particle {
    private final float cubeSize;

    protected GlowCubeParticle(ClientLevel level, double x, double y, double z, double red, double green, double blue) {
        super(level, x, y, z);
        this.lifetime = 44 + this.random.nextInt(24);
        this.friction = 0.9F;
        this.gravity = 0.06F;
        this.hasPhysics = true;
        this.alpha = 1.0F;
        this.cubeSize = 0.08F + this.random.nextFloat() * 0.1F;

        this.rCol = Mth.clamp((float) red, 0.0F, 1.0F);
        this.gCol = Mth.clamp((float) green, 0.0F, 1.0F);
        this.bCol = Mth.clamp((float) blue, 0.0F, 1.0F);
        if (this.rCol + this.gCol + this.bCol <= 0.001F) {
            this.rCol = 1.0F;
            this.gCol = 1.0F;
            this.bCol = 1.0F;
        }

        double angle = this.random.nextDouble() * Math.PI * 2.0D;
        double horizontalSpeed = 0.08D + this.random.nextDouble() * 0.18D;
        this.xd = Math.cos(angle) * horizontalSpeed;
        this.yd = 0.14D + this.random.nextDouble() * 0.2D;
        this.zd = Math.sin(angle) * horizontalSpeed;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            remove();
            return;
        }

        float progress = (float) this.age / (float) this.lifetime;
        if (progress > 0.68F) {
            this.alpha = 1.0F - (progress - 0.68F) / 0.32F;
        }

        this.xd *= this.friction;
        this.yd = (this.yd - this.gravity) * this.friction;
        this.zd *= this.friction;
        move(this.xd, this.yd, this.zd);

        if (this.onGround) {
            this.xd *= 0.35D;
            this.zd *= 0.35D;
            this.yd = 0.0D;
        }

        if (this.alpha <= 0.001F) {
            remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        if (GenesisShaderCompat.shouldDeferWorldEffects()) {
            return;
        }
        renderDeferred(buffer, camera, partialTicks);
    }

    public void renderDeferred(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vector3f cameraPos = camera.getPosition().toVector3f();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        renderCube(buffer, x, y, z, this.cubeSize);
    }

    private void renderCube(VertexConsumer buffer, float x, float y, float z, float size) {
        float halfSize = size / 2.0F;
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-halfSize, -halfSize, -halfSize),
                new Vector3f(halfSize, -halfSize, -halfSize),
                new Vector3f(halfSize, halfSize, -halfSize),
                new Vector3f(-halfSize, halfSize, -halfSize),
                new Vector3f(-halfSize, -halfSize, halfSize),
                new Vector3f(halfSize, -halfSize, halfSize),
                new Vector3f(halfSize, halfSize, halfSize),
                new Vector3f(-halfSize, halfSize, halfSize)
        };

        for (Vector3f vertex : vertices) {
            vertex.add(x, y, z);
        }

        int light = 15728880;
        addQuad(buffer, vertices[4], vertices[5], vertices[6], vertices[7], light);
        addQuad(buffer, vertices[1], vertices[0], vertices[3], vertices[2], light);
        addQuad(buffer, vertices[5], vertices[1], vertices[2], vertices[6], light);
        addQuad(buffer, vertices[0], vertices[4], vertices[7], vertices[3], light);
        addQuad(buffer, vertices[3], vertices[7], vertices[6], vertices[2], light);
        addQuad(buffer, vertices[4], vertices[0], vertices[1], vertices[5], light);
    }

    private void addQuad(VertexConsumer buffer, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, int light) {
        buffer.vertex(v1.x(), v1.y(), v1.z()).color(this.rCol, this.gCol, this.bCol, this.alpha).endVertex();
        buffer.vertex(v2.x(), v2.y(), v2.z()).color(this.rCol, this.gCol, this.bCol, this.alpha).endVertex();
        buffer.vertex(v3.x(), v3.y(), v3.z()).color(this.rCol, this.gCol, this.bCol, this.alpha).endVertex();
        buffer.vertex(v4.x(), v4.y(), v4.z()).color(this.rCol, this.gCol, this.bCol, this.alpha).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return GlowParticleRenderTypes.GLOW_CUBE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet sprites) {
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double red, double green, double blue) {
            return new GlowCubeParticle(level, x, y, z, red, green, blue);
        }
    }
}
