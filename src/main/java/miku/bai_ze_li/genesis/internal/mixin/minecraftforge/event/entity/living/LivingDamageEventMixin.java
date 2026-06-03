package miku.bai_ze_li.genesis.internal.mixin.minecraftforge.event.entity.living;

import miku.bai_ze_li.genesis.internal.access.GenesisLivingEventAccess;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingDamageEvent.class, remap = false)
public abstract class LivingDamageEventMixin implements GenesisLivingEventAccess {
    @Shadow
    private float amount;
    @Unique
    private boolean genesis$minimumDamageLocked;

    @Override
    public boolean genesis$isMinimumDamageLocked() {
        return genesis$minimumDamageLocked;
    }

    @Override
    public void genesis$setMinimumDamageLocked(boolean value) {
        genesis$minimumDamageLocked = value;
    }

    @Inject(method = "setAmount", at = @At("HEAD"), cancellable = true)
    private void setAmount(float amount, CallbackInfo ci) {
        if (genesis$minimumDamageLocked) {
            ci.cancel();
            this.amount = Math.max(this.amount, amount);
        }
    }
}
