package miku.bai_ze_li.genesis.api.render.tooltip;

import miku.bai_ze_li.genesis.api.annotation.GenesisTooltipParticles;

public final class GenesisTooltipParticlePresets {
    private GenesisTooltipParticlePresets() {
    }

    public static TooltipParticleSystem.ParticleConfig create(GenesisTooltipParticles.Preset preset) {
        return switch (preset) {
            case COLORFUL -> colorful();
            case PURPLE -> purpleStars();
            case GOLD -> goldLeaves();
        };
    }

    public static TooltipParticleSystem.ParticleConfig colorful() {
        return new TooltipParticleSystem.ParticleConfig()
                .setTextures(PTID.Star_0, PTID.Star_1, PTID.Star_2, PTID.Star_3, PTID.Star_4, PTID.Star_5, PTID.Star_6, PTID.Star_7, PTID.Star_8, PTID.Star_9)
                .setParticleCount(1, 3)
                .setMaxTotalParticles(400)
                .setSize(4.0F, 12.0F)
                .setRandomSize(false)
                .setLife(3.0F, 4.0F)
                .setSpeed(70.0F, 100.0F)
                .setColors(0xFFFFB800, 0xFFCD7231, 0xFFFFEB00, 0xFFE0AE2D)
                .setRainbowColors(true, 2.0F)
                .setColorTransitionSpeed(1.5F)
                .setGravity(false, 40.0F)
                .setWind(true, 0.0F, 110.0F)
                .setAirResistance(0.1F)
                .setBounciness(10.0F)
                .setRotation(true, 0.0F, 0.01F)
                .setInitialRotation(0.0F, 360.0F)
                .setMotionType(TooltipParticleSystem.MotionType.RAIN)
                .setMotionProperties(20.0F, 1.5F)
                .setRadius(200.0F)
                .setFadeIn(true, 0.05F)
                .setFadeOut(true, 0.25F)
                .setDepthLayers(true, 12, 0.08F)
                .setSizeCurve(TooltipParticleSystem.BezierCurveType.STAR_EXPAND)
                .setAlphaCurve(TooltipParticleSystem.BezierCurveType.NONE)
                .setSpeedCurve(TooltipParticleSystem.BezierCurveType.NONE)
                .setRotationCurve(TooltipParticleSystem.BezierCurveType.NONE);
    }

    public static TooltipParticleSystem.ParticleConfig purpleStars() {
        return stars(0xFFE0AAFF, 0xFFC77DFF, 0xFF9D4EDD, 0xFF5A189A);
    }

    public static TooltipParticleSystem.ParticleConfig goldLeaves() {
        return leaves(0xFFFFF3B0, 0xFFFFD166, 0xFFFFB703, 0xFFFB8500);
    }

    private static TooltipParticleSystem.ParticleConfig stars(int... colors) {
        return new TooltipParticleSystem.ParticleConfig()
                .setTextures(PTID.Star_0, PTID.Star_1, PTID.Star_2, PTID.Star_3, PTID.Star_4, PTID.Star_5, PTID.Star_6, PTID.Star_7, PTID.Star_8, PTID.Star_9)
                .setParticleCount(1, 3)
                .setMaxTotalParticles(360)
                .setSize(4.0F, 11.0F)
                .setRandomSize(false)
                .setLife(3.0F, 4.2F)
                .setSpeed(62.0F, 92.0F)
                .setColors(true, colors)
                .setColorTransitionSpeed(0.9F)
                .setGravity(false, 24.0F)
                .setWind(true, 0.0F, 85.0F)
                .setAirResistance(0.1F)
                .setRotation(true, 0.0F, 0.01F)
                .setInitialRotation(0.0F, 360.0F)
                .setMotionType(TooltipParticleSystem.MotionType.RAIN)
                .setMotionProperties(18.0F, 1.4F)
                .setRadius(180.0F)
                .setFadeIn(true, 0.06F)
                .setFadeOut(true, 0.3F)
                .setDepthLayers(true, 10, 0.07F)
                .setSizeCurve(TooltipParticleSystem.BezierCurveType.STAR_EXPAND)
                .setAlphaCurve(TooltipParticleSystem.BezierCurveType.NONE)
                .setSpeedCurve(TooltipParticleSystem.BezierCurveType.NONE)
                .setRotationCurve(TooltipParticleSystem.BezierCurveType.NONE);
    }

    private static TooltipParticleSystem.ParticleConfig leaves(int... colors) {
        return new TooltipParticleSystem.ParticleConfig()
                .setTextures(PTID.Leaf_0, PTID.Leaf_1, PTID.Leaf_2, PTID.Leaf_3)
                .setParticleCount(1, 4)
                .setMaxTotalParticles(280)
                .setSize(5.0F, 13.0F)
                .setRandomSize(true)
                .setLife(2.8F, 4.5F)
                .setSpeed(55.0F, 95.0F)
                .setColors(true, colors)
                .setColorTransitionSpeed(0.65F)
                .setGravity(false, 0.0F)
                .setWind(true, 48.0F, 4.0F)
                .setAirResistance(0.08F)
                .setRotation(true, -120.0F, 120.0F)
                .setInitialRotation(0.0F, 360.0F)
                .setMotionType(TooltipParticleSystem.MotionType.LEAF_WIND_RIGHT)
                .setMotionProperties(18.0F, 1.35F)
                .setFadeIn(true, 0.12F)
                .setFadeOut(true, 0.45F)
                .setDepthLayers(true, 8, 0.06F)
                .setSizeCurve(TooltipParticleSystem.BezierCurveType.BREATH)
                .setAlphaCurve(TooltipParticleSystem.BezierCurveType.NONE)
                .setSpeedCurve(TooltipParticleSystem.BezierCurveType.EASE_OUT)
                .setRotationCurve(TooltipParticleSystem.BezierCurveType.NONE);
    }
}
