package miku.bai_ze_li.genesis.internal.damage;

import miku.bai_ze_li.genesis.api.damage.GenesisDamagePolicy;
import miku.bai_ze_li.genesis.api.damage.GenesisDamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

public final class DamagePolicyResolver {
    private DamagePolicyResolver() {
    }

    public static GenesisDamagePolicy fromSource(DamageSource source) {
        if (GenesisDamageKernel.hasBypassAll(source) || source.is(GenesisDamageTypeTags.BYPASSES_ALL)) {
            return GenesisDamagePolicy.TRUE_DAMAGE;
        }
        if (GenesisDamageKernel.hasBypassArmor(source) || source.is(GenesisDamageTypeTags.BYPASSES_ARMOR)) {
            return GenesisDamagePolicy.builder().bypassArmor().build();
        }
        return GenesisDamagePolicy.NONE;
    }

    public static boolean isBypassesAll(DamageSource source) {
        return GenesisDamageKernel.hasBypassAll(source) || source.is(GenesisDamageTypeTags.BYPASSES_ALL);
    }
}
