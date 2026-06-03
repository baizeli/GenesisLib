package miku.bai_ze_li.genesis.internal.mixin.minecraftforge.event.entity.living;

import miku.bai_ze_li.genesis.internal.access.GenesisLivingEventAccess;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEvent.class)
public abstract class LivingEventMixin extends EntityEvent implements GenesisLivingEventAccess {
    @Unique
    private boolean genesis$uncancelable;

    public LivingEventMixin(Entity entity) {
        super(entity);
    }

    @Override
    public boolean genesis$isUncancelable() {
        return genesis$uncancelable;
    }

    @Override
    public void genesis$setUncancelable(boolean value) {
        genesis$uncancelable = value;
    }

    @Override
    public boolean genesis$isMinimumDamageLocked() {
        return false;
    }

    @Override
    public void genesis$setMinimumDamageLocked(boolean value) {
    }

    @Override
    public void setCanceled(boolean cancel) {
        if (this.genesis$uncancelable) {
            return;
        }
        super.setCanceled(cancel);
    }
}
