package techguns.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import techguns.multiblock.MultiblockPart;
import techguns.multiblock.MultiblockPartBlock;

/**
 * Ore Drill scaffold (Techguns 2 {@code EnumOreDrillType.SCAFFOLD}); in the drill's last layer it becomes the hidden
 * drill plate ({@code SCAFFOLD_HIDDEN}) while formed, the renderer draws the drill bit there.
 */
public class OreDrillScaffoldBlock extends MultiblockPartBlock {
    public static final BooleanProperty HIDDEN = BooleanProperty.create("hidden");
    public static final MapCodec<OreDrillScaffoldBlock> CODEC = simpleCodec(OreDrillScaffoldBlock::new);

    public OreDrillScaffoldBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HIDDEN, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HIDDEN);
    }

    @Override
    public BlockState formedState(BlockState state, MultiblockPart part) {
        return super.formedState(state, part).setValue(HIDDEN, part.hidden());
    }

    @Override
    public BlockState unformedState(BlockState state) {
        return super.unformedState(state).setValue(HIDDEN, false);
    }
}
