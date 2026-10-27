package techguns.world.structure.dungeon;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import techguns.TGIds;
import techguns.registry.TGNpcs;

/**
 * Techguns 2 maze dungeon presets ({@code IDungeonPreset}): the converted templates per level, the path settings and
 * the spawners. Templates are {@code techguns:dungeon/<template>/<segment>} (converted from the Techguns 2 {@code .ser}
 * dungeon templates). Techguns 2 also had a Nether dungeon preset, but never spawned it (its structure was commented
 * out); it is not ported.
 */
public enum DungeonPreset implements StringRepresentable {
    /**
     * Techguns 2 {@code PresetCastle}: dungeon level at the bottom, then lower, mid, upper and top floors, a roof; 2
     * zombie or skeleton soldiers per spawner.
     */
    CASTLE(5, 5, path -> {
        path.startHeightLevel = 1;
        path.chanceStraight = 0.8f;
        path.chanceRamp = 0.5f;
        path.chanceRoom = 0.25f;
        path.chanceFork = 0.4f;
        path.chanceUp = 0.65f;
        path.useFoundations = true;
        path.usePillars = true;
        path.useRoof = true;
    }, List.of(TGNpcs.ZOMBIE_SOLDIER, TGNpcs.SKELETON_SOLDIER));

    public static final StringRepresentable.EnumCodec<DungeonPreset> CODEC = StringRepresentable.fromEnum(DungeonPreset::values);
    /** Techguns 2 castle templates; the layers the castle preset picks from. */
    public static final List<String> CASTLE_TEMPLATES = List.of("ncdung1", "nclower1", "ncmid1", "ncupper1", "nctop1", "ncroof1");

    /** Blocks per cell side and per level. */
    public final int sizeXZ;
    public final int sizeY;
    private final Consumer<MazeDungeonPath> pathSettings;
    /** Mob types of the spawners, weight 1 each. */
    public final List<TGNpcs.Npc> spawnerMobs;
    /** Techguns 2 {@code initSpawner}: {@code setParams(2, 2, 200, 2)}. */
    public static final int SPAWNER_MOBS = 2;
    public static final int SPAWNER_MAX_ACTIVE = 2;
    public static final int SPAWNER_DELAY = 200;
    public static final int SPAWNER_RANGE = 2;
    /** Techguns 2 {@code getSpawnDensity}: spawners per cell. */
    public static final float SPAWN_DENSITY = 0.1f;
    /** Techguns 2 {@code getSpawnYOffset}: the spawner stands on the floor, 1 above the cell's base. */
    public static final int SPAWN_Y_OFFSET = 1;

    DungeonPreset(int sizeXZ, int sizeY, Consumer<MazeDungeonPath> pathSettings, List<TGNpcs.Npc> spawnerMobs) {
        this.sizeXZ = sizeXZ;
        this.sizeY = sizeY;
        this.pathSettings = pathSettings;
        this.spawnerMobs = spawnerMobs;
    }

    public void init(MazeDungeonPath path) {
        pathSettings.accept(path);
    }

    /**
     * Techguns 2 {@code PresetCastle.getSegment}: the template of a segment on a level (-1: roof) of a dungeon
     * {@code levels} high.
     */
    public ResourceLocation template(SegmentType type, int level, int levels) {
        String template;
        if (level == -1) {
            template = "ncroof1";
        } else if (level == 0) {
            template = "ncdung1";
        } else if (level <= 1) {
            template = "nclower1";
        } else if (level < levels - 2) {
            template = "ncmid1";
        } else if (level < levels - 1) {
            template = "ncupper1";
        } else {
            template = "nctop1";
        }
        return TGIds.id("dungeon/" + template + "/" + type.fileName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
