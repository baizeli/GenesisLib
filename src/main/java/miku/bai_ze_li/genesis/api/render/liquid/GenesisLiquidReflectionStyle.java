package miku.bai_ze_li.genesis.api.render.liquid;

import org.joml.Vector4f;

public final class GenesisLiquidReflectionStyle {
    public static final GenesisLiquidReflectionStyle DEFAULT = builder().build();

    private final Vector4f tint;
    private final float reflectionStrength;
    private final float flowStrength;
    private final float flowSpeed;
    private final int scanRadius;
    private final int verticalRadius;

    private GenesisLiquidReflectionStyle(Builder builder) {
        this.tint = new Vector4f(builder.tint);
        this.reflectionStrength = builder.reflectionStrength;
        this.flowStrength = builder.flowStrength;
        this.flowSpeed = builder.flowSpeed;
        this.scanRadius = builder.scanRadius;
        this.verticalRadius = builder.verticalRadius;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Vector4f tint() {
        return new Vector4f(tint);
    }

    public float reflectionStrength() {
        return reflectionStrength;
    }

    public float flowStrength() {
        return flowStrength;
    }

    public float flowSpeed() {
        return flowSpeed;
    }

    public int scanRadius() {
        return scanRadius;
    }

    public int verticalRadius() {
        return verticalRadius;
    }

    public static final class Builder {
        private final Vector4f tint = new Vector4f(0.12F, 0.72F, 1.0F, 0.58F);
        private float reflectionStrength = 0.68F;
        private float flowStrength = 0.55F;
        private float flowSpeed = 1.0F;
        private int scanRadius = 22;
        private int verticalRadius = 8;

        private Builder() {
        }

        public Builder tint(float red, float green, float blue, float alpha) {
            this.tint.set(red, green, blue, alpha);
            return this;
        }

        public Builder reflectionStrength(float reflectionStrength) {
            this.reflectionStrength = reflectionStrength;
            return this;
        }

        public Builder flowStrength(float flowStrength) {
            this.flowStrength = flowStrength;
            return this;
        }

        public Builder flowSpeed(float flowSpeed) {
            this.flowSpeed = flowSpeed;
            return this;
        }

        public Builder scanRadius(int scanRadius) {
            this.scanRadius = Math.max(4, scanRadius);
            return this;
        }

        public Builder verticalRadius(int verticalRadius) {
            this.verticalRadius = Math.max(2, verticalRadius);
            return this;
        }

        public GenesisLiquidReflectionStyle build() {
            return new GenesisLiquidReflectionStyle(this);
        }
    }
}
