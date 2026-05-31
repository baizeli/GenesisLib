package miku.bai_ze_li.genesis.core.mixin.minecraftforge.event.entity.living;

import miku.bai_ze_li.genesis.core.api.mixin.LivingEventEC;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingHurtEvent.class, remap = false)
public abstract class LivingHurtEventMixin implements LivingEventEC {
    @Shadow
    private float amount;
    @Unique
    private boolean genesisLib$onlyAmountUp;

    @Override
    public boolean ironSpellGenesis$isHackedOnlyAmountUp() {
        return genesisLib$onlyAmountUp;
    }

    @Override
    public void ironSpellGenesis$hackedOnlyAmountUp(boolean target) {
        genesisLib$onlyAmountUp = target;
    }

    @Inject(method = "setAmount", at = @At("HEAD"), cancellable = true)
    private void setAmount(float amount, CallbackInfo ci) {
        if (genesisLib$onlyAmountUp) {
            ci.cancel();
            this.amount = Math.max(this.amount, amount);
        }
    }
}
