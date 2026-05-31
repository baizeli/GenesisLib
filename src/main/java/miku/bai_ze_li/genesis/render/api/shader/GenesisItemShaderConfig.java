package miku.bai_ze_li.genesis.render.api.shader;

import dev.xkmc.l2library.serial.config.BaseConfig;
import dev.xkmc.l2serial.serialization.SerialClass;
import org.joml.Vector4f;

@SerialClass
public class GenesisItemShaderConfig extends BaseConfig {
    @SerialClass.SerialField
    public int use_type;

    @SerialClass.SerialField
    public float scale = 0.6F;

    @SerialClass.SerialField
    public float red = 0.0F;

    @SerialClass.SerialField
    public float green = 0.02F;

    @SerialClass.SerialField
    public float blue = 0.03F;

    @SerialClass.SerialField
    public float alpha = 1.0F;

    public GenesisItemShaderEffect toEffect() {
        return new GenesisItemShaderEffect(use_type, scale, new Vector4f(red, green, blue, alpha));
    }
}
