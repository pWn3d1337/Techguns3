package techguns.world.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The virtual world the military camp is generated in (Techguns 2 built it straight into the world and read back the
 * ground heights and blocks it placed). The terrain is a height per column, the top ground block; everything the camp
 * places is a block override on top of it (air included, to dig into the ground). Raising or lowering a column drops
 * the overrides the terrain now covers or cut away, like Techguns 2 {@code adjustHeightAtPos} did.
 * <p>
 * Columns outside the area keep the height of the nearest edge column.
 */
public final class CampLevel {
    /** What {@link #get} returns for terrain below the surface; only its identity matters to the generator. */
    static final BlockState GROUND = Blocks.DIRT.defaultBlockState();

    final int minX;
    final int minZ;
    final int sizeX;
    final int sizeZ;
    private final int[] heights;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final Map<BlockPos, ResourceKey<LootTable>> chests = new HashMap<>();
    private final Map<BlockPos, Spawner> spawners = new LinkedHashMap<>();

    public enum Spawner {
        /** Techguns 2 camp flag: one attack helicopter, spawned 64 blocks up. */
        HELICOPTER,
        /** Techguns 2 road spawner: three army soldiers. */
        SOLDIERS
    }

    /**
     * @param heights top ground block per column, x-major ({@code heights[x * sizeZ + z]})
     */
    CampLevel(int minX, int minZ, int sizeX, int sizeZ, int[] heights) {
        this.minX = minX;
        this.minZ = minZ;
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        this.heights = heights.clone();
    }

    private int index(int x, int z) {
        int ix = Math.clamp(x - minX, 0, sizeX - 1);
        int iz = Math.clamp(z - minZ, 0, sizeZ - 1);
        return ix * sizeZ + iz;
    }

    private boolean inside(int x, int z) {
        return x >= minX && x < minX + sizeX && z >= minZ && z < minZ + sizeZ;
    }

    /** Techguns 2 {@code getGroundY}: the top ground block. */
    public int groundY(int x, int z) {
        return heights[index(x, z)];
    }

    /** Moves the surface of a column; overrides between the old and the new surface go (now ground or cut away). */
    void setGroundY(int x, int z, int y) {
        if (!inside(x, z)) {
            return;
        }
        int old = heights[index(x, z)];
        for (int i = Math.min(old, y) + 1; i <= Math.max(old, y); i++) {
            blocks.remove(new BlockPos(x, i, z));
        }
        heights[index(x, z)] = y;
    }

    public BlockState get(int x, int y, int z) {
        BlockState state = blocks.get(new BlockPos(x, y, z));
        if (state != null) {
            return state;
        }
        return y <= groundY(x, z) ? GROUND : Blocks.AIR.defaultBlockState();
    }

    boolean isAir(int x, int y, int z) {
        return get(x, y, z).isAir();
    }

    void set(int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        blocks.put(pos, state);
        chests.remove(pos);
        spawners.remove(pos);
    }

    void setAir(int x, int y, int z) {
        set(x, y, z, Blocks.AIR.defaultBlockState());
    }

    void chest(int x, int y, int z, BlockState state, ResourceKey<LootTable> lootTable) {
        set(x, y, z, state);
        chests.put(new BlockPos(x, y, z), lootTable);
    }

    /** Joins the loot chests standing side by side into double chests, like 1.12 chests joined when placed. */
    void pairChests() {
        Map<BlockPos, BlockState> states = new HashMap<>();
        chests.keySet().forEach(pos -> states.put(pos, blocks.get(pos)));
        ChestPairs.pair(states).forEach(blocks::put);
    }

    void spawner(int x, int y, int z, BlockState state, Spawner type) {
        set(x, y, z, state);
        spawners.put(new BlockPos(x, y, z), type);
    }

    /** Techguns 2 {@code BlockUtils.fillBlocks}. */
    void fill(int x, int y, int z, int sizeX, int sizeY, int sizeZ, BlockState state) {
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeZ; j++) {
                for (int k = 0; k < sizeY; k++) {
                    set(x + i, y + k, z + j, state);
                }
            }
        }
    }

    /** Techguns 2 {@code BlockUtils.fillBlocksHollow}: the outer ring of each layer. */
    void fillHollow(int x, int y, int z, int sizeX, int sizeY, int sizeZ, BlockState state) {
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeZ; j++) {
                if (j == 0 || j == sizeZ - 1 || i == 0 || i == sizeX - 1) {
                    for (int k = 0; k < sizeY; k++) {
                        set(x + i, y + k, z + j, state);
                    }
                }
            }
        }
    }

    /**
     * Techguns 2 {@code BlockUtils.flattenArea}: squeezes the heights of the area into {@code maxHeightDiff} blocks
     * around their median (0: flat at the median). Techguns 2 used a median of 0 for areas of up to two columns, which
     * dug them down to the bottom of the world; here the median is always taken.
     */
    void flatten(int posX, int posZ, int sizeX, int sizeZ, int maxHeightDiff) {
        if (sizeX <= 0 || sizeZ <= 0) {
            return;
        }
        List<Integer> heights = new ArrayList<>(sizeX * sizeZ);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                int y = groundY(posX + x, posZ + z);
                heights.add(y);
                min = Math.min(min, y);
                max = Math.max(max, y);
            }
        }
        int span = max - min;
        Collections.sort(heights);
        // the Techguns 2 index arithmetic picks the lower median for both even and odd counts
        float median = heights.get(Math.max(0, heights.size() / 2 - 1));
        float f = median - maxHeightDiff / 2.0f;
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                int y = groundY(posX + x, posZ + z);
                int newY = span <= maxHeightDiff ? y : Math.round(((float) (y - min) / span) * maxHeightDiff + f);
                setGroundY(posX + x, posZ + z, newY);
            }
        }
    }

    /** Techguns 2 {@code BlockUtils.apply2DHeightmapFilter}, in place like the original (later columns see the new heights). */
    void filter(int posX, int posZ, int sizeX, int sizeZ, float[][] filter) {
        int xoffset = -filter.length / 2;
        int zoffset = -filter[0].length / 2;
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                float newY = 0.0f;
                for (int i = 0; i < filter.length; i++) {
                    for (int j = 0; j < filter[i].length; j++) {
                        newY += groundY(posX + x + xoffset + i, posZ + z + zoffset + j) * filter[i][j];
                    }
                }
                setGroundY(posX + x, posZ + z, Math.round(newY));
            }
        }
    }

    public Map<BlockPos, BlockState> blocks() {
        return Collections.unmodifiableMap(blocks);
    }

    public Map<BlockPos, ResourceKey<LootTable>> chests() {
        return Collections.unmodifiableMap(chests);
    }

    public Map<BlockPos, Spawner> spawners() {
        return Collections.unmodifiableMap(spawners);
    }
}
