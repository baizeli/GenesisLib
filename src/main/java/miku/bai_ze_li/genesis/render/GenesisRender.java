package miku.bai_ze_li.genesis.render;

import miku.bai_ze_li.genesis.GenesisLib;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = GenesisLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GenesisRender {
    private GenesisRender() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        GenesisLib.LOGGER.debug("Genesis Render initialized");
    }
}
