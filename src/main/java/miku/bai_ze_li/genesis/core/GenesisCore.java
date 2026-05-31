package miku.bai_ze_li.genesis.core;

import miku.bai_ze_li.genesis.GenesisLib;

public final class GenesisCore {
    private GenesisCore() {
    }

    public static void init() {
        GenesisLib.LOGGER.debug("Genesis Core initialized");
    }
}
