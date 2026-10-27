package techguns.multiblock;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/**
 * One block of a multiblock structure (Techguns 2 {@code MultiBlockMachineSchematic} position lists): {@code block}
 * must stand at {@code pos}; a null block means the position must be air. Linked parts forward the master's
 * capabilities by their {@code connector}; a {@code hidden} part shows nothing when formed (the renderer draws it,
 * e.g. the Ore Drill plate).
 */
public record MultiblockPart(BlockPos pos, @Nullable Block block, Connector connector, boolean hidden) {
    public MultiblockPart(BlockPos pos, @Nullable Block block, Connector connector) {
        this(pos, block, connector, false);
    }

    public static MultiblockPart air(BlockPos pos) {
        return new MultiblockPart(pos, null, Connector.NONE);
    }

    public boolean isAir() {
        return block == null;
    }

    /** What a linked part exposes of its master to pipes and cables (Techguns 2 slave types). */
    public enum Connector {
        NONE(false, false, false),
        /** Fabricator housing (Techguns 2 type 2). */
        ITEMS_ENERGY(true, false, true),
        /** Reaction Chamber side connector (type 2). */
        ITEMS_FLUIDS(true, true, false),
        /** Reaction Chamber top connector (type 3). */
        ENERGY(false, false, true),
        /** Ore Drill frame, scaffold and engines (Techguns 2 forwarded everything). */
        ALL(true, true, true);

        private final boolean items;
        private final boolean fluids;
        private final boolean energy;

        Connector(boolean items, boolean fluids, boolean energy) {
            this.items = items;
            this.fluids = fluids;
            this.energy = energy;
        }

        public boolean items() {
            return items;
        }

        public boolean fluids() {
            return fluids;
        }

        public boolean energy() {
            return energy;
        }
    }
}
