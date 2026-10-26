package techguns.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import techguns.core.damage.DeathType;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.damage.TGExplosion;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;
import techguns.registry.TGSounds;

/**
 * Bio blob left by the bio gun (Fabric {@code BlockBioBlob}): a toxic blob stuck to a block face that shrinks over time,
 * disappears at size 0 and bursts into a poison explosion when stepped on. Size 0..2, facing = the face it sits on.
 */
public class BioBlobBlock extends Block {
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 2);
    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    /** Radius (blocks) within which players see the burst FX. */
    private static final double BURST_FX_RADIUS = 64.0;

    // horizontal inset and height (in 1/16 blocks) of the box per size
    private static final int[] WIDTH_INSET = {5, 3, 1};
    private static final int[] HEIGHT = {4, 4, 5};

    private static final VoxelShape[][] SHAPES = new VoxelShape[6][3];

    static {
        for (Direction facing : Direction.values()) {
            for (int size = 0; size < 3; size++) {
                SHAPES[facing.ordinal()][size] = shapeFor(WIDTH_INSET[size], HEIGHT[size], facing);
            }
        }
    }

    public BioBlobBlock(Properties properties) {
        super(properties.pushReaction(PushReaction.DESTROY));
        registerDefaultState(stateDefinition.any().setValue(SIZE, 0).setValue(FACING, Direction.DOWN));
    }

    private static VoxelShape shapeFor(int w, int h, Direction facing) {
        final double u = 0.0625;
        return switch (facing) {
            case WEST -> Shapes.box(0, u * w, u * w, u * h, u * (16 - w), u * (16 - w));
            case NORTH -> Shapes.box(u * w, u * w, 0, u * (16 - w), u * (16 - w), u * h);
            case SOUTH -> Shapes.box(u * w, u * w, u * (16 - h), u * (16 - w), u * (16 - w), 1);
            case UP -> Shapes.box(u * w, u * (16 - h), u * w, u * (16 - w), 1, u * (16 - w));
            case EAST -> Shapes.box(u * (16 - h), u * w, u * w, 1, u * (16 - w), u * (16 - w));
            default -> Shapes.box(u * w, 0, u * w, u * (16 - w), u * h, u * (16 - w));
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SIZE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).ordinal()][state.getValue(SIZE)];
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int size = state.getValue(SIZE);
        if (size > 0) {
            level.setBlockAndUpdate(pos, state.setValue(SIZE, size - 1));
            scheduleShrink(level, pos, random);
        } else {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    /** Schedules the next shrink step (4 to 6 seconds). */
    public void scheduleShrink(Level level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, Mth.nextInt(random, 80, 120), TickPriority.LOW);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        scheduleShrink(level, pos, level.random);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level instanceof ServerLevel server) {
            burst(server, pos, state.getValue(SIZE) + 1, entity);
        }
    }

    /** Stepped on: the blob bursts into a poison explosion that grows with its size. */
    private static void burst(ServerLevel level, BlockPos pos, int power, Entity trigger) {
        float radius = power;
        explodeBlob(level, pos, trigger, 10 * radius, 0.5 * radius, radius, radius * 1.5f);
    }

    /** Removes the blob and detonates it: poison damage, bio death FX. */
    private static void explodeBlob(ServerLevel level, BlockPos pos, @Nullable Entity attacker, double damageMax, double damageMin,
                                    double radius1, double radius2) {
        Vec3 center = Vec3.atCenterOf(pos);
        TGDamageSource source = TGDamage.poison(level, null, attacker, DeathType.BIO).setGoreChance(1.0f).setArmorPenetration(0.35f);
        TGExplosion explosion = new TGExplosion(level, attacker, null, center, damageMax, damageMin, radius1, radius2, 0.0);
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        explosion.explode(source, false);
        level.playSound(null, pos, TGSounds.DEATH_BIO.get(), SoundSource.BLOCKS, 4.0f, 1.0f);
        TGNetwork.sendFxNear(level, center.x, center.y, center.z, BURST_FX_RADIUS,
                new FxPayloads.SpawnFx("bioblobExplosion", center.x, center.y, center.z));
    }

    /** Bio gun hit on an existing blob: it grows by {@code growth}, or bursts when it would exceed size 2. */
    public void hitByBioShot(ServerLevel level, BlockPos pos, BlockState state, int growth, @Nullable Entity shooter) {
        int newSize = state.getValue(SIZE) + growth;
        if (newSize <= 2) {
            level.setBlockAndUpdate(pos, state.setValue(SIZE, newSize));
            scheduleShrink(level, pos, level.random);
        } else {
            explodeBlob(level, pos, shooter, 30, 15, 3.0, 4.5);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState state = defaultBlockState().setValue(FACING, direction);
            if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
                return state;
            }
        }
        return null;
    }
}
