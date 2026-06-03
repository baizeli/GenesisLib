package miku.bai_ze_li.genesis.api.render;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

public class TrailRenderStyle {
    private final ResourceLocation texture;
    private final ResourceLocation overlayTexture;
    private final ResourceLocation coneTexture;
    private final float width;
    private final float alphaMultiplier;
    private final boolean emissive;
    private final boolean headless;
    private final ColorProvider colorProvider;
    private final RenderTypeProvider renderTypeProvider;

    private TrailRenderStyle(Builder builder) {
        this.texture = builder.texture;
        this.overlayTexture = builder.overlayTexture;
        this.coneTexture = builder.coneTexture;
        this.width = builder.width;
        this.alphaMultiplier = builder.alphaMultiplier;
        this.emissive = builder.emissive;
        this.headless = builder.headless;
        this.colorProvider = builder.colorProvider;
        this.renderTypeProvider = builder.renderTypeProvider;
    }

    public static Builder builder(ResourceLocation texture, ColorProvider colorProvider) {
        return new Builder(texture, colorProvider);
    }

    public ResourceLocation texture() {
        return texture;
    }

    @Nullable
    public ResourceLocation overlayTexture() {
        return overlayTexture;
    }

    @Nullable
    public ResourceLocation coneTexture() {
        return coneTexture;
    }

    public float width() {
        return width;
    }

    public float alphaMultiplier() {
        return alphaMultiplier;
    }

    public boolean emissive() {
        return emissive;
    }

    public boolean headless() {
        return headless;
    }

    public float[] color(float progress, float time, int entityId) {
        return colorProvider.color(progress, time, entityId);
    }

    @Nullable
    public RenderTypeProvider renderTypeProvider() {
        return renderTypeProvider;
    }

    @FunctionalInterface
    public interface ColorProvider {
        float[] color(float progress, float time, int entityId);
    }

    @FunctionalInterface
    public interface RenderTypeProvider {
        RenderType renderType(TrailRenderStyle style, ResourceLocation texture);
    }

    public static class Builder {
        private final ResourceLocation texture;
        private final ColorProvider colorProvider;
        private ResourceLocation overlayTexture;
        private ResourceLocation coneTexture;
        private float width = 1.0F;
        private float alphaMultiplier = 1.0F;
        private boolean emissive = true;
        private boolean headless;
        private RenderTypeProvider renderTypeProvider;

        private Builder(ResourceLocation texture, ColorProvider colorProvider) {
            this.texture = texture;
            this.colorProvider = colorProvider;
        }

        public Builder overlayTexture(ResourceLocation overlayTexture) {
            this.overlayTexture = overlayTexture;
            return this;
        }

        public Builder coneTexture(ResourceLocation coneTexture) {
            this.coneTexture = coneTexture;
            return this;
        }

        public Builder width(float width) {
            this.width = width;
            return this;
        }

        public Builder alphaMultiplier(float alphaMultiplier) {
            this.alphaMultiplier = alphaMultiplier;
            return this;
        }

        public Builder emissive(boolean emissive) {
            this.emissive = emissive;
            return this;
        }

        public Builder headless(boolean headless) {
            this.headless = headless;
            return this;
        }

        public Builder renderTypeProvider(RenderTypeProvider renderTypeProvider) {
            this.renderTypeProvider = renderTypeProvider;
            return this;
        }

        public TrailRenderStyle build() {
            return new TrailRenderStyle(this);
        }
    }
}