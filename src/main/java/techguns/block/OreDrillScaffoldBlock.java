package techguns.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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

    /**
     * Techguns 2 {@code shouldSideBeRendered}: the formed (see-through) scaffold hides its faces towards formed scaffold
     * in the same state, so a sheet of it shows no inner faces; faces towards the frame and the drill plate stay.
     */
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return state.getValue(FORMED) && adjacent == state || super.skipRendering(state, adjacent, direction);
    }

    // lit and shaded like glass (see MultiblockGlassBlock): the see-through scaffold casts no ambient occlusion
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
