package techguns.world.structure;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootTable;
import techguns.TGIds;
import techguns.block.TGLampBlock;
import techguns.block.TGLanternBlock;
import techguns.registry.TGBlocks;
import techguns.world.LegacyPaletteProcessor.BiomeColor;

/**
 * The Techguns 2 military camp buildings ({@code WorldgenStructure} subclasses), placed into a {@link CampLevel}. Each
 * has the Techguns 2 size range (-1: no maximum) and gets the segment size, a height in its range and the direction
 * it faces (0 -x, 1 -z, 2 +x, 3 +z). Techguns 2 computed the camouflage colour of tents and watch towers but placed the
 * woodland nets anyway; here they take the biome colour like the other camouflage nets.
 */
final class CampBuildings {
    private static final ResourceKey<LootTable> BUNKER_LOOT = ResourceKey.create(Registries.LOOT_TABLE, TGIds.id("chests/militarybase_bunker"));
    private static final ResourceKey<LootTable> BARRACKS_LOOT = ResourceKey.create(Registries.LOOT_TABLE, TGIds.id("chests/militarybase_barracks"));

    abstract static class Building {
        final int minX;
        final int minY;
        final int minZ;
        final int maxX;
        final int maxY;
        final int maxZ;
        boolean canSwapXZ;

        Building(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        Building swapXZ() {
            canSwapXZ = true;
            return this;
        }

        /** Techguns 2 {@code getMatchingStructure}; with swapping, the minimum and maximum are checked independently. */
        boolean matches(int sizeX, int sizeZ) {
            if (!canSwapXZ) {
                return sizeX >= minX && sizeZ >= minZ && maxFits(sizeX, sizeZ);
            }
            return (sizeX >= minX && sizeZ >= minZ || sizeZ >= minX && sizeX >= minZ) && (maxFits(sizeX, sizeZ) || maxFits(sizeZ, sizeX));
        }

        private boolean maxFits(int sizeX, int sizeZ) {
            return (maxX == -1 || sizeX <= maxX) && (maxZ == -1 || sizeZ <= maxZ);
        }

        abstract void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color,
                            RandomSource rnd);
    }

