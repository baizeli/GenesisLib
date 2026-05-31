package miku.bai_ze_li.genesis.render.api;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class TrailHelp {

    public static float calculateTrailWidth(float progress, float gsStartPos, float trailWidth) {
        float baseWidth = trailWidth * Math.max(0.01f, progress);
        if (progress >= 1.0f - gsStartPos && gsStartPos != 0.0f) {
            float headProgress = (progress - (1.0f - gsStartPos)) / 0.05f;
            float narrowFactor = 1.0f - smoothStep(headProgress);
            baseWidth *= Math.max(narrowFactor, 0.01f);
        }
        return baseWidth;
    }

    private static float smoothStep(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    public static float calculateAlpha(float progress, boolean isShadow) {
        if (progress > 0.3f) {
            return isShadow ? 0.3f : 0.8f;
        } else {
            float fade = progress / 0.3f;
            return fade * (isShadow ? 0.3f : 0.8f);
        }
    }

    public static List<Vec3[]> calculatePerpendicularSets(List<Vec3> directions, int planeCount) {
        List<Vec3[]> result = new ArrayList<>();
        for (Vec3 dir : directions) {
            Vec3[] perps = new Vec3[planeCount];
            Vec3 ref = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 perp1 = ref.cross(dir).normalize();
            Vec3 perp2 = dir.cross(perp1).normalize();
            for (int p = 0; p < planeCount; p++) {
                double angle = p * Math.PI / planeCount;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                perps[p] = perp1.scale(cos).add(perp2.scale(sin)).normalize();
            }
            result.add(perps);
        }
        return result;
    }

    public static List<Vec3> calculateSmoothDirections(List<Vec3> positions) {
        List<Vec3> directions = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            Vec3 dir;
            if (i == 0) {
                dir = positions.get(1).subtract(positions.get(0)).normalize();
            } else if (i == positions.size() - 1) {
                dir = positions.get(i).subtract(positions.get(i - 1)).normalize();
            } else {
                Vec3 prev = positions.get(i).subtract(positions.get(i - 1)).normalize();
                Vec3 next = positions.get(i + 1).subtract(positions.get(i)).normalize();
                Vec3 sum = prev.add(next);
                dir = sum.lengthSqr() < 0.001 ? prev : sum.normalize();
            }
            directions.add(dir);
        }
        return directions;
    }

    public static float[] interpolateGradientColor(float position, List<Integer> colors) {
        if (colors.isEmpty()) return new float[]{1, 1, 1};
        if (colors.size() == 1) return hexToRgb(colors.get(0));

        float scaled = position * (colors.size() - 1);
        int idx = (int) Math.floor(scaled);
        float frac = scaled - idx;
        idx = Mth.clamp(idx, 0, colors.size() - 2);

        float[] c1 = hexToRgb(colors.get(idx));
        float[] c2 = hexToRgb(colors.get(idx + 1));
        return new float[]{
                Mth.lerp(frac, c1[0], c2[0]),
                Mth.lerp(frac, c1[1], c2[1]),
                Mth.lerp(frac, c1[2], c2[2])
        };
    }

    private static float[] hexToRgb(int hex) {
        float r = ((hex >> 16) & 0xFF) / 255.0f;
        float g = ((hex >> 8) & 0xFF) / 255.0f;
        float b = (hex & 0xFF) / 255.0f;
        return new float[]{r, g, b};
    }

    public static Vec3 lerpVec3(Vec3 a, Vec3 b, float t) {
        return new Vec3(
                Mth.lerp(t, a.x, b.x),
                Mth.lerp(t, a.y, b.y),
                Mth.lerp(t, a.z, b.z)
        );
    }

    public static Vec3 calculatePerpendicular(Vec3 dir) {
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 perp = dir.cross(up);
        if (perp.lengthSqr() < 0.001) {
            perp = dir.cross(new Vec3(1, 0, 0));
        }
        return perp.normalize();
    }

    public static Vec3[] calculatePerpendicularVectors(Vec3 dir) {
        Vec3 up = Math.abs(dir.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 right = dir.cross(up).normalize();
        Vec3 actualUp = right.cross(dir).normalize();
        Vec3[] perps = new Vec3[4];
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2.0;
            perps[i] = right.scale(Math.cos(angle)).add(actualUp.scale(Math.sin(angle)));
        }
        return perps;
    }

    public static void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                 Vec3 pos, float[] color, float alpha, float u, float v, int packedLight) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(color[0], color[1], color[2], alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    public static void addVertexWithColor(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                          Vec3 pos, float[] color, float alpha, float u, float v, int packedLight) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(color[0], color[1], color[2], alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }
}

