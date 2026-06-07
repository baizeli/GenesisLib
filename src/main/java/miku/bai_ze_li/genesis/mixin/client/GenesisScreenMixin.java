package miku.bai_ze_li.genesis.mixin.client;

import miku.bai_ze_li.genesis.api.render.outline.GenesisOutlineRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class, priority = 950)
public class GenesisScreenMixin {
    @Inject(
            method = "renderWithTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void genesis$flushGuiOutlineBeforeDeferredTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                                              float partialTick, CallbackInfo ci) {
        guiGraphics.flush();
        GenesisOutlineRenderer.flushGuiPass();
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void genesis$flushRemainingGuiOutline(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick,
                                                  CallbackInfo ci) {
        guiGraphics.flush();
        GenesisOutlineRenderer.flushGuiPass();
    }
}
