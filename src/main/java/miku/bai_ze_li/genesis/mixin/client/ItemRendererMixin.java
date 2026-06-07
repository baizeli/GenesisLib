package miku.bai_ze_li.genesis.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import miku.bai_ze_li.genesis.api.render.outline.GenesisEffect;
import miku.bai_ze_li.genesis.api.render.outline.GenesisItemOutlineRegistry;
import miku.bai_ze_li.genesis.api.render.outline.GenesisOutlineRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = 950)
public class ItemRendererMixin {
    @Unique
    private static boolean genesis$captureMaskPass;

    @Inject(method = "render", at = @At("RETURN"))
    private void genesis$captureGuiOutlineMask(ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                               PoseStack poseStack, MultiBufferSource bufferSource,
                                               int packedLight, int packedOverlay, BakedModel model,
                                               CallbackInfo ci) {
        if (genesis$captureMaskPass || context != ItemDisplayContext.GUI) {
            return;
        }

        GenesisEffect effect = GenesisItemOutlineRegistry.resolve(stack);
        if (effect == null) {
            return;
        }

        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }

        if (!GenesisOutlineRenderer.beginGuiMaskCapture(effect)) {
            return;
        }

        genesis$captureMaskPass = true;
        try {
            ((ItemRenderer) (Object) this).render(stack, context, leftHand, poseStack, bufferSource, packedLight, packedOverlay, model);
            if (bufferSource instanceof MultiBufferSource.BufferSource maskSource) {
                maskSource.endBatch();
            }
        } finally {
            genesis$captureMaskPass = false;
            GenesisOutlineRenderer.stopGuiMaskCapture();
        }
    }
}
