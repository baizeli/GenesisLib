package miku.bai_ze_li.genesis.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import miku.bai_ze_li.genesis.api.render.outline.GenesisEffect;
import miku.bai_ze_li.genesis.api.render.outline.GenesisItemOutlineRegistry;
import miku.bai_ze_li.genesis.api.render.outline.GenesisOutlineRenderer;
import miku.bai_ze_li.genesis.api.render.shader.GenesisShaderCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    Minecraft minecraft;

    @Shadow
    @Final
    public ItemInHandRenderer itemInHandRenderer;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Final
    private LightTexture lightTexture;

    @Unique
    private Matrix4f genesis$handOutlinePose;

    @Unique
    private Matrix3f genesis$handOutlineNormal;

    @Inject(
            method = "renderItemInHand",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LightTexture;turnOnLightLayer()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void genesis$rememberHandOutlinePose(PoseStack poseStack, Camera activeRenderInfo, float partialTicks,
                                                 CallbackInfo ci) {
        if (GenesisOutlineRenderer.isHandMaskCaptureActive()) {
            return;
        }
        genesis$handOutlinePose = new Matrix4f(poseStack.last().pose());
        genesis$handOutlineNormal = new Matrix3f(poseStack.last().normal());
    }

    @Inject(
            method = "renderItemInHand",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LightTexture;turnOffLightLayer()V",
                    shift = At.Shift.AFTER
            )
    )
    private void genesis$captureHandOutlineMasks(PoseStack poseStack, Camera activeRenderInfo, float partialTicks,
                                                 CallbackInfo ci) {
        Matrix4f originalPose = genesis$handOutlinePose;
        Matrix3f originalNormal = genesis$handOutlineNormal;
        genesis$handOutlinePose = null;
        genesis$handOutlineNormal = null;

        if (originalPose == null || originalNormal == null) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();
        int packedLight = minecraft.getEntityRenderDispatcher().getPackedLightCoords(player, partialTicks);
        if (GenesisShaderCompat.shouldDeferWorldEffects()) {
            GenesisOutlineRenderer.queueDeferredHandOutline(
                    originalPose,
                    originalNormal,
                    new Matrix4f(RenderSystem.getProjectionMatrix()),
                    partialTicks,
                    packedLight
            );
            return;
        }

        genesis$captureHandMask(partialTicks, poseStack, bufferSource, player, packedLight,
                originalPose, originalNormal, InteractionHand.MAIN_HAND);
        genesis$captureHandMask(partialTicks, poseStack, bufferSource, player, packedLight,
                originalPose, originalNormal, InteractionHand.OFF_HAND);
    }

    @Unique
    private void genesis$captureHandMask(float partialTicks, PoseStack poseStack,
                                         MultiBufferSource.BufferSource bufferSource, LocalPlayer player,
                                         int packedLight, Matrix4f originalPose,
                                         Matrix3f originalNormal, InteractionHand hand) {
        ItemStack stack = hand == InteractionHand.MAIN_HAND ? player.getMainHandItem() : player.getOffhandItem();
        GenesisEffect effect = GenesisItemOutlineRegistry.resolve(stack);
        if (effect == null || !GenesisOutlineRenderer.beginHandMaskCapture(effect, hand)) {
            return;
        }

        lightTexture.turnOnLightLayer();
        try {
            poseStack.last().pose().set(originalPose);
            poseStack.last().normal().set(originalNormal);
            itemInHandRenderer.renderHandsWithItems(partialTicks, poseStack, bufferSource, player, packedLight);
            bufferSource.endBatch();
        } finally {
            GenesisOutlineRenderer.stopHandMaskCapture();
            lightTexture.turnOffLightLayer();
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void genesis$renderDeferredOutlines(float partialTicks, long finishTimeNano, boolean renderLevel, CallbackInfo ci) {
        if (renderLevel) {
            GenesisOutlineRenderer.renderDeferredOutlines(partialTicks, itemInHandRenderer, lightTexture);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void genesis$flushRemainingGuiOutline(float partialTicks, long finishTimeNano, boolean renderLevel, CallbackInfo ci) {
        GenesisOutlineRenderer.flushGuiPass();
    }
}
