package techguns.debug;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import techguns.world.structure.dungeon.DungeonDebug;

/** The Techguns 2 dungeon scanner and generator blocks (debug only): use opens the tool's screen. */
public class DungeonToolBlock extends BaseEntityBlock {
    private final Supplier<? extends BlockEntityType<? extends DungeonToolBlockEntity>> type;
    private final MapCodec<DungeonToolBlock> codec;

    public DungeonToolBlock(Properties properties, Supplier<? extends BlockEntityType<? extends DungeonToolBlockEntity>> type) {
        super(properties);
        this.type = type;
        this.codec = simpleCodec(p -> new DungeonToolBlock(p, type));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return type.get().create(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && TGDebug.check(player)
                && level.getBlockEntity(pos) instanceof DungeonToolBlockEntity tool) {
            var templates = DungeonDebug.templateNames((ServerLevel) level);
            serverPlayer.openMenu(tool, buf -> {
                buf.writeBlockPos(pos);
                buf.writeCollection(templates, FriendlyByteBuf::writeUtf);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
