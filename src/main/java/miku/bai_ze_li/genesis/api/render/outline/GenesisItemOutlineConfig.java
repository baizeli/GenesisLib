package miku.bai_ze_li.genesis.api.render.outline;

import dev.xkmc.l2library.serial.config.BaseConfig;
import dev.xkmc.l2serial.serialization.SerialClass;

@SerialClass
public class GenesisItemOutlineConfig extends BaseConfig {
    @SerialClass.SerialField
    public String effect = GenesisEffect.BLUE_WHITE.getId();

    public GenesisEffect toEffect() {
        return GenesisEffect.byId(effect);
    }
}
