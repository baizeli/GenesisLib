package miku.bai_ze_li.genesis.api.render.effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import miku.bai_ze_li.genesis.api.render.shader.GenesisShaderCompat;
import miku.bai_ze_li.genesis.mixin.client.GameRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SlashEffectManager {

    private static final int MAX_SLASH_EFFECTS = 256;
    private static final List<BaiZeLiSlashEffect> EFFECTS = new ArrayList<>();
    private static RenderContext lastContext;

    public static void add(BaiZeLiSlashEffect effect) {
        while (EFFECTS.size() >= MAX_SLASH_EFFECTS) {
            EFFECTS.remove(0);
        }
        EFFECTS.add(effect);
    }

    public static void captureLevelRenderContext(PoseStack poseStack, float partialTick, Camera camera, Matrix4f projectionMatrix) {
        if (!GenesisShaderCompat.shouldDeferWorldEffects()) {
            lastContext = null;
            return;
        }
        lastContext = new RenderContext(
                new Matrix4f(poseStack.last().pose()),
                new Matrix4f(projectionMatrix),
                partialTick
        );
    }

    public static void tick() {
        Iterator<BaiZeLiSlashEffect> it = EFFECTS.iterator();
        while (it.hasNext()) {
            BaiZeLiSlashEffect e = it.next();
            e.tick();
            if (e.isFinished()) {
                it.remove();
            }
        }
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, float partialTick) {
        for (BaiZeLiSlashEffect e : EFFECTS) {
            e.render(poseStack, buffer, partialTick);
        }
    }

    public static void renderDeferred(PoseStack poseStack, MultiBufferSource buffer, float partialTick) {
        render(poseStack, buffer, partialTick);
    }

    public static void renderDeferred(float partialTick) {
        if (!GenesisShaderCompat.shouldDeferWorldEffects()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            clear();
            lastContext = null;
            return;
        }

        RenderSystem.backupProjectionMatrix();
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();

        try {
            Camera camera = minecraft.gameRenderer.getMainCamera();
            RenderContext context = lastContext;
            Matrix4f projection = context != null
                    ? new Matrix4f(context.projection)
                    : createProjectionMatrix(minecraft, camera, partialTick);

            RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.setIdentity();
            modelViewStack.mulPoseMatrix(createModelViewMatrix(context, camera));
            RenderSystem.applyModelViewMatrix();

            minecraft.getMainRenderTarget().bindWrite(false);
            MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            renderDeferred(new PoseStack(), bufferSource, context != null ? context.partialTick : partialTick);
            bufferSource.endBatch();
        } finally {
            modelViewStack.popPose();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.applyModelViewMatrix();
            lastContext = null;
        }
    }

    public static void clear() {
        EFFECTS.clear();
        lastContext = null;
    }

    private static Matrix4f createProjectionMatrix(Minecraft minecraft, Camera camera, float partialTick) {
        double fov = ((GameRendererAccessor) minecraft.gameRenderer).callGetFov(camera, partialTick, true);
        return minecraft.gameRenderer.getProjectionMatrix(fov);
    }

    private static Matrix4f createModelViewMatrix(RenderContext context, Camera camera) {
        if (context != null) {
            return new Matrix4f(context.modelView);
        }

        PoseStack viewPoseStack = new PoseStack();
        viewPoseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        viewPoseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        return new Matrix4f(viewPoseStack.last().pose());
    }

    private static final class RenderContext {
        private final Matrix4f modelView;
        private final Matrix4f projection;
        private final float partialTick;

        private RenderContext(Matrix4f modelView, Matrix4f projection, float partialTick) {
            this.modelView = modelView;
            this.projection = projection;
            this.partialTick = partialTick;
        }
    }
}
