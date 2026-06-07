package miku.bai_ze_li.genesis.mixin.client;

import miku.bai_ze_li.genesis.api.render.outline.GenesisOutlineRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractContainerScreen.class, priority = 950)
public class GenesisAbstractContainerScreenMixin {
    @Inject(method = "renderTooltip", at = @At("HEAD"))
    private void genesis$flushContainerGuiOutlineBeforeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                                               CallbackInfo ci) {
        guiGraphics.flush();
        GenesisOutlineRenderer.flushGuiPass();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void genesis$flushContainerGuiOutline(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick,
                                                  CallbackInfo ci) {
        guiGraphics.flush();
        GenesisOutlineRenderer.flushGuiPass();
    }
}
