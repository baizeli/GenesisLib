package miku.bai_ze_li.genesis;

import com.mojang.logging.LogUtils;
import miku.bai_ze_li.genesis.internal.GenesisBootstrap;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(GenesisLib.MODID)
public final class GenesisLib {
    public static final String MODID = "genesis_api";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GenesisLib() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        GenesisBootstrap.init();
    }
}
