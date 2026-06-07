package miku.bai_ze_li.genesis.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import miku.bai_ze_li.genesis.api.render.effect.SlashEffectManager;
import miku.bai_ze_li.genesis.api.render.outline.GenesisOutlineRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void genesis$captureOutlineRenderContext(PoseStack poseStack, float partialTick, long finishTimeNano,
                                                     boolean renderBlockOutline, Camera camera,
                                                     GameRenderer gameRenderer, LightTexture lightTexture,
                                                     Matrix4f projectionMatrix, CallbackInfo ci) {
        SlashEffectManager.captureLevelRenderContext(poseStack, partialTick, camera, projectionMatrix);
        GenesisOutlineRenderer.captureLevelRenderContext(poseStack, partialTick, camera, projectionMatrix);
    }
}
