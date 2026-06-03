package miku.bai_ze_li.genesis.api.damage;

import java.util.EnumSet;
import java.util.Set;

public final class GenesisDamagePolicy {
    public static final GenesisDamagePolicy NONE = new GenesisDamagePolicy(EnumSet.noneOf(GenesisDamageFlag.class));
    public static final GenesisDamagePolicy TRUE_DAMAGE = builder()
            .bypassAll()
            .uncancelable()
            .minimumDamageLocked()
            .build();

    private final EnumSet<GenesisDamageFlag> flags;

    private GenesisDamagePolicy(EnumSet<GenesisDamageFlag> flags) {
        this.flags = flags.isEmpty() ? EnumSet.noneOf(GenesisDamageFlag.class) : EnumSet.copyOf(flags);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean has(GenesisDamageFlag flag) {
        return flags.contains(flag);
    }

    public Set<GenesisDamageFlag> flags() {
        return EnumSet.copyOf(flags);
    }

    public static final class Builder {
        private final EnumSet<GenesisDamageFlag> flags = EnumSet.noneOf(GenesisDamageFlag.class);

        public Builder bypassArmor() {
            flags.add(GenesisDamageFlag.BYPASS_ARMOR);
            return this;
        }

        public Builder bypassAll() {
            flags.add(GenesisDamageFlag.BYPASS_ALL);
            return this;
        }

        public Builder uncancelable() {
            flags.add(GenesisDamageFlag.UNCANCELABLE);
            return this;
        }

        public Builder minimumDamageLocked() {
            flags.add(GenesisDamageFlag.MINIMUM_DAMAGE_LOCKED);
            return this;
        }

        public Builder forceDeath() {
            flags.add(GenesisDamageFlag.FORCE_DEATH);
            return this;
        }

        public GenesisDamagePolicy build() {
            return new GenesisDamagePolicy(flags);
        }
    }
}
