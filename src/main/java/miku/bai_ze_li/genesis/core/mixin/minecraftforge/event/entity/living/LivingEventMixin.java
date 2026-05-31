package miku.bai_ze_li.genesis.core.mixin.minecraftforge.event.entity.living;

import miku.bai_ze_li.genesis.core.api.mixin.LivingEventEC;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEvent.class)
public abstract class LivingEventMixin extends EntityEvent implements LivingEventEC {
    @Unique
    private boolean genesisLib$unCancelable;

    public LivingEventMixin(Entity entity) {
        super(entity);
    }

    @Override
    public boolean ironSpellGenesis$isHackedUnCancelable() {
        return genesisLib$unCancelable;
    }

    @Override
    public void ironSpellGenesis$hackedUnCancelable(boolean target) {
        genesisLib$unCancelable = target;
    }

    @Override
    public boolean ironSpellGenesis$isHackedOnlyAmountUp() {
        return false;
    }

    @Override
    public void ironSpellGenesis$hackedOnlyAmountUp(boolean target) {
    }

    @Override
    public void setCanceled(boolean cancel) {
        if (this.genesisLib$unCancelable) {
            return;
        }
        super.setCanceled(cancel);
    }
}
