package miku.bai_ze_li.genesis.render.api.shader;

import org.joml.Vector4f;

public record GenesisItemShaderEffect(int useType, float scale, Vector4f color) {
    public GenesisItemShaderEffect {
        color = new Vector4f(color);
    }

    public Vector4f copyColor() {
        return new Vector4f(color);
    }
}
