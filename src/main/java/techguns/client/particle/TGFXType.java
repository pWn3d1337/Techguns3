package techguns.client.particle;

import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

/** A named effect definition from the FX files: either one particle system or a list of them. */
public abstract class TGFXType {
    public String name;

    public abstract List<TGParticleSystem> createParticleSystems(ClientLevel level, double posX, double posY, double posZ,
                                                                 double motionX, double motionY, double motionZ);

    public abstract List<TGParticleSystem> createParticleSystemsOnEntity(Entity entity);

    public abstract List<TGParticleSystem> createParticleSystemsOnParticle(ClientLevel level, TGParticle particle);
}
