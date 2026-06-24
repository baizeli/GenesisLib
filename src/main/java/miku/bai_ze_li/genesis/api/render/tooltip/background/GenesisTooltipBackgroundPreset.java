package miku.bai_ze_li.genesis.api.render.tooltip.background;

public enum GenesisTooltipBackgroundPreset {
    BLUE_GLASS(new GenesisTooltipBackgroundStyle(
            5,
            0x349FC7FF,
            0x881B2A3A,
            0xD8E9F7FF,
            0x706EA8FF,
            8.0F,
            3,
            3
    )),
    PURPLE_GLASS(new GenesisTooltipBackgroundStyle(
            5,
            0x36AE7DFF,
            0x88221832,
            0xE0D8B5FF,
            0x78735CFF,
            8.0F,
            3,
            3
    )),
    GOLD_GLASS(new GenesisTooltipBackgroundStyle(
            5,
            0x34FFD884,
            0x882F2412,
            0xE0FFF1B8,
            0x78FFB64D,
            8.0F,
            3,
            3
    )),
    DARK_GLASS(new GenesisTooltipBackgroundStyle(
            5,
            0x2C6D7F9A,
            0x9018202D,
            0xB8C9D8EA,
            0x68506A8B,
            7.0F,
            3,
            3
    ));

    private final GenesisTooltipBackgroundStyle style;

    GenesisTooltipBackgroundPreset(GenesisTooltipBackgroundStyle style) {
        this.style = style;
    }

    public GenesisTooltipBackgroundStyle style() {
        return style;
    }
}
