package techguns.entity.spawn;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Techguns 2 {@code TGDummySpawn}: a monster in the biome spawn lists that never enters the world. Natural spawning
 * places it like any monster (darkness, solid ground); {@link TGSpawnManager} replaces it with a Techguns NPC picked
 * by danger level.
 */
public class NpcSpawnMarker extends Monster {
    public NpcSpawnMarker(EntityType<? extends NpcSpawnMarker> type, Level level) {
        super(type, level);
    }
}
