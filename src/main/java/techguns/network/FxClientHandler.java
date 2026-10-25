package techguns.network;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import techguns.core.damage.DeathType;
import techguns.core.fx.EntityCondition;

/**
 * Client-side handling of the FX payloads. The client entrypoint installs the real implementation; on a
 * dedicated server the no-op default stays, so common code never references client classes.
 */
public interface FxClientHandler {
    FxClientHandler NONE = new FxClientHandler() {
    };

    /** Spawns the particle system {@code name} at a position. */
    default void spawnFx(Level level, String name, double x, double y, double z, double motionX, double motionY, double motionZ,
                         float pitch, float yaw, float scale) {
    }

    /** Spawns the particle system {@code name} attached to an entity. */
    default void spawnFxOnEntity(Level level, String name, Entity entity, float offsetX, float offsetY, float offsetZ,
                                 boolean attachToHead, EntityCondition condition) {
    }

    /** Bullet impact on a block: sound and particles depending on the block's sound type. */
    default void gunImpact(Level level, short soundType, double x, double y, double z, float pitch, float yaw, boolean incendiary) {
    }

    /** A living entity died with a special death effect (gore, bio, laser). */
    default void entityDeath(LivingEntity entity, DeathType deathType, float motionX, float motionY, float motionZ) {
    }

    static FxClientHandler get() {
        return Holder.instance;
    }

    static void install(FxClientHandler handler) {
        Holder.instance = handler;
    }

    final class Holder {
        private static volatile FxClientHandler instance = NONE;

        private Holder() {
        }
    }
}
