package miku.bai_ze_li.genesis.core.api.mixin;

public interface LivingEventEC {
    //婧愪唬鐮佹潵鑷猺evelationfix锛屽師浣滆€卪ega32k
    boolean ironSpellGenesis$isHackedUnCancelable();

    void ironSpellGenesis$hackedUnCancelable(boolean target);

    //浠呴€傞厤閮ㄥ垎浜嬩欢
    boolean ironSpellGenesis$isHackedOnlyAmountUp();

    void ironSpellGenesis$hackedOnlyAmountUp(boolean target);
}
