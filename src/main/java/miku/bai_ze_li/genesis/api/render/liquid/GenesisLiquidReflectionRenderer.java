package miku.bai_ze_li.genesis.api.render.liquid;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import miku.bai_ze_li.genesis.GenesisLib;
import miku.bai_ze_li.genesis.api.render.shader.GenesisShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.Map;

@Mod.EventBusSubscriber(modid = GenesisLib.MODID, value = Dist.CLIENT)
public final class GenesisLiquidReflectionRenderer {
    private static TextureTarget sceneCopy;

    private GenesisLiquidReflectionRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Map<Fluid, GenesisLiquidReflectionStyle> fluids = GenesisLiquidReflectionApi.fluids();
        if (fluids.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null) {
            return;
        }

        RenderTarget main = minecraft.getMainRenderTarget();
        sceneCopy = syncBuffer(sceneCopy, main.width, main.height);
        copyMainColor(main, sceneCopy);
        main.bindWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        ShaderInstance shader = GenesisShaders.getLiquidReflectionShader();
        shader.setSampler("DiffuseSampler", sceneCopy.getColorTextureId());
        setUniform(shader, "ScreenSize", (float) main.width, (float) main.height);
        setUniform(shader, "Time", (level.getGameTime() + event.getPartialTick()) * 0.05F);

        Vec3 camera = event.getCamera().getPosition();
        BlockPos center = player.blockPosition();

        for (Map.Entry<Fluid, GenesisLiquidReflectionStyle> entry : fluids.entrySet()) {
            renderFluid(level, center, camera, event.getPoseStack(), shader, entry.getKey(), entry.getValue());
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    private static void renderFluid(
            ClientLevel level,
            BlockPos center,
            Vec3 camera,
            PoseStack poseStack,
            ShaderInstance shader,
            Fluid sourceFluid,
            GenesisLiquidReflectionStyle style
    ) {
        setUniform(shader, "ReflectionStrength", style.reflectionStrength());
        setUniform(shader, "FlowStrength", style.flowStrength());
        setUniform(shader, "FlowSpeed", style.flowSpeed());
        Vector4f tint = style.tint();
        setUniform(shader, "TintColor", tint.x(), tint.y(), tint.z(), tint.w());

        RenderSystem.setShader(() -> shader);

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        int radius = style.scanRadius();
        int verticalRadius = style.verticalRadius();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        for (int y = center.getY() - verticalRadius; y <= center.getY() + verticalRadius; y++) {
            for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
                    mutable.set(x, y, z);
                    FluidState state = level.getFluidState(mutable);
                    if (!state.isSourceOfType(sourceFluid)) {
                        continue;
                    }
                    above.set(x, y + 1, z);
                    if (level.getFluidState(above).isSourceOfType(sourceFluid)) {
                        continue;
                    }

                    float height = state.getHeight(level, mutable);
                    if (height <= 0.01F) {
                        continue;
                    }
                    float surfaceY = y + height + 0.012F;
                    addSurface(buffer, matrix, x, surfaceY, z);
                }
            }
        }

        BufferBuilder.RenderedBuffer renderedBuffer = buffer.endOrDiscardIfEmpty();
        if (renderedBuffer != null) {
            BufferUploader.drawWithShader(renderedBuffer);
        }
        poseStack.popPose();
    }

    private static void addSurface(
            BufferBuilder buffer,
            Matrix4f matrix,
            int x,
            float y,
            int z
    ) {
        buffer.vertex(matrix, x, y, z + 1).color(255, 255, 255, 255).uv(0.0F, 1.0F).endVertex();
        buffer.vertex(matrix, x + 1, y, z + 1).color(255, 255, 255, 255).uv(1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, x + 1, y, z).color(255, 255, 255, 255).uv(1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x, y, z).color(255, 255, 255, 255).uv(0.0F, 0.0F).endVertex();
    }

    private static TextureTarget syncBuffer(TextureTarget buffer, int width, int height) {
        if (buffer == null || buffer.width != width || buffer.height != height) {
            if (buffer != null) {
                buffer.destroyBuffers();
            }
            return new TextureTarget(width, height, false, Minecraft.ON_OSX);
        }
        return buffer;
    }

    private static void copyMainColor(RenderTarget main, TextureTarget target) {
        int previousRead = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousDraw = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        try {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.frameBufferId);
            GL30.glBlitFramebuffer(
                    0, 0, main.width, main.height,
                    0, 0, target.width, target.height,
                    GL11.GL_COLOR_BUFFER_BIT,
                    GL11.GL_LINEAR
            );
        } finally {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousRead);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDraw);
        }
    }

    private static void setUniform(ShaderInstance shader, String name, float value) {
        if (shader.getUniform(name) != null) {
            shader.getUniform(name).set(value);
        }
    }

    private static void setUniform(ShaderInstance shader, String name, float x, float y) {
        if (shader.getUniform(name) != null) {
            shader.getUniform(name).set(x, y);
        }
    }

    private static void setUniform(ShaderInstance shader, String name, float x, float y, float z, float w) {
        if (shader.getUniform(name) != null) {
            shader.getUniform(name).set(x, y, z, w);
        }
    }
}
