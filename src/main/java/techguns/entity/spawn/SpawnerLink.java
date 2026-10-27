package techguns.entity.spawn;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import techguns.registry.TGAttachments;

/**
 * Techguns 2 {@code TGSpawnerNPCData}: the TG spawner an NPC came from, saved with the NPC so it can report its death
 * or despawn and re-register after a reload.
 */
public final class SpawnerLink {
    private SpawnerLink() {
    }

    public static Optional<BlockPos> get(Entity entity) {
        return entity.hasData(TGAttachments.SPAWNER_POS) ? Optional.of(entity.getData(TGAttachments.SPAWNER_POS)) : Optional.empty();
    }

    public static void set(Entity entity, BlockPos spawner) {
        entity.setData(TGAttachments.SPAWNER_POS, spawner.immutable());
    }

    /** The NPC's spawner block entity, if it still exists and its chunk is loaded. */
    public static Optional<TGSpawnerBlockEntity> spawner(Entity entity) {
        return get(entity).filter(pos -> entity.level().isLoaded(pos))
                .map(pos -> entity.level().getBlockEntity(pos) instanceof TGSpawnerBlockEntity spawner ? spawner : null);
    }
}
