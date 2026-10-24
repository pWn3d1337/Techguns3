package techguns.client.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

/** An FX that consists of several other FX (the {@code FXList} blocks). */
public class TGParticleListType extends TGFXType {
    private final List<String> particleSystems = new ArrayList<>();

    public void addParticleSystem(String particleSystem) {
        particleSystems.add(particleSystem);
    }

    private List<TGParticleSystem> collect(Function<TGFXType, List<TGParticleSystem>> factory) {
        List<TGParticleSystem> list = new ArrayList<>();
        for (String system : particleSystems) {
            TGFXType type = TGFX.FX_LIST.get(system);
            if (type != null) {
                List<TGParticleSystem> created = factory.apply(type);
                if (created != null) {
                    list.addAll(created);
                }
            }
        }
        return list;
    }

    @Override
    public List<TGParticleSystem> createParticleSystems(ClientLevel level, double posX, double posY, double posZ,
                                                        double motionX, double motionY, double motionZ) {
        return collect(type -> type.createParticleSystems(level, posX, posY, posZ, motionX, motionY, motionZ));
    }

    @Override
    public List<TGParticleSystem> createParticleSystemsOnEntity(Entity entity) {
        return collect(type -> type.createParticleSystemsOnEntity(entity));
    }

    @Override
    public List<TGParticleSystem> createParticleSystemsOnParticle(ClientLevel level, TGParticle particle) {
        return collect(type -> type.createParticleSystemsOnParticle(level, particle));
    }
}