    static final Building EMPTY_PLANE = new Building(1, 1, 1, -1, -1, -1) {
        @Override
        void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color, RandomSource rnd) {
            level.fill(posX, posY, posZ, sizeX, 1, sizeZ, Blocks.COBBLESTONE.defaultBlockState());
        }
    };

    static final List<Building> REGULAR = List.of(
            new Tent(5, 4, 5, 7, 5, 7, 0),
            new Tent(3, 4, 3, 6, 5, 6, 1),
            new Tent(4, 4, 4, 8, 6, 8, 2),
            new Containers(2, 3, 2, 7, 3, 7, 2, 2),
            new CampProps(1, 3, 1, 5, 5, 5, 0), // crates
            new CampProps(3, 5, 3, 6, 7, 6, 1), // crates + roof
            new Bunker(5, 3, 5, 8, 3, 8, 0), // no roof
            new Barracks(7, 7, 7, -1, -1, -1),
            new Helipad(7, 7, 7, 10, -1, 10),
            new Tanks(4, 5, 4, 10, -1, 10),
            new CampProps(3, 5, 3, 10, 7, 10, 5), // crates + roof + sandbags
            new Bunker(3, 3, 5, 15, 3, 15, 2).swapXZ(), // closed bunker
            new Bunker(4, 3, 6, 15, 3, 15, 3).swapXZ()); // closed bunker with sandbag roof

    static final List<Building> BORDER = List.of(
            new Bunker(5, 3, 5, 8, 3, 8, 0), // no roof
            new Bunker(5, 4, 5, -1, 4, -1, 1), // roof
            new CampProps(3, 2, 3, 8, 2, 8, 2), // sandbags
            new CampProps(3, 4, 3, 8, 5, 8, 3), // sandbags + roof
            new CampProps(1, 2, 3, -1, 4, -1, 4).swapXZ(), // row of sandbags
            new Bunker(3, 3, 5, 10, 3, 10, 2).swapXZ(),
            new Bunker(4, 3, 6, 10, 3, 10, 3).swapXZ());

    static final List<Building> CORNER = List.of(
            new WatchTowerSmall(3, 8, 3, 4, 8, -1, 3).swapXZ(),
            new WatchTowerSmall(4, 10, 4, -1, 12, -1, 4));

    private CampBuildings() {
    }

    /** Techguns 2 {@code directionToFacing}. */
    static Direction facing(int direction) {
        return switch (direction) {
            case 0 -> Direction.WEST;
            case 2 -> Direction.EAST;
            case 3 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
    }

    static BlockState camoNet(BiomeColor color) {
        return (switch (color) {
            case DESERT -> TGBlocks.CAMO_NET_DESERT;
            case SNOW -> TGBlocks.CAMO_NET_SNOW;
            default -> TGBlocks.CAMO_NET_WOOD;
        }).get().defaultBlockState();
    }

    static BlockState camoNetTop(BiomeColor color) {
        return (switch (color) {
            case DESERT -> TGBlocks.CAMO_NET_TOP_DESERT;
            case SNOW -> TGBlocks.CAMO_NET_TOP_SNOW;
            default -> TGBlocks.CAMO_NET_TOP_WOOD;
        }).get().defaultBlockState();
    }

    /** Techguns 2 {@code MBlockRegister.crates}: the nine military crates. */
    static BlockState randomCrate(RandomSource rnd) {
        List<Block> crates = List.of(TGBlocks.MILITARY_CRATE_AMMO.get(), TGBlocks.MILITARY_CRATE_GUN.get(), TGBlocks.MILITARY_CRATE_ARMOR.get(),
                TGBlocks.MILITARY_CRATE_MEDICAL.get(), TGBlocks.MILITARY_CRATE_EXPLOSIVE.get(), TGBlocks.MILITARY_CRATE_GENERIC_OAK.get(),
                TGBlocks.MILITARY_CRATE_GENERIC_JUNGLE.get(), TGBlocks.MILITARY_CRATE_GENERIC_BIRCH.get(), TGBlocks.MILITARY_CRATE_GENERIC_SPRUCE.get());
        return crates.get(rnd.nextInt(crates.size())).defaultBlockState();
    }

    /** A Techguns lamp attached to the block in {@code towards}. */
    static BlockState lamp(Direction towards) {
        return TGBlocks.LAMP_YELLOW.get().defaultBlockState().setValue(TGLampBlock.FACING, towards);
    }

    static void door(CampLevel level, int x, int y, int z, Block door, Direction facing, DoorHingeSide hinge) {
        BlockState state = door.defaultBlockState().setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HINGE, hinge);
        level.set(x, y, z, state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        level.set(x, y + 1, z, state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** Techguns 2 {@code Tent}: camouflage net walls and roof on gravel, a door (0, 1) or open sides (2). */
    static final class Tent extends Building {
        private final int type;

        Tent(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int type) {
            super(minX, minY, minZ, maxX, maxY, maxZ);
            this.type = type;
        }

        @Override
        void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color, RandomSource rnd) {
            if (type == 0) {
                posX++;
                posZ++;
                sizeX -= 2;
                sizeZ -= 2;
            }
            BlockState ground = Blocks.GRAVEL.defaultBlockState();
            BlockState wall = camoNet(color);
            BlockState roof = camoNetTop(color);
            for (int i = 0; i < sizeX; i++) {
                for (int j = 0; j < sizeZ; j++) {
                    for (int k = 0; k < sizeY; k++) {
                        if (k == 0) {
                            level.set(posX + i, posY + k, posZ + j, ground);
                        } else if (k < sizeY - 1) {
                            if (j == 0 || j == sizeZ - 1 || i == 0 || i == sizeX - 1) {
                                level.set(posX + i, posY + k, posZ + j, wall);
                            }
                        } else {
                            level.set(posX + i, posY + k, posZ + j, roof);
                        }
                    }
                }
            }
            if (type == 0 || type == 1) {
                int x;
                int z;
                int outX;
                int outZ;
                switch (direction) {
                    case 0 -> {
                        x = posX;
                        z = posZ + sizeZ / 2;
                        outX = x - 1;
                        outZ = z;
                    }
                    case 1 -> {
                        x = posX + sizeX / 2;
                        z = posZ;
                        outX = x;
                        outZ = z - 1;
                    }
                    case 2 -> {
                        x = posX + sizeX - 1;
                        z = posZ + sizeZ / 2;
                        outX = x + 1;
                        outZ = z;
                    }
                    default -> {
                        x = posX + sizeX / 2;
                        z = posZ + sizeZ - 1;
                        outX = x;
                        outZ = z + 1;
                    }
                }
                level.setAir(x, posY + 1, z);
                level.setAir(x, posY + 2, z);
                if (type == 0) {
                    level.set(outX, posY, outZ, ground);
                }
            }
            if (type == 2) {
                for (int y = 1; y < sizeY - 1; y++) {
                    if (direction == 0 || direction == 2) {
                        int x = direction == 0 ? 0 : sizeX - 1;
                        for (int z = 1; z < sizeZ - 1; z++) {
                            if (z % 3 != 0) {
                                level.setAir(posX + x, posY + y, posZ + z);
                            }
                        }
                    } else {
                        int z = direction == 1 ? 0 : sizeZ - 1;
                        for (int x = 1; x < sizeX - 1; x++) {
                            if (x % 3 != 1) {
                                level.setAir(posX + x, posY + y, posZ + z);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Techguns 2 {@code Containers}: rows of red shipping containers. */
    static final class Containers extends Building {
        private final int containerWidth;
        private final int containerHeight;

        Containers(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int containerWidth, int containerHeight) {
            super(minX, minY, minZ, maxX, maxY, maxZ);
            this.containerWidth = containerWidth;
            this.containerHeight = containerHeight;
        }

        @Override
        void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color, RandomSource rnd) {
            BlockState container = TGBlocks.CONTAINER_RED.get().defaultBlockState();
            int offset = 0;
            if (sizeX > sizeZ) {
                int count = (int) ((sizeZ + 1.0f) / (containerWidth + 1.0f));
                for (int i = 0; i < count; i++) {
                    level.fill(posX, posY + 1, posZ + offset, sizeX, containerHeight, containerWidth, container);
                    offset += containerWidth + 1;
                }
            } else {
                int count = (int) ((sizeX + 1.0f) / (containerWidth + 1.0f));
                for (int i = 0; i < count; i++) {
                    level.fill(posX + offset, posY + 1, posZ, containerWidth, containerHeight, sizeZ, container);
                    offset += containerWidth + 1;
                }
            }
        }
    }

    /** Techguns 2 {@code CampProps}: crate piles, camouflage roofs and sandbag walls in six variants. */
    static final class CampProps extends Building {
        private final int variant;

        CampProps(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int variant) {
            super(minX, minY, minZ, maxX, maxY, maxZ);
            this.variant = variant;
        }

        @Override
        void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color, RandomSource rnd) {
            switch (variant) {
                case 1 -> {
                    setRoofBlocks(level, posX, posY, posZ, sizeX, sizeY, sizeZ, color);
                    setCrateBlocks(level, posX + 1, posY, posZ + 1, sizeX - 2, sizeY - 2, sizeZ - 2, rnd);
                }
                case 2 -> setSandbagBlocks(level, posX, posY, posZ, sizeX, sizeZ, direction);
                case 3 -> {
                    setRoofBlocks(level, posX, posY + 1, posZ, sizeX, sizeY - 1, sizeZ, color);
                    setSandbagBlocks(level, posX, posY, posZ, sizeX, sizeZ, direction);
                }
                case 4 -> setSandbagRow(level, posX, posY, posZ, sizeX, sizeY, sizeZ);
                case 5 -> {
                    setRoofBlocks(level, posX, posY + 1, posZ, sizeX, sizeY - 1, sizeZ, color);
                    setSandbagBlocks(level, posX, posY, posZ, sizeX, sizeZ, direction);
                    setCrateBlocks(level, posX + 1, posY, posZ + 1, sizeX - 2, sizeY - 2, sizeZ - 2, rnd);
                }
                default -> setCrateBlocks(level, posX, posY, posZ, sizeX, sizeY, sizeZ, rnd);
            }
        }

        /** A pyramid-shaped sandbag wall along the long side. */
        private static void setSandbagRow(CampLevel level, int px, int py, int pz, int sizeX, int sizeY, int sizeZ) {
            BlockState sandbags = TGBlocks.SANDBAGS.get().defaultBlockState();
            int i = 1;
            for (int y = 1; y < sizeY; y++) {
                if (sizeX > sizeZ) {
                    for (int x = i; x < sizeX - i; x++) {
                        level.set(px + x, py + y, pz + sizeZ / 2, sandbags);
                    }
                } else {
                    for (int z = i; z < sizeZ - i; z++) {
                        level.set(px + sizeX / 2, py + y, pz + z, sandbags);
                    }
                }
                i++;
            }
        }

        /** A ring of sandbags with a gap on the front side. */
        private static void setSandbagBlocks(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeZ, int direction) {
            BlockState sandbags = TGBlocks.SANDBAGS.get().defaultBlockState();
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    if (x == 0 || x == sizeX - 1 || z == 0 || z == sizeZ - 1) {
                        level.set(posX + x, posY + 1, posZ + z, sandbags);
                    }
                }
            }
            switch (direction) {
                case 0 -> level.setAir(posX, posY + 1, posZ + sizeZ / 2);
                case 1 -> level.setAir(posX + sizeX / 2, posY + 1, posZ);
                case 2 -> level.setAir(posX + sizeX - 1, posY + 1, posZ + sizeZ / 2);
                default -> level.setAir(posX + sizeX / 2, posY + 1, posZ + sizeZ - 1);
            }
        }

        /** Four fence posts and a camouflage net roof. */
        private static void setRoofBlocks(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, BiomeColor color) {
            BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
            level.fill(posX, posY + 1, posZ, 1, sizeY - 2, 1, fence);
            level.fill(posX + sizeX - 1, posY + 1, posZ, 1, sizeY - 2, 1, fence);
            level.fill(posX, posY + 1, posZ + sizeZ - 1, 1, sizeY - 2, 1, fence);
            level.fill(posX + sizeX - 1, posY + 1, posZ + sizeZ - 1, 1, sizeY - 2, 1, fence);
            level.fill(posX, posY + sizeY - 1, posZ, sizeX, 1, sizeZ, camoNetTop(color));
        }

        /** Random crate stacks on gravel, sometimes with a lantern on top. */
        private static void setCrateBlocks(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, RandomSource rnd) {
            BlockState gravel = Blocks.GRAVEL.defaultBlockState();
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    level.set(posX + x, posY, posZ + z, gravel);
                    rnd.nextFloat(); // Techguns 2 chest roll, its chests were commented out
                    for (int y = 1; y < sizeY; y++) {
                        if (!isFreeSpace(level, posX + x, posY + y - 1, posZ + z) && rnd.nextFloat() > 0.5f) {
                            level.set(posX + x, posY + y, posZ + z, randomCrate(rnd));
                        }
                    }
                }
            }
            if ((sizeX + sizeZ) / 2.0f / 6.0f > rnd.nextFloat()) {
                BlockState lantern = TGBlocks.LAMP_YELLOW_LANTERN.get().defaultBlockState().setValue(PipeBlock.DOWN, true);
                for (int y = sizeY; y > 1; y--) {
                    for (int t = 0; t < 3; t++) {
                        int x = rnd.nextInt(sizeX);
                        int z = rnd.nextInt(sizeZ);
                        if (level.isAir(posX + x, posY + y, posZ + z) && !isFreeSpace(level, posX + x, posY + y - 1, posZ + z)) {
                            level.set(posX + x, posY + y, posZ + z, lantern);
                            return;
                        }
                    }
                }
            }
        }

        private static boolean isFreeSpace(CampLevel level, int x, int y, int z) {
            BlockState state = level.get(x, y, z);
            return state.isAir() || state.is(Blocks.SNOW) || state.getBlock() instanceof TGLampBlock || state.getBlock() instanceof TGLanternBlock;
        }
    }

    /**
     * Techguns 2 {@code Bunker}: a concrete bunker sunk one block into the ground, with a bunker door, steps down to
     * it and lamps; types 0 (windows), 1 (windows, sandbag roof, ladder up), 2 (closed, crates and loot chests), 3
     * (closed, crates, sandbag roof).
     */
    static final class Bunker extends Building {
        private final int type;

        Bunker(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int type) {
            super(minX, minY, minZ, maxX, maxY, maxZ);
            this.type = type;
        }

        @Override
        void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color, RandomSource rnd) {
            BlockState ground = TGBlocks.CONCRETE_BROWN_LIGHT.get().defaultBlockState();
            BlockState wall = TGBlocks.CONCRETE_BROWN.get().defaultBlockState();
            BlockState roof = TGBlocks.CONCRETE_GREY.get().defaultBlockState();
            BlockState sandbags = TGBlocks.SANDBAGS.get().defaultBlockState();
            if (sizeX <= 4 && sizeZ >= 5) {
                if (direction == 0 || direction == 2) {
                    direction = (direction + 1) % 4;
                }
            } else if (sizeZ <= 4 && sizeX >= 5) {
                if (direction == 1 || direction == 3) {
                    direction = (direction + 1) % 4;
                }
            }
            switch (direction) {
                case 0 -> {
                    posX += 2;
                    sizeX -= 2;
                }
                case 1 -> {
                    posZ += 2;
                    sizeZ -= 2;
                }
                case 2 -> sizeX -= 2;
                default -> sizeZ -= 2;
            }
            Direction facing = facing(direction);
            for (int y = -1; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    for (int z = 0; z < sizeZ; z++) {
                        boolean edge = x == 0 || x == sizeX - 1 || z == 0 || z == sizeZ - 1;
                        if (y == -1) {
                            level.set(posX + x, posY + y, posZ + z, ground);
                        } else if (y == 0) {
                            if (edge) {
                                level.set(posX + x, posY + y, posZ + z, wall);
                            } else {
                                level.setAir(posX + x, posY + y, posZ + z);
                            }
                        } else if (y == 1) {
                            if (type == 2 || type == 3) { // no windows
                                if (edge) {
                                    level.set(posX + x, posY + y, posZ + z, wall);
                                }
                            } else if ((x == 0 || x == sizeX - 1) && (z == 0 || z == sizeZ - 1 || z % 3 == 0)
                                    || (x == 0 || x == sizeX - 1 || x % 3 == 0) && (z == 0 || z == sizeZ - 1)) {
                                level.set(posX + x, posY + y, posZ + z, wall);
                            }
                        } else if (y == 2) {
                            level.set(posX + x, posY + y, posZ + z, edge ? wall : roof);
                        } else if (y == 3 && (type == 1 || type == 3) && edge) { // sandbag roof
                            level.set(posX + x, posY + y, posZ + z, sandbags);
                        }
                    }
                }
            }
            int xoffset;
            int zoffset;
            int dx;
            int dz;
            switch (direction) {
                case 0 -> {
                    xoffset = 0;
                    zoffset = sizeZ / 2;
                    dx = -1;
                    dz = 0;
                }
                case 1 -> {
                    xoffset = sizeX / 2;
                    zoffset = 0;
                    dx = 0;
                    dz = -1;
                }
                case 2 -> {
                    xoffset = sizeX - 1;
                    zoffset = sizeZ / 2;
                    dx = 1;
                    dz = 0;
                }
                default -> {
                    xoffset = sizeX / 2;
                    zoffset = sizeZ - 1;
                    dx = 0;
                    dz = 1;
                }
            }
            int doorX = posX + xoffset;
            int doorZ = posZ + zoffset;
            // the door, the walls beside it and the side walls of the steps in front
            door(level, doorX, posY, doorZ, TGBlocks.BUNKER_DOOR.get(), facing, DoorHingeSide.RIGHT);
            int sx = dx == 0 ? 1 : 0;
            int sz = dz == 0 ? 1 : 0;
            for (int side : new int[] {1, -1}) {
                level.set(doorX + sx * side, posY, doorZ + sz * side, wall);
                level.set(doorX + sx * side, posY + 1, doorZ + sz * side, wall);
            }
            for (int side : new int[] {1, -1}) {
                for (int step = 1; step <= 2; step++) {
                    int wx = doorX + (dx == 0 ? side : dx * step);
                    int wz = doorZ + (dz == 0 ? side : dz * step);
                    level.set(wx, posY - 1, wz, wall);
                    level.set(wx, posY, wz, wall);
                }
            }
            for (int side : new int[] {1, -1}) {
                level.set(doorX + (dx == 0 ? side : dx), posY + 2, doorZ + (dz == 0 ? side : dz), lamp(facing.getOpposite()));
            }
            level.set(doorX, posY - 1, doorZ, ground);
            level.set(doorX + dx, posY - 1, doorZ + dz, ground);
            level.setAir(doorX + dx, posY, doorZ + dz);
            level.setAir(doorX + dx, posY + 1, doorZ + dz);
            level.set(doorX + dx * 2, posY, doorZ + dz * 2, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing));
            level.setAir(doorX + dx * 2, posY + 1, doorZ + dz * 2);

            if (type == 2 || type == 3) {
                level.set(doorX - dx, posY + 1, doorZ - dz, lamp(Direction.UP));
                setCrateBlocks(level, posX + 2, posY, posZ + 2, sizeX - 4, 2, sizeZ - 4, rnd);
            } else {
                level.set(posX + sizeX / 2, posY + 1, posZ + sizeZ / 2, lamp(Direction.UP));
            }

            if (type == 1 || type == 3) { // a ladder up to the roof in the corner behind the door
                Direction ladderFacing;
                switch (direction) {
                    case 0 -> {
                        xoffset = sizeX - 2;
                        zoffset = sizeZ - 2;
                        dx = 1;
                        dz = 0;
                        ladderFacing = Direction.WEST;
                    }
                    case 1 -> {
                        xoffset = 1;
                        zoffset = sizeZ - 2;
                        dx = 0;
                        dz = 1;
                        ladderFacing = Direction.NORTH;
                    }
                    case 2 -> {
                        xoffset = 1;
                        zoffset = 1;
                        dx = -1;
                        dz = 0;
                        ladderFacing = Direction.EAST;
                    }
                    default -> {
                        xoffset = sizeX - 2;
                        zoffset = 1;
                        dx = 0;
                        dz = -1;
                        ladderFacing = Direction.SOUTH;
                    }
                }
                level.setAir(posX + xoffset, posY + 2, posZ + zoffset);
                level.set(posX + xoffset + dx, posY + 1, posZ + zoffset + dz, wall);
                BlockState ladder = TGBlocks.LADDER_METAL.get().defaultBlockState().setValue(LadderBlock.FACING, ladderFacing);
                for (int y = 0; y <= 2; y++) {
                    level.set(posX + xoffset, posY + y, posZ + zoffset, ladder);
                }
            }
        }

        /** Crate stacks with a 1 in 10 chance of a loot chest per column. */
        private static void setCrateBlocks(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, RandomSource rnd) {
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    if (rnd.nextFloat() <= 0.1f) {
                        Direction facing = Direction.from2DDataValue(rnd.nextInt(4));
                        level.chest(posX + x, posY, posZ + z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), BUNKER_LOOT);
                    } else {
                        for (int y = 0; y < sizeY; y++) {
                            if (!level.isAir(posX + x, posY + y - 1, posZ + z) && rnd.nextFloat() > 0.5) {
                                level.set(posX + x, posY + y, posZ + z, randomCrate(rnd));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Techguns 2 {@code Barracks}: a wooden house on log pillars, one room with beds and loot chests along the walls,
     * stairs up to the door; its size snaps to the 4-block pillar grid.
     */
    static final class Barracks extends Building {
        Barracks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            super(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color, RandomSource rnd) {
            BlockState pillar = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
            BlockState wallBlock = Blocks.OAK_PLANKS.defaultBlockState();
            BlockState floor = Blocks.OAK_PLANKS.defaultBlockState();
            BlockState scaffold = Blocks.DARK_OAK_PLANKS.defaultBlockState();
            BlockState roof = Blocks.SPRUCE_PLANKS.defaultBlockState();
            BlockState roofSlab = Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            BlockState ground = Blocks.GRAVEL.defaultBlockState();
            BlockState window = Blocks.GLASS_PANE.defaultBlockState();
            Direction facing = facing(direction);
            switch (direction) {
                case 0 -> {
                    posX += 2;
                    sizeX -= 2;
                }
                case 1 -> {
                    posZ += 2;
                    sizeZ -= 2;
                }
                case 2 -> sizeX -= 2;
                default -> sizeZ -= 2;
            }
            sizeX -= (sizeX - 1) % 4;
            sizeZ -= (sizeZ - 1) % 4;
            sizeY = 7;
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    boolean edge = x == 0 || z == 0 || x == sizeX - 1 || z == sizeZ - 1;
                    for (int y = 0; y < sizeY; y++) {
                        int bx = posX + x;
                        int by = posY + y;
                        int bz = posZ + z;
                        if (y == 0) {
                            level.set(bx, by, bz, ground);
                        } else if (y == 1) {
                            if (x % 4 == 0 && z % 4 == 0) {
                                level.set(bx, by, bz, pillar);
                            }
                        } else if (y == 2) {
                            level.set(bx, by, bz, edge ? scaffold : floor);
                        } else if (y <= 4) {
                            if (edge) {
                                if (x % 4 == 0 && z % 4 == 0) {
                                    level.set(bx, by, bz, scaffold);
                                } else if (y == 4 && (x % 4 == 2 || z % 4 == 2)) {
                                    level.set(bx, by, bz, window);
                                } else {
                                    level.set(bx, by, bz, wallBlock);
                                }
                            }
                        } else if (y == 5) {
                            if (edge) {
                                level.set(bx, by, bz, scaffold);
                            }
                        } else {
                            level.set(bx, by, bz, edge ? roofSlab : roof);
                        }
                    }
                }
            }
            int xoffset;
            int zoffset;
            int dx;
            int dz;
            switch (direction) {
                case 0 -> {
                    xoffset = 0;
                    zoffset = sizeZ / 8 * 4 + 2;
                    dx = -1;
                    dz = 0;
                }
                case 1 -> {
                    xoffset = sizeX / 8 * 4 + 2;
                    zoffset = 0;
                    dx = 0;
                    dz = -1;
                }
                case 2 -> {
                    xoffset = sizeX - 1;
                    zoffset = sizeZ / 8 * 4 + 2;
                    dx = 1;
                    dz = 0;
                }
                default -> {
                    xoffset = sizeX / 8 * 4 + 2;
                    zoffset = sizeZ - 1;
                    dx = 0;
                    dz = 1;
                }
            }
            int doorX = posX + xoffset;
            int doorZ = posZ + zoffset;
            door(level, doorX, posY + 3, doorZ, Blocks.OAK_DOOR, facing.getOpposite(), DoorHingeSide.LEFT);
            level.set(doorX + dx, posY + 5, doorZ + dz, lamp(facing.getOpposite()));
            BlockState stairs = Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing.getOpposite());
            level.set(doorX + dx, posY + 2, doorZ + dz, stairs);
            level.set(doorX + dx * 2, posY + 1, doorZ + dz * 2, stairs);

            int xHalf = sizeX / 2;
            int zHalf = sizeZ / 2;
            for (int x = 1; x < sizeX - 1; x++) {
                for (int z = 1; z < sizeZ - 1; z++) {
                    for (int y = 3; y < 6; y++) {
                        int bx = posX + x;
                        int by = posY + y;
                        int bz = posZ + z;
                        if (x == 1 || z == 1 || x == sizeX - 2 || z == sizeZ - 2) {
                            if (y == 5 && (x % 4 == 2 || z % 4 == 2)) {
                                level.set(bx, by, bz, lamp(Direction.UP));
                            }
                            if (y == 3 && (x % 4 == 0 || z % 4 == 0) && x != xoffset && z != zoffset) {
                                Direction chestFacing = Direction.EAST;
                                if (z % 4 == 0) {
                                    if (x > xHalf) {
                                        chestFacing = chestFacing.getOpposite();
                                    }
                                } else {
                                    chestFacing = z > zHalf ? Direction.NORTH : Direction.SOUTH;
                                }
                                level.chest(bx, by, bz, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, chestFacing), BARRACKS_LOOT);
                            }
                        } else {
                            if ((x == 2 || z == 2 || x == sizeX - 3 || z == sizeZ - 3) && (sizeX / 4 > 1 || sizeZ / 4 > 1)
                                    && y == 3 && x % 4 == 2 && z % 4 == 2 && x != xoffset && z != zoffset) {
                                if (x == 2) {
                                    bed(level, bx, by, bz, Direction.WEST);
                                } else if (x == sizeX - 3) {
                                    bed(level, bx, by, bz, Direction.EAST);
                                } else if (z == 2) {
                                    bed(level, bx, by, bz, Direction.NORTH);
                                } else if (z == sizeZ - 3) {
                                    bed(level, bx, by, bz, Direction.SOUTH);
                                }
                            }
                            if (y == 5 && x % 8 == 0 && z % 8 == 0) {
                                level.set(bx, by, bz, lamp(Direction.UP));
                            }
                        }
                    }
                }
            }
        }

        /** A red bed, foot at the position, head towards {@code facing}. */
        private static void bed(CampLevel level, int x, int y, int z, Direction facing) {
            BlockState bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, facing);
            level.set(x, y, z, bed.setValue(BedBlock.PART, BedPart.FOOT));
            level.set(x + facing.getStepX(), y, z + facing.getStepZ(), bed.setValue(BedBlock.PART, BedPart.HEAD));
        }
    }

    /** Techguns 2 {@code Helipad}: a 7x7 concrete pad with a hazard border and a lit H, steel frame rim from 9x9. */
    static final class Helipad extends Building {
        Helipad(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            super(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color, RandomSource rnd) {
            if (sizeX < 7 || sizeZ < 7) {
                return;
            }
            if (sizeX > 9) {
                posX += (sizeX - 9) / 2;
                sizeX = 9;
            }
            if (sizeZ > 9) {
                posZ += (sizeZ - 9) / 2;
                sizeZ = 9;
            }
            BlockState frame = TGBlocks.STEELFRAME_DARK.get().defaultBlockState();
            if (sizeX == 9 && sizeZ == 9) {
                for (int z = 1; z < sizeZ - 1; z++) {
                    level.set(posX, posY, posZ + z, frame);
                    level.set(posX + sizeX - 1, posY, posZ + z, frame);
                }
                posX++;
                sizeX = 7;
                for (int x = 0; x < sizeX; x++) {
                    level.set(posX + x, posY, posZ, frame);
                    level.set(posX + x, posY, posZ + sizeZ - 1, frame);
                }
                posZ++;
            }
            sizeX = 7;
            sizeZ = 7;
            BlockState hazard = Blocks.YELLOW_CONCRETE.defaultBlockState();
            BlockState concrete = TGBlocks.CONCRETE_GREY_DARK.get().defaultBlockState();
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    boolean edge = x == 0 || z == 0 || x == sizeX - 1 || z == sizeZ - 1;
                    level.set(posX + x, posY + 1, posZ + z, edge ? hazard : concrete);
                }
            }
            BlockState light = TGBlocks.NEONSQUARE_WHITE.get().defaultBlockState();
            for (int[] h : new int[][] {{2, 2}, {3, 2}, {4, 2}, {3, 3}, {2, 4}, {3, 4}, {4, 4}}) {
                level.set(posX + h[0], posY + 1, posZ + h[1], light);
            }
        }
    }

    /** Techguns 2 {@code Tanks}: rows of lava tanks of steel frame and container panels. */
    static final class Tanks extends Building {
        private static final int TANK_WIDTH = 4;

        Tanks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            super(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color, RandomSource rnd) {
            if (sizeX > sizeZ) {
                int count = (int) ((sizeZ + 1.0f) / (TANK_WIDTH + 1.0f));
                for (int i = 0, offset = 0; i < count; i++, offset += TANK_WIDTH + 1) {
                    tank(level, posX, posY, posZ + offset, sizeX, false);
                }
            } else {
                int count = (int) ((sizeX + 1.0f) / (TANK_WIDTH + 1.0f));
                for (int i = 0, offset = 0; i < count; i++, offset += TANK_WIDTH + 1) {
                    tank(level, posX + offset, posY, posZ, sizeZ, true);
                }
            }
        }

        /**
         * One tank, {@code length} long along x (or z when {@code alongZ}), 4 wide from {@code (x, z)}: the Techguns 2
         * block list, written in tank coordinates ({@code l} along, {@code w} across).
         */
        private static void tank(CampLevel level, int x, int y, int z, int length, boolean alongZ) {
            BlockState base = TGBlocks.STEELFRAME_SCAFFOLD.get().defaultBlockState();
            BlockState wall = TGBlocks.CONTAINER_RED.get().defaultBlockState();
            BlockState border = TGBlocks.CONTAINER_ORANGE.get().defaultBlockState();
            BlockState fill = Blocks.LAVA.defaultBlockState();
            Placer p = (l, h, w, state) -> {
                if (alongZ) {
                    level.set(x + w, y + h, z + l, state);
                } else {
                    level.set(x + l, y + h, z + w, state);
                }
            };
            int last = length - 1;
            for (int w : new int[] {0, TANK_WIDTH - 1}) { // feet
                p.set(1, 1, w, base);
                p.set(last - 1, 1, w, base);
                p.set(1, 2, w, base);
                p.set(last - 1, 2, w, base);
            }
            for (int l = 1; l < last; l++) {
                p.set(l, 1, 1, base);
                p.set(l, 1, 2, base);
                p.set(l, 2, 1, wall);
                p.set(l, 2, 2, wall);
                for (int h = 3; h <= 4; h++) {
                    p.set(l, h, 0, wall);
                    p.set(l, h, 3, wall);
                    p.set(l, h, 1, fill);
                    p.set(l, h, 2, fill);
                }
                p.set(l, 5, 1, wall);
                p.set(l, 5, 2, wall);
            }
            for (int l : new int[] {0, last}) { // the end caps
                p.set(l, 2, 1, border);
                p.set(l, 2, 2, border);
                for (int h = 3; h <= 4; h++) {
                    p.set(l, h, 0, border);
                    p.set(l, h, 3, border);
                    p.set(l, h, 1, wall);
                    p.set(l, h, 2, wall);
                }
                p.set(l, 5, 1, border);
                p.set(l, 5, 2, border);
            }
        }

        private interface Placer {
            void set(int along, int height, int across, BlockState state);
        }
    }

    /** Techguns 2 {@code WatchTowerSmall}: a wooden tower on fence posts with a ladder, railing and camouflage roof. */
    static final class WatchTowerSmall extends Building {
        private final int towerSize;

        WatchTowerSmall(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int towerSize) {
            super(minX, minY, minZ, maxX, maxY, maxZ);
            this.towerSize = towerSize;
        }

        @Override
        void place(CampLevel level, int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, int direction, BiomeColor color, RandomSource rnd) {
            if (sizeX >= towerSize + 2) {
                posX += (sizeX - towerSize) / 2;
            }
            if (sizeZ >= towerSize + 2) {
                posZ += (sizeZ - towerSize) / 2;
            }
            BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
            BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
            int far = towerSize - 1;
            for (int y = 1; y < sizeY - 1; y++) {
                level.set(posX, posY + y, posZ, fence);
                level.set(posX + far, posY + y, posZ, fence);
                level.set(posX, posY + y, posZ + far, fence);
                level.set(posX + far, posY + y, posZ + far, fence);
                if (y < sizeY - 3) {
                    for (int x = 1; x < far; x++) {
                        for (int z = 1; z < far; z++) {
                            level.set(posX + x, posY + y, posZ + z, planks);
                        }
                    }
                }
            }
            level.fillHollow(posX, posY + sizeY - 4, posZ, towerSize, 2, towerSize, fence);
            level.fill(posX, posY + sizeY - 1, posZ, towerSize, 1, towerSize, camoNetTop(color));
            int xoffset;
            int zoffset;
            Direction ladderFacing;
            switch (direction) {
                case 0 -> {
                    xoffset = 0;
                    zoffset = 1;
                    ladderFacing = Direction.WEST;
                }
                case 1 -> {
                    xoffset = 1;
                    zoffset = 0;
                    ladderFacing = Direction.NORTH;
                }
                case 2 -> {
                    xoffset = far;
                    zoffset = towerSize - 2;
                    ladderFacing = Direction.EAST;
                }
                default -> {
                    xoffset = towerSize - 2;
                    zoffset = far;
                    ladderFacing = Direction.SOUTH;
                }
            }
            BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, ladderFacing);
            for (int y = 1; y < sizeY - 3; y++) {
                level.set(posX + xoffset, posY + y, posZ + zoffset, ladder);
            }
            level.set(posX + xoffset, posY + sizeY - 3, posZ + zoffset,
                    Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, facing(direction)));
        }
    }
}
