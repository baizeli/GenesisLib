package miku.bai_ze_li.genesis.core.mixin.minecraft.client.renderer;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor
    double getXd();

    @Accessor
    double getYd();

    @Accessor
    double getZd();

    @Accessor
    double getZ();

    @Accessor
    double getY();

    @Accessor
    double getX();

    @Accessor
    double getZo();

    @Accessor
    double getYo();

    @Accessor
    double getXo();
}
