package miku.bai_ze_li.genesis.internal;

import miku.bai_ze_li.genesis.GenesisLib;

public final class GenesisBootstrap {
    private GenesisBootstrap() {
    }

    public static void init() {
        GenesisLib.LOGGER.debug("GenesisLib initialized");
    }
}
