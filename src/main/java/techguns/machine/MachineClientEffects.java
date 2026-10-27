package techguns.machine;

import net.minecraft.world.level.Level;

/**
 * Client-side machine effects triggered from common machine code (Techguns 2 particle systems). The client entrypoint
 * installs the real implementation; on a dedicated server the no-op default stays.
 */
public interface MachineClientEffects {
    MachineClientEffects NONE = new MachineClientEffects() {
    };

    /** Spawns the Techguns particle system {@code name} at a position. */
    default void spawnFx(String name, Level level, double x, double y, double z) {
    }

    static MachineClientEffects get() {
        return Holder.instance;
    }

    static void install(MachineClientEffects effects) {
        Holder.instance = effects;
    }

    final class Holder {
        private static volatile MachineClientEffects instance = NONE;

        private Holder() {
        }
    }
}
