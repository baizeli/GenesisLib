package miku.bai_ze_li.genesis.api.annotation;

import miku.bai_ze_li.genesis.api.render.shader.GenesisItemShaderEffect;
import miku.bai_ze_li.genesis.api.render.shader.GenesisItemShaderRegistry;
import miku.bai_ze_li.genesis.api.render.text.GenesisTextEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.ModList;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class GenesisAnnotations {
    private GenesisAnnotations() {
    }

    public static Optional<GenesisCosmicItemEffect> cosmicItemEffect(Class<?> type) {
        return Optional.ofNullable(type.getAnnotation(GenesisCosmicItemEffect.class));
    }

    public static Optional<GenesisItemShader> itemShader(Class<?> type) {
        return Optional.ofNullable(type.getAnnotation(GenesisItemShader.class));
    }

    public static boolean registerCosmicItemEffect(ItemLike item) {
        return registerCosmicItemEffect(item, item.asItem().getClass());
    }

    public static boolean registerCosmicItemEffect(ItemLike item, Class<?> annotatedType) {
        GenesisItemShader itemShader = annotatedType.getAnnotation(GenesisItemShader.class);
        if (itemShader != null) {
            GenesisItemShaderRegistry.register(item, createShaderEffect(
                    itemShader.useType(),
                    itemShader.scale(),
                    itemShader.red(),
                    itemShader.green(),
                    itemShader.blue(),
                    itemShader.alpha()
            ));
            return true;
        }

        GenesisCosmicItemEffect cosmicItemEffect = annotatedType.getAnnotation(GenesisCosmicItemEffect.class);
        if (cosmicItemEffect != null) {
            GenesisItemShaderRegistry.register(item, createShaderEffect(
                    cosmicItemEffect.useType(),
                    cosmicItemEffect.scale(),
                    cosmicItemEffect.red(),
                    cosmicItemEffect.green(),
                    cosmicItemEffect.blue(),
                    cosmicItemEffect.alpha()
            ));
            return true;
        }

        return false;
    }

    public static List<GenesisTextEffect> textEffects(Class<?> type) {
        return Arrays.asList(type.getAnnotationsByType(GenesisTextEffect.class));
    }

    public static Component applyItemNameEffect(Class<?> type, Component originalName) {
        GenesisItemNameEffect itemNameEffect = type.getAnnotation(GenesisItemNameEffect.class);
        if (itemNameEffect != null) {
            return applyTextPreset(itemNameEffect.preset(), originalName);
        }

        for (GenesisTextEffect effect : type.getAnnotationsByType(GenesisTextEffect.class)) {
            if (effect.target() == GenesisTextEffect.TextTarget.ITEM_NAME) {
                return applyTextPreset(effect.preset(), originalName);
            }
        }
        return originalName;
    }

    public static void addTooltipEffects(Class<?> type, List<Component> tooltip) {
        for (GenesisTooltipTextEffect effect : type.getAnnotationsByType(GenesisTooltipTextEffect.class)) {
            if (!effect.translation().isEmpty()) {
                tooltip.add(applyTextPreset(effect.preset(), Component.translatable(effect.translation())));
            }
        }

        for (GenesisTextEffect effect : type.getAnnotationsByType(GenesisTextEffect.class)) {
            if (effect.target() == GenesisTextEffect.TextTarget.TOOLTIP_LINE && !effect.translation().isEmpty()) {
                tooltip.add(applyTextPreset(effect.preset(), Component.translatable(effect.translation())));
            }
        }
    }

    public static boolean isExperimental(Class<?> type) {
        return type.isAnnotationPresent(GenesisExperimental.class);
    }

    public static Optional<GenesisExperimental> experimental(Class<?> type) {
        return Optional.ofNullable(type.getAnnotation(GenesisExperimental.class));
    }

    public static boolean isCompatAvailable(Class<?> type) {
        GenesisCompat compat = type.getAnnotation(GenesisCompat.class);
        return compat == null || ModList.get().isLoaded(compat.value());
    }

    public static Optional<GenesisCompat> compat(Class<?> type) {
        return Optional.ofNullable(type.getAnnotation(GenesisCompat.class));
    }

    private static Component applyTextPreset(GenesisTextEffect.Preset preset, Component component) {
        return switch (preset) {
            case BLUE_GRADIENT -> GenesisTextEffects.blueGradient(component);
            case RAINBOW_GRADIENT -> GenesisTextEffects.rainbowGradient(component);
        };
    }

    private static GenesisItemShaderEffect createShaderEffect(int useType, float scale, float red, float green, float blue, float alpha) {
        return new GenesisItemShaderEffect(useType, scale, new Vector4f(red, green, blue, alpha));
    }
}
