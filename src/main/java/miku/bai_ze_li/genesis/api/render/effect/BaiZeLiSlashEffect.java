package miku.bai_ze_li.genesis.api.render.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import miku.bai_ze_li.genesis.api.render.shader.GenesisRenderType;
import miku.bai_ze_li.genesis.api.render.shader.GenesisShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Random;

public class BaiZeLiSlashEffect {

    private static final float RADIUS = 1.0F;
    private static final int LIFETIME = 18;
    private static final int SEGMENTS = 20;

    private int age = 0;
    private final Vec3 center;
    private final float yaw;
    private final float pitch;
    private final float slashTilt;
    private final boolean leftToRight;

    private boolean finished = false;
    private static final Random RANDOM = new Random();

    public BaiZeLiSlashEffect(Vec3 center, float yaw, float pitch) {
        this.center = center;
        this.yaw = yaw;
        this.pitch = pitch;

        this.leftToRight = RANDOM.nextBoolean();
        float baseTilt = 15.0F + RANDOM.nextFloat() * 45.0F;
        this.slashTilt = leftToRight ? -baseTilt : baseTilt;
    }

    public void tick() {
        age++;
        if (age >= LIFETIME) finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, float partialTick) {
        if (finished) return;

        float exactAge = age + partialTick;
        float progress = exactAge / (float) LIFETIME;
        if (progress >= 1.0F) return;

        float alpha = smoothStep(0.0F, 0.18F, progress) * (1.0F - smoothStep(0.62F, 1.0F, progress));
        if (alpha <= 0.01F) return;

        float cleaveProgress = Mth.clamp(smoothStep(0.0F, 1.0F, exactAge / 7.0F) * 1.14F, 0.0F, 1.18F);

        GenesisShaders.setTime(GenesisShaders.getRibbonShader(), exactAge);
        VertexConsumer vc = buffer.getBuffer(GenesisRenderType.ribbon);
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(center.x - camPos.x, center.y - camPos.y, center.z - camPos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(slashTilt));
        poseStack.scale(4.8F, 1.05F, 1.0F);

        Matrix4f matrix = poseStack.last().pose();
        float rColor = cleaveProgress;
        float gColor = leftToRight ? 1.0F : 0.0F;

        float prevLeftX = 0.0F;
        float prevLeftY = 0.0F;
        float prevRightX = 0.0F;
        float prevRightY = 0.0F;
        float prevU = 0.0F;

        for (int i = 0; i <= SEGMENTS; i++) {
            float t = i / (float) SEGMENTS;
            float x = Mth.lerp(t, -RADIUS, RADIUS);
            float y = arcY(t);

            float prevT = Math.max(0.0F, t - 1.0F / SEGMENTS);
            float nextT = Math.min(1.0F, t + 1.0F / SEGMENTS);
            float prevX = Mth.lerp(prevT, -RADIUS, RADIUS);
            float nextX = Mth.lerp(nextT, -RADIUS, RADIUS);
            float prevY = arcY(prevT);
            float nextY = arcY(nextT);

            float dx = nextX - prevX;
            float dy = nextY - prevY;
            float invLen = Mth.invSqrt(dx * dx + dy * dy);
            float normalX = -dy * invLen;
            float normalY = dx * invLen;

            float taper = Mth.sin(t * Mth.PI);
            float halfWidth = (0.10F + 0.90F * (float) Math.pow(taper, 0.55F)) * 0.42F;
            float leftX = x + normalX * halfWidth;
            float leftY = y + normalY * halfWidth;
            float rightX = x - normalX * halfWidth;
            float rightY = y - normalY * halfWidth;

            if (i > 0) {
                emitVertex(vc, matrix, prevLeftX, prevLeftY, prevU, 0.0F, rColor, gColor, alpha);
                emitVertex(vc, matrix, leftX, leftY, t, 0.0F, rColor, gColor, alpha);
                emitVertex(vc, matrix, rightX, rightY, t, 1.0F, rColor, gColor, alpha);

                emitVertex(vc, matrix, prevLeftX, prevLeftY, prevU, 0.0F, rColor, gColor, alpha);
                emitVertex(vc, matrix, rightX, rightY, t, 1.0F, rColor, gColor, alpha);
                emitVertex(vc, matrix, prevRightX, prevRightY, prevU, 1.0F, rColor, gColor, alpha);
            }

            prevLeftX = leftX;
            prevLeftY = leftY;
            prevRightX = rightX;
            prevRightY = rightY;
            prevU = t;
        }

        poseStack.popPose();
    }

    private static float arcY(float t) {
        return Mth.sin((t - 0.5F) * Mth.PI) * 0.23F + Mth.sin(t * Mth.PI) * 0.08F;
    }

    private static void emitVertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float u, float v, float r, float g, float alpha) {
        vc.vertex(matrix, x, y, 0.0F).color(r, g, 1.0F, alpha).uv(u, v).endVertex();
    }

    private static float smoothStep(float edge0, float edge1, float value) {
        float t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
