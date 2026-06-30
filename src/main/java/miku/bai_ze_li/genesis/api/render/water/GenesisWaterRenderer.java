package miku.bai_ze_li.genesis.api.render.water;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import miku.bai_ze_li.genesis.GenesisLib;
import miku.bai_ze_li.genesis.api.render.shader.GenesisShaderCompat;
import miku.bai_ze_li.genesis.api.render.shader.GenesisShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = GenesisLib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class GenesisWaterRenderer {
    private static TextureTarget sceneCopy;
    private static int copiedFrame = -1;
    private static boolean copyReady;
    private static boolean copyFailed;

    private GenesisWaterRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            prepareSceneCopy(event.getRenderTick());
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            copyReady = false;
        }
    }

    public static GenesisWaterQuality effectiveQuality() {
        if (!GenesisWaterConfig.ENABLED.get()) {
            return GenesisWaterQuality.OFF;
        }

        GenesisWaterQuality quality = GenesisWaterConfig.QUALITY.get();
        if (quality == GenesisWaterQuality.HIGH && (GenesisShaderCompat.shouldDeferWorldEffects() || copyFailed)) {
            return GenesisWaterQuality.MEDIUM;
        }
        return quality;
    }

    public static boolean useSpecialWaterRenderType() {
        if (!GenesisWaterConfig.ENABLED.get() || !GenesisWaterConfig.AFFECT_CUSTOM_FLUIDS.get()) {
            return false;
        }
        GenesisWaterQuality quality = GenesisWaterConfig.QUALITY.get();
        return GenesisWaterConfig.AFFECT_CUSTOM_FLUIDS.get() && quality != GenesisWaterQuality.OFF;
    }

    public static void applyWaterUniforms() {
        ShaderInstance shader = GenesisShaders.getWaterRefractionShader();
        if (shader == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget main = minecraft.getMainRenderTarget();
        GenesisWaterQuality quality = effectiveQuality();
        boolean high = quality == GenesisWaterQuality.HIGH && copyReady && sceneCopy != null;

        shader.setSampler("SceneSampler", high ? sceneCopy.getColorTextureId() : main.getColorTextureId());
        set2(shader, "ScreenSize", Math.max(1.0F, main.width), Math.max(1.0F, main.height));
        set1(shader, "Time", (minecraft.level == null ? 0.0F : (minecraft.level.getGameTime() + minecraft.getFrameTime())) / 20.0F);
        set4(shader, "TintColor", 0.56F, 0.88F, 0.94F, 1.0F);
        set1(shader, "Alpha", clamp((float) GenesisWaterConfig.ALPHA.get().doubleValue(), 0.05F, 0.95F));
        set1(shader, "RefractionStrength", high ? (float) GenesisWaterConfig.REFRACTION_STRENGTH.get().doubleValue() : 0.0F);
        set1(shader, "EdgeWobbleStrength", (float) GenesisWaterConfig.EDGE_WOBBLE_STRENGTH.get().doubleValue());
        set1(shader, "WaveStrength", quality == GenesisWaterQuality.LOW ? 0.18F : (float) GenesisWaterConfig.WAVE_STRENGTH.get().doubleValue());
        set1(shader, "WaveScale", (float) GenesisWaterConfig.WAVE_SCALE.get().doubleValue());
        set1(shader, "FlowSpeed", (float) GenesisWaterConfig.FLOW_SPEED.get().doubleValue());
        set1(shader, "DistanceFadeStart", (float) GenesisWaterConfig.DISTANCE_FADE_START.get().doubleValue());
        set1(shader, "DistanceFadeEnd", (float) GenesisWaterConfig.DISTANCE_FADE_END.get().doubleValue());
        set1(shader, "EdgeFadeStrength", (float) GenesisWaterConfig.EDGE_FADE_STRENGTH.get().doubleValue());
        set1(shader, "Quality", quality.ordinal());

        if (minecraft.gameRenderer != null) {
            var camera = minecraft.gameRenderer.getMainCamera();
            var pos = camera.getPosition();
            set3(shader, "CameraPosition", (float) pos.x, (float) pos.y, (float) pos.z);
        }
    }

    public static void clear() {
        copyReady = false;
        copyFailed = false;
        copiedFrame = -1;
        if (sceneCopy != null) {
            sceneCopy.destroyBuffers();
            sceneCopy = null;
        }
    }

    private static void prepareSceneCopy(int renderTick) {
        copyReady = false;
        if (effectiveQuality() != GenesisWaterQuality.HIGH) {
            return;
        }
        if (copiedFrame == renderTick) {
            copyReady = sceneCopy != null;
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        RenderTarget main = minecraft.getMainRenderTarget();
        try {
            sceneCopy = syncTarget(sceneCopy, main.width, main.height);
            copyMainColor(main, sceneCopy);
            copiedFrame = renderTick;
            copyReady = true;
            copyFailed = false;
            main.bindWrite(false);
        } catch (Throwable throwable) {
            copyReady = false;
            copyFailed = true;
            main.bindWrite(false);
            GenesisLib.LOGGER.warn("Source water scene copy failed; falling back to medium water quality", throwable);
        }
    }

    private static TextureTarget syncTarget(@Nullable TextureTarget target, int width, int height) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        if (target == null) {
            return new TextureTarget(safeWidth, safeHeight, false, Minecraft.ON_OSX);
        }
        if (target.width != safeWidth || target.height != safeHeight) {
            target.resize(safeWidth, safeHeight, Minecraft.ON_OSX);
        }
        return target;
    }

    private static void copyMainColor(RenderTarget main, TextureTarget target) {
        RenderSystem.assertOnRenderThread();
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

    private static void set1(ShaderInstance shader, String name, float value) {
        var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void set2(ShaderInstance shader, String name, float x, float y) {
        var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y);
        }
    }

    private static void set3(ShaderInstance shader, String name, float x, float y, float z) {
        var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y, z);
        }
    }

    private static void set4(ShaderInstance shader, String name, float x, float y, float z, float w) {
        var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y, z, w);
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
