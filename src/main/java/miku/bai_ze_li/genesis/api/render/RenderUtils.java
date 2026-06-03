package miku.bai_ze_li.genesis.api.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static com.mojang.math.Axis.XP;
import static com.mojang.math.Axis.YP;
import static com.mojang.math.Axis.ZP;

public class RenderUtils extends RenderType {
    public RenderUtils(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType createTexturedQuadType(ResourceLocation texture) {
        return create("genesis_textured_quad_no_cull",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                256,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .createCompositeState(false));
    }

    public static RenderType endPortal(ResourceLocation texture) {
        return RenderType.create("genesis_end_portal", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,
                RenderType.CompositeState.builder()
                        .setLayeringState(POLYGON_OFFSET_LAYERING)
                        .setShaderState(new RenderType.ShaderStateShard(GameRenderer::getRendertypeEndPortalShader))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setTextureState(RenderType.MultiTextureStateShard.builder()
                                .add(texture, false, false)
                                .add(texture, false, false)
                                .build())
                        .createCompositeState(true));
    }

    public static void renderItemRings(PoseStack poseStack, MultiBufferSource buffer, float angle, Axis axis, int r, int g, int b, double x, double y, double z) {
        VertexConsumer line = buffer.getBuffer(RenderType.LINES);
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        if (axis == YP) {
            poseStack.mulPose(XP.rotation((float) Math.toRadians(angle)));
            renderCircleWire(poseStack.last().pose(), line, YP, r, g, b);
        } else if (axis == XP) {
            poseStack.mulPose(XP.rotationDegrees(90));
            poseStack.mulPose(YP.rotation((float) Math.toRadians(angle)));
            renderCircleWire(poseStack.last().pose(), line, XP, r, g, b);
        } else {
            poseStack.mulPose(XP.rotationDegrees(90));
            poseStack.mulPose(ZP.rotationDegrees(angle));
            renderCircleWire(poseStack.last().pose(), line, ZP, r, g, b);
        }

        poseStack.popPose();
    }

    public static void renderCircleWire(Matrix4f mat, VertexConsumer vc, Axis axis, int r, int g, int b) {
        int segments = 256;
        float radius = 0.5F;
        float rf = r / 255.0F;
        float gf = g / 255.0F;
        float bf = b / 255.0F;

        for (int i = 0; i < segments; i++) {
            float angle1 = i * 2 * (float) Math.PI / segments;
            float angle2 = (i + 1) * 2 * (float) Math.PI / segments;
            float x1 = radius * Mth.cos(angle1);
            float y1 = radius * Mth.sin(angle1);
            float x2 = radius * Mth.cos(angle2);
            float y2 = radius * Mth.sin(angle2);

            if (axis == YP) {
                vc.vertex(mat, x1, 0, y1).color(rf, gf, bf, 1.0F).normal(1, 0, 0).endVertex();
                vc.vertex(mat, x2, 0, y2).color(rf, gf, bf, 1.0F).normal(0, 1, 0).endVertex();
            } else if (axis == XP) {
                vc.vertex(mat, 0, x1, y1).color(rf, gf, bf, 1.0F).normal(1, 0, 0).endVertex();
                vc.vertex(mat, 0, x2, y2).color(rf, gf, bf, 1.0F).normal(0, 1, 0).endVertex();
            } else if (axis == ZP) {
                vc.vertex(mat, x1, y1, 0).color(rf, gf, bf, 1.0F).normal(0, 0, 1).endVertex();
                vc.vertex(mat, x2, y2, 0).color(rf, gf, bf, 1.0F).normal(0, 0, 1).endVertex();
            }
        }
    }

    public static void renderWireCube(PoseStack poseStack, MultiBufferSource buffer, float halfSize, float angleDeg, Axis axis, int r, int g, int b, double x, double y, double z) {
        AABB box = new AABB(-halfSize, -halfSize, -halfSize, halfSize, halfSize, halfSize);
        VertexConsumer consumer = buffer.getBuffer(RenderType.LINES);

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        if (axis == YP) {
            poseStack.mulPose(XP.rotationDegrees(90));
            poseStack.mulPose(YP.rotationDegrees(angleDeg));
        } else if (axis == XP) {
            poseStack.mulPose(XP.rotationDegrees(angleDeg));
        } else {
            poseStack.mulPose(XP.rotationDegrees(90));
            poseStack.mulPose(ZP.rotationDegrees(angleDeg));
        }

        float rf = r / 255.0F;
        float gf = g / 255.0F;
        float bf = b / 255.0F;
        renderLineBox(poseStack, consumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, rf, gf, bf, 0.8F, rf, gf, bf);
        poseStack.popPose();
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer consumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float redMin, float greenMin, float blueMin, float alpha, float redMax, float greenMax, float blueMax) {
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        float f = (float) minX;
        float f1 = (float) minY;
        float f2 = (float) minZ;
        float f3 = (float) maxX;
        float f4 = (float) maxY;
        float f5 = (float) maxZ;
        consumer.vertex(matrix4f, f, f1, f2).color(redMin, greenMax, blueMax, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f3, f1, f2).color(redMin, greenMax, blueMax, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f, f1, f2).color(redMax, greenMin, blueMax, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f, f4, f2).color(redMax, greenMin, blueMax, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f, f1, f2).color(redMax, greenMax, blueMin, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix4f, f, f1, f5).color(redMax, greenMax, blueMin, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix4f, f3, f1, f2).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f3, f4, f2).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f3, f4, f2).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f, f4, f2).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f, f4, f2).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix4f, f, f4, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix4f, f, f4, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f, f1, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f, f1, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f3, f1, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f3, f1, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix4f, f3, f1, f2).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix4f, f, f4, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f3, f4, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f3, f1, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f3, f4, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, f3, f4, f2).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix4f, f3, f4, f5).color(redMin, greenMin, blueMin, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
    }

    public static void renderWireTriangle(PoseStack poseStack, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float rf, float gf, float bf, float alpha) {
        Matrix4f mat4 = poseStack.last().pose();
        Matrix3f mat3 = poseStack.last().normal();
        consumer.vertex(mat4, x1, y1, z1).color(rf, gf, bf, alpha).normal(mat3, 0, 0, 0).endVertex();
        consumer.vertex(mat4, x2, y2, z2).color(rf, gf, bf, alpha).normal(mat3, 0, 0, 0).endVertex();
        consumer.vertex(mat4, x2, y2, z2).color(rf, gf, bf, alpha).normal(mat3, 0, 0, 0).endVertex();
        consumer.vertex(mat4, x3, y3, z3).color(rf, gf, bf, alpha).normal(mat3, 0, 0, 0).endVertex();
        consumer.vertex(mat4, x3, y3, z3).color(rf, gf, bf, alpha).normal(mat3, 0, 0, 0).endVertex();
        consumer.vertex(mat4, x1, y1, z1).color(rf, gf, bf, alpha).normal(mat3, 0, 0, 0).endVertex();
    }

    public static void renderWireTriangle(PoseStack poseStack, MultiBufferSource buffer, float angleDeg, Axis axis, int r, int g, int b, double x, double y, double z) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.LINES);
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        if (axis == YP) {
            poseStack.mulPose(XP.rotationDegrees(90));
            poseStack.mulPose(YP.rotationDegrees(angleDeg));
        } else if (axis == XP) {
            poseStack.mulPose(XP.rotationDegrees(angleDeg));
        } else {
            poseStack.mulPose(ZP.rotationDegrees(angleDeg));
        }

        float rf = r / 255.0F;
        float gf = g / 255.0F;
        float bf = b / 255.0F;
        float h = 0.866F;
        renderWireTriangle(poseStack, consumer, 0.0F, h * 2.0F / 3.0F, 0.0F, -0.5F, -h / 3.0F, 0.0F, 0.5F, -h / 3.0F, 0.0F, rf, gf, bf, 0.8F);
        poseStack.popPose();
    }

    public static void renderNormalTexturedQuad(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture, float width, float height, float angleDeg, Axis axis, double x, double y, double z, int light) {
        VertexConsumer consumer = buffer.getBuffer(createTexturedQuadType(texture));
        renderTextured(poseStack, consumer, width, height, angleDeg, axis, x, y, z, light);
    }

    public static void renderTextured(PoseStack poseStack, VertexConsumer consumer, float width, float height, float angleDeg, Axis axis, double x, double y, double z, int light) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(axis.rotationDegrees(angleDeg));

        float w = width / 2.0F;
        float h = height / 2.0F;
        Matrix4f matrix = poseStack.last().pose();
        consumer.vertex(matrix, -w, -h, 0).color(255, 255, 255, 255).uv(0, 1).uv2(light).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, w, -h, 0).color(255, 255, 255, 255).uv(1, 1).uv2(light).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, w, h, 0).color(255, 255, 255, 255).uv(1, 0).uv2(light).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, -w, h, 0).color(255, 255, 255, 255).uv(0, 0).uv2(light).normal(0, 0, 1).endVertex();
        poseStack.popPose();
    }

    public static void fixRot(PoseStack poseStack, LivingEntity entity, float partialTick) {
        poseStack.mulPose(YP.rotationDegrees(-Mth.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot)));
    }
}
