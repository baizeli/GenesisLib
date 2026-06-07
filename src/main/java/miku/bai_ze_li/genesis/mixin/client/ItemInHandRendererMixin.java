package miku.bai_ze_li.genesis.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import miku.bai_ze_li.genesis.api.render.outline.GenesisEffect;
import miku.bai_ze_li.genesis.api.render.outline.GenesisItemOutlineRegistry;
import miku.bai_ze_li.genesis.api.render.outline.GenesisOutlineRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void genesis$filterHandMaskItem(AbstractClientPlayer player, float partialTicks, float pitch,
                                            InteractionHand hand, float swingProgress, ItemStack stack,
                                            float equipProgress, PoseStack poseStack,
                                            MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {
        if (GenesisOutlineRenderer.isHandMaskCaptureActive()) {
            GenesisEffect effect = GenesisItemOutlineRegistry.resolve(stack);
            if (effect == null || GenesisOutlineRenderer.shouldSkipHandMask(hand)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderPlayerArm", at = @At("HEAD"), cancellable = true)
    private void genesis$skipPlayerArmForHandMask(PoseStack poseStack, MultiBufferSource bufferSource,
                                                  int packedLight, float equippedProgress, float swingProgress,
                                                  HumanoidArm side, CallbackInfo ci) {
        if (GenesisOutlineRenderer.isHandMaskCaptureActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderMapHand", at = @At("HEAD"), cancellable = true)
    private void genesis$skipMapHandForHandMask(PoseStack poseStack, MultiBufferSource bufferSource,
                                                int packedLight, HumanoidArm side, CallbackInfo ci) {
        if (GenesisOutlineRenderer.isHandMaskCaptureActive()) {
            ci.cancel();
        }
    }
}
