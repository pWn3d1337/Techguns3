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
 * dungeon templates).
 */
public enum DungeonPreset implements StringRepresentable {
    /**
     * Techguns 2 {@code PresetCastle}: dungeon level at the bottom, then lower, mid, upper and top floors, a roof; 2
     * zombie or skeleton soldiers per spawner.
     */
    CASTLE(5, 5, false, List.of("ncdung1", "nclower1", "ncmid1", "ncupper1", "nctop1", "ncroof1"), path -> {
        path.startHeightLevel = 1;
        path.chanceStraight = 0.8f;
        path.chanceRamp = 0.5f;
        path.chanceRoom = 0.25f;
        path.chanceFork = 0.4f;
        path.chanceUp = 0.65f;
        path.useFoundations = true;
        path.usePillars = true;
        path.useRoof = true;
    }, List.of(TGNpcs.ZOMBIE_SOLDIER, TGNpcs.SKELETON_SOLDIER)),
    /**
     * Techguns 2 {@code PresetNetherDungeonUnderground} (new: Techguns 2 never spawned it): a fortress under the Nether
     * floor, entered from a hut on the top level and a ramp three levels down; bottom, lower, mid and upper floors, the
     * bottom layer doubled, no roof; the castle spawners.
     */
    NETHER(5, 5, true, List.of("nether_b0", "nether_b1", "nether0", "nether1"), path -> {
        path.startHeightLevel = -1;
        path.chanceStraight = 0.7f;
        path.chanceRamp = 0.4f;
        path.chanceRoom = 0.35f;
        path.chanceFork = 0.5f;
        path.chanceUp = 0.6f;
        path.useFoundations = true;
        path.usePillars = false;
        path.useRoof = false;
        path.useBottomLayer = true;
        path.entranceRampLength = 3;
    }, List.of(TGNpcs.ZOMBIE_SOLDIER, TGNpcs.SKELETON_SOLDIER));

    public static final StringRepresentable.EnumCodec<DungeonPreset> CODEC = StringRepresentable.fromEnum(DungeonPreset::values);

    /** Blocks per cell side and per level. */
    public final int sizeXZ;
    public final int sizeY;
    /** Below the ground (the Nether fortress) instead of on it. */
    public final boolean underground;
    /** The Techguns 2 templates the preset picks its levels from. */
    public final List<String> templates;
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

    DungeonPreset(int sizeXZ, int sizeY, boolean underground, List<String> templates, Consumer<MazeDungeonPath> pathSettings,
                  List<TGNpcs.Npc> spawnerMobs) {
        this.sizeXZ = sizeXZ;
        this.sizeY = sizeY;
        this.underground = underground;
        this.templates = templates;
        this.pathSettings = pathSettings;
        this.spawnerMobs = spawnerMobs;
    }

    public void init(MazeDungeonPath path) {
        pathSettings.accept(path);
    }

    /**
     * Techguns 2 {@code getSegment}: the template of a segment on a level (-1: roof) of a dungeon {@code levels} high.
     * Castle: dungeon, lower, mid, upper, top, roof. Nether: bottom, lower, mid, then upper floors.
     */
    public ResourceLocation template(SegmentType type, int level, int levels) {
        String template = switch (this) {
            case CASTLE -> level == -1 ? "ncroof1" : level == 0 ? "ncdung1" : level <= 1 ? "nclower1" : level < levels - 2 ? "ncmid1"
                    : level < levels - 1 ? "ncupper1" : "nctop1";
            case NETHER -> level <= 0 ? "nether_b0" : level <= 1 ? "nether_b1" : level <= 2 ? "nether0" : "nether1";
        };
        return TGIds.id("dungeon/" + template + "/" + type.fileName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
