package miku.bai_ze_li.genesis.internal.access;

public interface GenesisLivingEventAccess {
    boolean genesis$isUncancelable();

    void genesis$setUncancelable(boolean value);

    boolean genesis$isMinimumDamageLocked();

    void genesis$setMinimumDamageLocked(boolean value);
}
