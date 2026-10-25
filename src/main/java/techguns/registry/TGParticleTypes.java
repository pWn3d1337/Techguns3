package techguns.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;

/**
 * Vanilla particle types. Techguns' own effects use the FX system ({@code client/particle}), not vanilla particles.
 */
public final class TGParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Techguns.MODID);

    /**
     * Spawns nothing on the client. Passed to {@code Level.explode} so vanilla explosion particles don't
     * overlap the Techguns explosion FX (Fabric's {@code TGExplosion} spawned no vanilla particles).
     */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NONE =
            PARTICLE_TYPES.register("none", () -> new SimpleParticleType(true));

    private TGParticleTypes() {
    }
}
