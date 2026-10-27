package techguns.machine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import techguns.multiblock.MultiblockPart;
import techguns.multiblock.MultiblockSchematic;
import techguns.registry.TGBlocks;

/**
 * Techguns 2 {@code FabricatorDefinition}: a 2x2x2 machine. Bottom layer: the controller, one housing behind it and two
 * on its left (seen from the used side), the housings forward items and power; top layer: four glass blocks.
 */
public final class FabricatorSchematic implements MultiblockSchematic {
    public static final FabricatorSchematic INSTANCE = new FabricatorSchematic();

    private FabricatorSchematic() {
    }

    @Override
    public boolean canFormFromSide(Direction side) {
        return side.getAxis().isHorizontal();
    }

    @Override
    public List<MultiblockPart> parts(Level level, BlockPos master, Direction direction) {
        BlockPos behind = master.relative(direction.getOpposite());
        Direction left = direction.getClockWise();
        List<BlockPos> bottom = List.of(behind, master.relative(left), behind.relative(left));
        List<MultiblockPart> parts = new ArrayList<>();
        bottom.forEach(pos -> parts.add(new MultiblockPart(pos, TGBlocks.FABRICATOR_HOUSING.get(), MultiblockPart.Connector.ITEMS_ENERGY)));
        parts.add(new MultiblockPart(master.above(), TGBlocks.FABRICATOR_GLASS.get(), MultiblockPart.Connector.NONE));
        bottom.forEach(pos -> parts.add(new MultiblockPart(pos.above(), TGBlocks.FABRICATOR_GLASS.get(), MultiblockPart.Connector.NONE)));
        return parts;
    }
}
