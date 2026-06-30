package miku.bai_ze_li.genesis.mixin.client;

import com.google.common.collect.ImmutableList;
import miku.bai_ze_li.genesis.api.render.shader.GenesisRenderType;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RenderType.class)
public abstract class RenderTypeMixin {
    @Inject(method = "chunkBufferLayers", at = @At("RETURN"), cancellable = true)
    private static void genesis$addSourceWaterChunkLayer(CallbackInfoReturnable<List<RenderType>> cir) {
        List<RenderType> original = cir.getReturnValue();
        if (original.contains(GenesisRenderType.sourceWater)) {
            return;
        }
        cir.setReturnValue(ImmutableList.<RenderType>builder()
                .addAll(original)
                .add(GenesisRenderType.sourceWater)
                .build());
    }
}
