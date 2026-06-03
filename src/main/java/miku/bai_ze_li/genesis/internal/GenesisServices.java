package miku.bai_ze_li.genesis.internal;

import miku.bai_ze_li.genesis.internal.damage.DefaultGenesisDamageService;
import miku.bai_ze_li.genesis.internal.damage.GenesisDamageService;

public final class GenesisServices {
    private static GenesisDamageService damage = new DefaultGenesisDamageService();

    private GenesisServices() {
    }

    public static GenesisDamageService damage() {
        return damage;
    }

    public static void setDamageService(GenesisDamageService service) {
        damage = service == null ? new DefaultGenesisDamageService() : service;
    }
}
