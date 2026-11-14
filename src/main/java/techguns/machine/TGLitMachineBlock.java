package techguns.machine;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * A cube machine with a lit front while it works (generators burning, the Electric Furnace smelting), like the vanilla
 * furnace; the block entity sets {@link #LIT}. Give the block properties a light level for lit states.
 */
public class TGLitMachineBlock extends TGMachineBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final MapCodec<TGLitMachineBlock> codec;

    public TGLitMachineBlock(Properties properties, Supplier<? extends BlockEntityType<? extends TGMachineBlockEntity>> type) {
        super(properties, type, Shapes.block(), RenderShape.MODEL);
        this.codec = simpleCodec(p -> new TGLitMachineBlock(p, type));
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    /** Light level 13 while lit (the vanilla furnace). */
    public static int lightLevel(BlockState state) {
        return state.getValue(LIT) ? 13 : 0;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }
}
