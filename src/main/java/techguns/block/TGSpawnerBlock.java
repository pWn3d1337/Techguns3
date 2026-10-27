package techguns.block;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import techguns.entity.npc.TGNpc;
import techguns.entity.spawn.TGSpawnerBlockEntity;
import techguns.registry.TGBlockEntities;

/**
 * Techguns 2 {@code BlockTGSpawner} ({@code tg_spawner} hole / soldier_spawn): an unbreakable floor plate without
 * collision that spawns NPCs (its block entity). Placed by hand it spawns its default NPC; structures configure it.
 */
public class TGSpawnerBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 2, 14);

    private final MapCodec<TGSpawnerBlock> codec;
    private final Supplier<? extends EntityType<? extends TGNpc>> defaultMob;

    public TGSpawnerBlock(Properties properties, Supplier<? extends EntityType<? extends TGNpc>> defaultMob) {
        super(properties);
        this.defaultMob = defaultMob;
        this.codec = simpleCodec(p -> new TGSpawnerBlock(p, defaultMob));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TGSpawnerBlockEntity spawner && !spawner.hasMobTypes()) {
            spawner.addMobType(defaultMob.get(), 1);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TGSpawnerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TGBlockEntities.TG_SPAWNER.get(), TGSpawnerBlockEntity::serverTick);
    }
}
