package miku.bai_ze_li.genesis.internal.mixin.minecraft.world.damagesource;

import miku.bai_ze_li.genesis.api.damage.GenesisDamageTypeTags;
import miku.bai_ze_li.genesis.internal.access.GenesisDamageSourceAccess;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements GenesisDamageSourceAccess {
    @Unique
    private boolean genesis$bypassArmor = false;
    @Unique
    private boolean genesis$bypassAll = false;

    @Override
    public void genesis$setBypassArmor(boolean value) {
        this.genesis$bypassArmor = value;
    }

    @Override
    public boolean genesis$hasBypassArmor() {
        return this.genesis$bypassArmor;
    }

    @Override
    public void genesis$setBypassAll(boolean value) {
        genesis$bypassAll = value;
    }

    @Override
    public boolean genesis$hasBypassAll() {
        return genesis$bypassAll;
    }

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void is(TagKey<DamageType> tagKey, CallbackInfoReturnable<Boolean> cir) {
        Holder<DamageType> typeHolder = ((DamageSource) (Object) this).typeHolder();
        if (typeHolder.is(GenesisDamageTypeTags.BYPASSES_ALL) && genesis$isBypassAllTag(tagKey)) {
            cir.setReturnValue(true);
            return;
        }
        if (typeHolder.is(GenesisDamageTypeTags.BYPASSES_ARMOR) && tagKey == DamageTypeTags.BYPASSES_ARMOR) {
            cir.setReturnValue(true);
            return;
        }
        if (tagKey == GenesisDamageTypeTags.BYPASSES_ARMOR && this.genesis$hasBypassArmor()) {
            cir.setReturnValue(true);
            return;
        }
        if (tagKey == GenesisDamageTypeTags.BYPASSES_ALL && this.genesis$hasBypassAll()) {
            cir.setReturnValue(true);
            return;
        }
        if (tagKey == DamageTypeTags.BYPASSES_ARMOR && this.genesis$hasBypassArmor()) {
            cir.setReturnValue(true);
            return;
        }
        if (genesis$hasBypassAll() && genesis$isBypassAllTag(tagKey)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean genesis$isBypassAllTag(TagKey<DamageType> tagKey) {
        return tagKey == DamageTypeTags.BYPASSES_ARMOR
                || tagKey == DamageTypeTags.BYPASSES_SHIELD
                || tagKey == DamageTypeTags.BYPASSES_INVULNERABILITY
                || tagKey == DamageTypeTags.BYPASSES_COOLDOWN
                || tagKey == DamageTypeTags.BYPASSES_RESISTANCE
                || tagKey == DamageTypeTags.BYPASSES_EFFECTS;
    }
}
