package miku.bai_ze_li.genesis.core.mixin.minecraft.world.damagesource;

import miku.bai_ze_li.genesis.core.api.damage.GenesisDamageTypeTags;
import miku.bai_ze_li.genesis.core.api.mixin.DamageSourceInterface;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceInterface {
    @Shadow
    public Holder<DamageType> type;

    @Unique
    private boolean ironSpellGenesis$bypassArmor = false;
    @Unique
    private boolean ironSpellGenesis$bypassAll = false;

    @Override
    public void ironSpellGenesis$setBypassArmor(boolean z) {
        this.ironSpellGenesis$bypassArmor = z;
    }

    @Override
    public boolean ironSpellGenesis$bypassArmor() {
        return this.ironSpellGenesis$bypassArmor;
    }

    @Override
    public void ironSpellGenesis$setBypassAll(boolean z) {
        ironSpellGenesis$bypassAll = z;
    }

    @Override
    public boolean ironSpellGenesis$isBypassAll() {
        return ironSpellGenesis$bypassAll;
    }

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void is(TagKey<DamageType> tagKey, CallbackInfoReturnable<Boolean> cir) {
        if (this.type.is(GenesisDamageTypeTags.BYPASSES_ALL) && ironSpellGenesis$isBypassAllTag(tagKey)) {
            cir.setReturnValue(true);
            return;
        }
        if (this.type.is(GenesisDamageTypeTags.BYPASSES_ARMOR) && tagKey == DamageTypeTags.BYPASSES_ARMOR) {
            cir.setReturnValue(true);
            return;
        }
        if (tagKey == DamageTypeTags.BYPASSES_ARMOR && this.ironSpellGenesis$bypassArmor()) {
            cir.setReturnValue(true);
            return;
        }
        if (ironSpellGenesis$isBypassAll() && ironSpellGenesis$isBypassAllTag(tagKey)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean ironSpellGenesis$isBypassAllTag(TagKey<DamageType> tagKey) {
        return tagKey == DamageTypeTags.BYPASSES_ARMOR
                || tagKey == DamageTypeTags.BYPASSES_SHIELD
                || tagKey == DamageTypeTags.BYPASSES_INVULNERABILITY
                || tagKey == DamageTypeTags.BYPASSES_COOLDOWN
                || tagKey == DamageTypeTags.BYPASSES_RESISTANCE
                || tagKey == DamageTypeTags.BYPASSES_EFFECTS;
    }
}
