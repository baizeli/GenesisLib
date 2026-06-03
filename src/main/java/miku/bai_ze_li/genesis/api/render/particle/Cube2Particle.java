package miku.bai_ze_li.genesis.api.render.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Cube2Particle extends Particle {

    private float rotationX, rotationY, rotationZ;
    private float rotationSpeedX, rotationSpeedY, rotationSpeedZ;

    protected Cube2Particle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z);

        this.lifetime = 100;

        this.setSize(0.2f, 0.2f);

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.gravity = 0.0F;
        this.lifetime=500;

        
        initRandomRotation();
    }

    private void initRandomRotation() {
        
        this.rotationX = this.random.nextFloat() * 360.0F;
        this.rotationY = this.random.nextFloat() * 360.0F;
        this.rotationZ = this.random.nextFloat() * 360.0F;

        
        float speedFactor = (float) Math.sqrt(xd * xd + yd * yd + zd * zd);

        
        float baseSpeed = 2.0F; 

        
        int directionX = this.random.nextBoolean() ? 1 : -1;
        int directionY = this.random.nextBoolean() ? 1 : -1;
        int directionZ = this.random.nextBoolean() ? 1 : -1;

        
        this.rotationSpeedX = (baseSpeed + speedFactor * 10.0F) * directionX;
        this.rotationSpeedY = (baseSpeed + speedFactor * 10.0F) * directionY;
        this.rotationSpeedZ = (baseSpeed + speedFactor * 10.0F) * directionZ;

        
        this.rotationSpeedX *= (0.5F + this.random.nextFloat() * 0.5F);
        this.rotationSpeedY *= (0.5F + this.random.nextFloat() * 0.5F);
        this.rotationSpeedZ *= (0.5F + this.random.nextFloat() * 0.5F);
    }

    @Override
    public void tick() {
        super.tick();

        
        updateRotation();
        updateColor();

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    private void updateColor() {


            this.rCol = 0.5F + this.random.nextFloat() * 0.5F;
            this.gCol = 0.5F + this.random.nextFloat() * 0.5F;
            this.bCol = 0.5F + this.random.nextFloat() * 0.5F;

    }

    private void updateRotation() {
        
        this.rotationX += this.rotationSpeedX;
        this.rotationY += this.rotationSpeedY;
        this.rotationZ += this.rotationSpeedZ;

        
        this.rotationX %= 360.0F;
        this.rotationY %= 360.0F;
        this.rotationZ %= 360.0F;

        
        if (this.age % 20 == 0) { 
            this.rotationSpeedX *= (0.95F + this.random.nextFloat() * 0.1F);
            this.rotationSpeedY *= (0.95F + this.random.nextFloat() * 0.1F);
            this.rotationSpeedZ *= (0.95F + this.random.nextFloat() * 0.1F);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        if (this.removed) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

        Vec3 camPos = camera.getPosition();
        float offsetX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
        float offsetY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
        float offsetZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);

        float size = 0.1f;

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        
        poseStack.translate(offsetX, offsetY, offsetZ);

        
        float interpolatedRotationX = Mth.lerp(partialTicks, this.rotationX - this.rotationSpeedX, this.rotationX);
        float interpolatedRotationY = Mth.lerp(partialTicks, this.rotationY - this.rotationSpeedY, this.rotationY);
        float interpolatedRotationZ = Mth.lerp(partialTicks, this.rotationZ - this.rotationSpeedZ, this.rotationZ);

        
        poseStack.mulPose(new Quaternionf().rotationZ(interpolatedRotationZ * Mth.DEG_TO_RAD));
        poseStack.mulPose(new Quaternionf().rotationX(interpolatedRotationX * Mth.DEG_TO_RAD));
        poseStack.mulPose(new Quaternionf().rotationY(interpolatedRotationY * Mth.DEG_TO_RAD));

        
        drawCubeLines(vertexConsumer, poseStack, size);

        bufferSource.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private void drawCubeLines(VertexConsumer consumer, PoseStack poseStack, float size) {
        
        Vector3f[] vertices = {
                new Vector3f(-size, -size, -size), 
                new Vector3f( size, -size, -size), 
                new Vector3f( size, -size,  size), 
                new Vector3f(-size, -size,  size), 
                new Vector3f(-size,  size, -size), 
                new Vector3f( size,  size, -size), 
                new Vector3f( size,  size,  size), 
                new Vector3f(-size,  size,  size)  
        };

        
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0}, 
                {4, 5}, {5, 6}, {6, 7}, {7, 4}, 
                {0, 4}, {1, 5}, {2, 6}, {3, 7}  
        };

        Matrix4f matrix = poseStack.last().pose();

        for (int[] edge : edges) {
            Vector3f start = vertices[edge[0]];
            Vector3f end = vertices[edge[1]];

            drawLine(consumer, matrix, start.x(), start.y(), start.z(), end.x(), end.y(), end.z());
        }
    }

    private void drawLine(VertexConsumer consumer, Matrix4f matrix,
                          float x1, float y1, float z1, float x2, float y2, float z2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;

        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        consumer.vertex(matrix, x1, y1, z1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .normal(dx, dy, dz)
                .endVertex();

        consumer.vertex(matrix, x2, y2, z2)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .normal(dx, dy, dz)
                .endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprites) {
            this.spriteSet = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new Cube2Particle(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}