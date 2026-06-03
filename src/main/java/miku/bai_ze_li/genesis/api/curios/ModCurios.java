package miku.bai_ze_li.genesis.api.curios;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Arrays;
import java.util.function.Predicate;

public enum ModCurios {
    RING("ring", 4, null, null, null),
    HEAD("head", 1, null, null, null),
    HANDS("hands", 2, null, null, null),
    CROWN("crown", 1, false, 41,
            ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "slot/crown_slot")
    ),
    CHARM("charm", 2, null, null, null),
    BRACELET("bracelet", 1, null, null, null),
    BODY("body", 1, null, null, null),
    BELT("belt", null, null, null, null),
    BACK("back", 1, null, null, null),
    ;


    public final String name;
    @Nullable
    public final Integer size;
    @Nullable
    public final ResourceLocation icon;
    @Nullable
    public final Boolean replace;
    @Nullable
    public final Integer order;

    ModCurios(
            String name,
            @Nullable Integer size,
            @Nullable Boolean replace,
            @Nullable Integer order,
            @Nullable ResourceLocation icon
    ) {
        this.name = name;
        this.size = size;
        this.replace = replace;
        this.order = order;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public static String[] getAll(){
        return Arrays.stream(ModCurios.values()).map(ModCurios::getName).toArray(String[]::new);
    }

    public static boolean hasCurios(@Nullable LivingEntity entity, Predicate<ItemStack> predicate) {
        if(entity == null) return false;
        ICuriosItemHandler itemHandler = CuriosApi.getCuriosInventory(entity).resolve().orElse(null);
        if(itemHandler == null) return false;
        return itemHandler.findFirstCurio(predicate).isPresent();
    }
}
