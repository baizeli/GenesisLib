package miku.bai_ze_li.genesis.api.render.tooltip.background;

public record GenesisTooltipBackgroundStyle(
        int padding,
        int overlayColor,
        int fallbackColor,
        int borderStartColor,
        int borderEndColor,
        float blurRadius,
        int blurPasses,
        int downscale
) {
    public static final GenesisTooltipBackgroundStyle DEFAULT_GLASS = new GenesisTooltipBackgroundStyle(
            5,
            0x349FC7FF,
            0x881B2A3A,
            0xD8E9F7FF,
            0x706EA8FF,
            8.0F,
            3,
            3
    );

    public GenesisTooltipBackgroundStyle {
        padding = Math.max(0, padding);
        blurRadius = Math.max(0.0F, blurRadius);
        blurPasses = Math.max(1, blurPasses);
        downscale = Math.max(1, downscale);
    }
}
