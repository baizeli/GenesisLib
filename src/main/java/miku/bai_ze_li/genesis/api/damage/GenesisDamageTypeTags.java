package miku.bai_ze_li.genesis.api.damage;

import miku.bai_ze_li.genesis.GenesisLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class GenesisDamageTypeTags {
    public static final TagKey<DamageType> BYPASSES_ARMOR = create("bypasses_armor");
    public static final TagKey<DamageType> BYPASSES_ALL = create("bypasses_all");

    private GenesisDamageTypeTags() {
    }

    private static TagKey<DamageType> create(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(GenesisLib.MODID, name));
    }
}
