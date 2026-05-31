package miku.bai_ze_li.genesis.render.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class TrailRenderApi {
    private static final float CONE_LENGTH = 1.9F;
    private static final int CONE_SIDES = 8;

    private TrailRenderApi() {
    }

    public static void renderTrail(List<Vec3> worldPositions, PoseStack poseStack, MultiBufferSource buffer,
                                   TrailRenderStyle style, float time, int entityId) {
        if (worldPositions.size() < 3) {
            return;
        }

        List<Vec3> workingTrail = new ArrayList<>();
        for (int i = 1; i < worldPositions.size(); i++) {
            workingTrail.add(worldPositions.get(i));
        }
        if (workingTrail.size() < 2) {
            return;
        }

        Vec3 renderOrigin = workingTrail.get(workingTrail.size() - 1);
        List<Vec3> relativePositions = new ArrayList<>();
        for (Vec3 pos : workingTrail) {
            relativePositions.add(pos.subtract(renderOrigin));
        }

        renderTrailStrip(relativePositions, poseStack, buffer, style, style.texture(), time, false, entityId);
        if (style.overlayTexture() != null) {
            renderTrailStrip(relativePositions, poseStack, buffer, style, style.overlayTexture(), time, true, entityId);
        }
        if (!style.headless() && style.coneTexture() != null) {
            renderTrailCone(relativePositions, poseStack, buffer, style, time, false, entityId);
            renderTrailCone(relativePositions, poseStack, buffer, style, time, true, entityId);
        }
    }

    private static RenderType renderType(TrailRenderStyle style, ResourceLocation texture) {
        if (style.renderTypeProvider() != null) {
            return style.renderTypeProvider().renderType(style, texture);
        }
        return style.emissive() ? RenderType.entityTranslucentEmissive(texture) : RenderType.entityTranslucent(texture);
    }

    private static void renderTrailStrip(List<Vec3> positions, PoseStack poseStack, MultiBufferSource buffer,
                                         TrailRenderStyle style, ResourceLocation texture, float time, boolean shadow,
                                         int entityId) {
        poseStack.pushPose();

        VertexConsumer consumer = buffer.getBuffer(renderType(style, texture));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        int planeCount = 2;
        List<Vec3> directions = TrailHelp.calculateSmoothDirections(positions);
        List<Vec3[]> perpendicularSets = TrailHelp.calculatePerpendicularSets(directions, planeCount);
        for (int plane = 0; plane < planeCount; plane++) {
            renderContinuousTrailPlane(positions, perpendicularSets, plane, consumer, matrix, normal, time, shadow, style, entityId);
        }

        poseStack.popPose();
    }

    private static void renderContinuousTrailPlane(List<Vec3> positions, List<Vec3[]> perpendicularSets, int planeIndex,
                                                   VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                                   float time, boolean shadow, TrailRenderStyle style, int entityId) {
        final int subdivisions = 5;
        for (int i = 0; i < positions.size() - 1; i++) {
            Vec3 pos1 = positions.get(i);
            Vec3 pos2 = positions.get(i + 1);
            Vec3 perpendicular1 = perpendicularSets.get(i)[planeIndex];
            Vec3 perpendicular2 = perpendicularSets.get(i + 1)[planeIndex];

            float baseProgress1 = (float) i / (positions.size() - 1);
            float baseProgress2 = (float) (i + 1) / (positions.size() - 1);

            for (int sub = 0; sub < subdivisions; sub++) {
                float t1 = (float) sub / subdivisions;
                float t2 = (float) (sub + 1) / subdivisions;

                Vec3 subPos1 = TrailHelp.lerpVec3(pos1, pos2, t1);
                Vec3 subPos2 = TrailHelp.lerpVec3(pos1, pos2, t2);
                Vec3 subPerpendicular1 = TrailHelp.lerpVec3(perpendicular1, perpendicular2, t1).normalize();
                Vec3 subPerpendicular2 = TrailHelp.lerpVec3(perpendicular1, perpendicular2, t2).normalize();

                float subProgress1 = Mth.lerp(t1, baseProgress1, baseProgress2);
                float subProgress2 = Mth.lerp(t2, baseProgress1, baseProgress2);
                float headTaper = style.headless() ? 0.18F : 0.05F;
                float width1 = TrailHelp.calculateTrailWidth(subProgress1, headTaper, style.width());
                float width2 = TrailHelp.calculateTrailWidth(subProgress2, headTaper, style.width());
                float alpha1 = TrailHelp.calculateAlpha(subProgress1, shadow) * style.alphaMultiplier();
                float alpha2 = TrailHelp.calculateAlpha(subProgress2, shadow) * style.alphaMultiplier();
                float[] color1 = style.color(subProgress1, time, entityId);
                float[] color2 = style.color(subProgress2, time, entityId);

                Vec3 offset1 = subPerpendicular1.scale(width1);
                Vec3 offset2 = subPerpendicular2.scale(width2);
                Vec3 topLeft = subPos1.add(offset1);
                Vec3 bottomLeft = subPos1.subtract(offset1);
                Vec3 topRight = subPos2.add(offset2);
                Vec3 bottomRight = subPos2.subtract(offset2);

                TrailHelp.addVertexWithColor(consumer, matrix, normal, bottomLeft, color1, alpha1, subProgress1, 0.0F, LightTexture.FULL_BRIGHT);
                TrailHelp.addVertexWithColor(consumer, matrix, normal, bottomRight, color2, alpha2, subProgress2, 0.0F, LightTexture.FULL_BRIGHT);
                TrailHelp.addVertexWithColor(consumer, matrix, normal, topRight, color2, alpha2, subProgress2, 1.0F, LightTexture.FULL_BRIGHT);
                TrailHelp.addVertexWithColor(consumer, matrix, normal, topLeft, color1, alpha1, subProgress1, 1.0F, LightTexture.FULL_BRIGHT);
            }
        }
    }

    private static void renderTrailCone(List<Vec3> positions, PoseStack poseStack, MultiBufferSource buffer,
                                        TrailRenderStyle style, float time, boolean inner, int entityId) {
        ResourceLocation coneTexture = style.coneTexture();
        if (coneTexture == null || positions.size() < 2) {
            return;
        }

        poseStack.pushPose();

        float coneBaseRadius = 0.8F * style.width();
        float scale = inner ? coneBaseRadius : coneBaseRadius + 0.5F;
        float length = inner ? CONE_LENGTH : CONE_LENGTH + 0.2F;
        int sides = inner ? CONE_SIDES : 32;
        VertexConsumer consumer = buffer.getBuffer(renderType(style, coneTexture));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        Vec3 headPos = positions.get(positions.size() - 1);
        Vec3 neckPos = positions.size() > 1 ? positions.get(positions.size() - 2) : headPos;
        Vec3 bodyPos = positions.size() > 2 ? positions.get(positions.size() - 3) : neckPos;
        Vec3 direction = headPos.subtract(neckPos).normalize();
        if (direction.lengthSqr() < 0.001D) {
            direction = neckPos.subtract(bodyPos).normalize();
        }
        if (direction.lengthSqr() < 0.001D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        }

        Vec3 coneTop = headPos.add(direction.scale(length * 0.3F));
        Vec3 coneBottom = headPos.subtract(direction.scale(length * 0.7F));
        Vec3 perpendicular1 = TrailHelp.calculatePerpendicular(direction);
        Vec3 perpendicular2 = direction.cross(perpendicular1).normalize();

        renderConeGeometry(consumer, matrix, normal, coneTop, coneBottom, perpendicular1, perpendicular2,
                time, scale, sides, style, entityId);

        poseStack.popPose();
    }

    private static void renderConeGeometry(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Vec3 top,
                                           Vec3 bottom, Vec3 perp1, Vec3 perp2, float time, float scale,
                                           int sides, TrailRenderStyle style, int entityId) {
        Vec3[] bottomVertices = new Vec3[sides];
        for (int i = 0; i < sides; i++) {
            float angle = (float) (i * 2.0D * Math.PI / sides);
            Vec3 offset = perp1.scale(Math.cos(angle) * scale).add(perp2.scale(Math.sin(angle) * scale));
            bottomVertices[i] = bottom.add(offset);
        }

        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            float[] topColor = style.color(1.0F, time, entityId);
            float[] bottomColor = style.color(0.7F, time, entityId);

            TrailHelp.addVertexWithColor(consumer, matrix, normal, top, topColor, 1.0F, 0.5F, 1.0F, LightTexture.FULL_BRIGHT);
            TrailHelp.addVertexWithColor(consumer, matrix, normal, bottomVertices[i], bottomColor, 0.0F, (float) i / sides, 0.0F, LightTexture.FULL_BRIGHT);
            TrailHelp.addVertexWithColor(consumer, matrix, normal, bottomVertices[next], bottomColor, 0.0F, (float) next / sides, 0.0F, LightTexture.FULL_BRIGHT);
        }
    }
}

