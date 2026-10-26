package techguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import techguns.registry.TGSounds;

/**
 * The 3x3 tech and hangar doors (Techguns 2 {@code BlockTGDoor3x3}). One door is nine blocks of the same door block;
 * every block stores its column (along the door plane) and row, the centre block (column 1, row 1) is the master that
 * renders the whole door model, the other eight render nothing. Right-clicking any part or a redstone signal on any part
 * opens/closes the whole door; an unpowered open door closes again after five seconds. Breaking any part removes the whole
 * door and drops one door item (not for creative players).
 * <p>
 * Simplified vs 1.12: the open/close animation is not ported (the door switches between its closed and open model at
 * once), and the door block entity with its GUI (Manual / Redstone / Player Detector mode, auto close toggle, owner) is
 * not ported yet, so a door is always manual (use) and redstone controlled with auto close on.
 */
public class Door3x3Block extends Block {
    /** The four door kinds: the bi-parting tech door, the nether tech door, and the hangar doors that retract up or down. */
    public enum Kind {
        METAL, HANGAR_UP, HANGAR_DOWN, NETHER;

        public boolean isHangar() {
            return this == HANGAR_UP || this == HANGAR_DOWN;
        }
    }

    /** True when the door plane is the Z axis (the door spans north-south, facing east-west). */
    public static final BooleanProperty ZPLANE = BooleanProperty.create("zplane");
    /** Position along the door plane, 0..2; 1 is the centre. Lower coordinates first. */
    public static final IntegerProperty COL = IntegerProperty.create("col", 0, 2);
    /** Position from the bottom, 0..2. */
    public static final IntegerProperty ROW = IntegerProperty.create("row", 0, 2);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    /** Redstone state of the whole door (like the vanilla door): redstone only acts on a change of power. */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    /** Ticks until an unpowered open door closes again (5000 ms in 1.12). */
    public static final int AUTO_CLOSE_TICKS = 100;

    // thickness: size = 6/16 from both sides (4/16 thick), size2 = 4/16 for the open frame strips
    private static final VoxelShape CLOSED_X = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape CLOSED_Z = Block.box(6, 0, 0, 10, 16, 16);
    private static final VoxelShape TOP_X = Block.box(0, 12, 6, 16, 16, 10);
    private static final VoxelShape TOP_Z = Block.box(6, 12, 0, 10, 16, 16);
    private static final VoxelShape BOTTOM_X = Block.box(0, 0, 6, 16, 4, 10);
    private static final VoxelShape BOTTOM_Z = Block.box(6, 0, 0, 10, 4, 16);
    private static final VoxelShape LEFT_X = Block.box(0, 0, 6, 4, 16, 10);
    private static final VoxelShape RIGHT_X = Block.box(12, 0, 6, 16, 16, 10);
    private static final VoxelShape LEFT_Z = Block.box(6, 0, 0, 10, 16, 4);
    private static final VoxelShape RIGHT_Z = Block.box(6, 0, 12, 10, 16, 16);

    private final Kind kind;

    public Door3x3Block(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(ZPLANE, false).setValue(COL, 1).setValue(ROW, 1)
                .setValue(OPEN, false).setValue(POWERED, false));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ZPLANE, COL, ROW, OPEN, POWERED);
    }

    // --- geometry of the nine parts ---

    public static boolean isMaster(BlockState state) {
        return state.getValue(COL) == 1 && state.getValue(ROW) == 1;
    }

    /** Direction along the door plane in which the column index grows. */
    private static Direction planeDirection(boolean zplane) {
        return zplane ? Direction.SOUTH : Direction.EAST;
    }

    /** Position of the part in column {@code col}, row {@code row} of the door whose master block is at {@code master}. */
    public static BlockPos partPos(BlockPos master, boolean zplane, int col, int row) {
        return master.relative(planeDirection(zplane), col - 1).above(row - 1);
    }

    public static BlockPos masterPos(BlockState state, BlockPos pos) {
        return pos.relative(planeDirection(state.getValue(ZPLANE)), 1 - state.getValue(COL)).above(1 - state.getValue(ROW));
    }

    /** The door state of one part of a closed, unpowered door. */
    public BlockState partState(boolean zplane, int col, int row) {
        return defaultBlockState().setValue(ZPLANE, zplane).setValue(COL, col).setValue(ROW, row);
    }

    /** Whether the nine blocks at {@code master} (centre) are all replaceable. */
    public static boolean isFree(Level level, BlockPos master, boolean zplane) {
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                BlockPos pos = partPos(master, zplane, col, row);
                if (!level.isInWorldBounds(pos) || !level.getBlockState(pos).canBeReplaced()) {
                    return false;
                }
            }
        }
        return true;
    }

    // --- shapes ---

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean z = state.getValue(ZPLANE);
        if (!state.getValue(OPEN)) {
            return z ? CLOSED_Z : CLOSED_X;
        }
        int col = state.getValue(COL);
        int row = state.getValue(ROW);
        switch (kind) {
            case HANGAR_UP:
                return row == 2 ? (z ? TOP_Z : TOP_X) : Shapes.empty();
            case HANGAR_DOWN:
                return row == 0 ? (z ? BOTTOM_Z : BOTTOM_X) : Shapes.empty();
            default:
                if (row == 2) {
                    return z ? TOP_Z : TOP_X;
                }
                if (col == 0) {
                    return z ? LEFT_Z : LEFT_X;
                }
                if (col == 2) {
                    return z ? RIGHT_Z : RIGHT_X;
                }
                return Shapes.empty();
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(OPEN)) {
            return Shapes.empty();
        }
        return state.getValue(ZPLANE) ? CLOSED_Z : CLOSED_X;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return type != PathComputationType.WATER && state.getValue(OPEN);
    }

    // --- opening and closing ---

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockPos master = masterPos(state, pos);
            BlockState masterState = level.getBlockState(master);
            if (masterState.is(this)) {
                setOpen(level, master, masterState, !masterState.getValue(OPEN), masterState.getValue(POWERED), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private SoundEvent sound(boolean opening) {
        return (opening ? TGSounds.TECHDOOR_OPEN : TGSounds.TECHDOOR_CLOSE).get();
    }

    /**
     * Sets the open and powered state of the whole door and plays the open/close sound. Hangar doors also move the hangar
     * doors next to and above/below them that were in the same state, so a row of hangar doors opens together.
     */
    public void setOpen(Level level, BlockPos master, BlockState masterState, boolean open, boolean powered, boolean syncHangarNeighbors) {
        boolean zplane = masterState.getValue(ZPLANE);
        boolean changed = masterState.getValue(OPEN) != open;
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                BlockPos pos = partPos(master, zplane, col, row);
                BlockState part = level.getBlockState(pos);
                if (part.is(this)) {
                    // flags: clients only; the parts do not need neighbour updates and must not re-trigger the redstone check
                    level.setBlock(pos, part.setValue(OPEN, open).setValue(POWERED, powered), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
        if (changed) {
            level.playSound(null, master, sound(open), SoundSource.BLOCKS, (float) (0.9 + level.random.nextDouble() * 0.2),
                    (float) (0.9 + level.random.nextDouble() * 0.2));
            if (open && !powered) {
                level.scheduleTick(master, this, AUTO_CLOSE_TICKS);
            }
            if (syncHangarNeighbors && kind.isHangar()) {
                syncHangarNeighbors(level, master, zplane, open);
            }
        }
    }

    /** 1.12 {@code checkOpenNeighbours}: hangar doors sideways (up to three) and above/below follow this door. */
    private void syncHangarNeighbors(Level level, BlockPos master, boolean zplane, boolean open) {
        syncRow(level, master, zplane, open);
        BlockPos other = master.above(kind == Kind.HANGAR_UP ? -3 : 3);
        if (syncNeighbor(level, other, zplane, open)) {
            syncRow(level, other, zplane, open);
        }
    }

    private void syncRow(Level level, BlockPos master, boolean zplane, boolean open) {
        Direction plane = planeDirection(zplane);
        for (Direction direction : new Direction[]{plane.getOpposite(), plane}) {
            for (int distance = 3; distance <= 9; distance += 3) {
                if (!syncNeighbor(level, master.relative(direction, distance), zplane, open)) {
                    break;
                }
            }
        }
    }

    /** Moves a hangar door master at {@code pos} that is still in the old state; false when there is no such door. */
    private boolean syncNeighbor(Level level, BlockPos pos, boolean zplane, boolean open) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof Door3x3Block door && door.kind.isHangar() && isMaster(state) && state.getValue(ZPLANE) == zplane
                && state.getValue(OPEN) != open) {
            door.setOpen(level, pos, state, open, state.getValue(POWERED), false);
            return true;
        }
        return false;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isMaster(state) && state.getValue(OPEN) && !state.getValue(POWERED)) {
            setOpen(level, pos, state, false, false, true);
        }
    }

    // --- redstone ---

    private boolean isPowered(Level level, BlockPos master, boolean zplane) {
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                if (level.hasNeighborSignal(partPos(master, zplane, col, row))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Opens or closes the door when the redstone power of any of its blocks changed. */
    public void updatePower(Level level, BlockPos master) {
        BlockState masterState = level.getBlockState(master);
        if (!masterState.is(this)) {
            return;
        }
        boolean powered = isPowered(level, master, masterState.getValue(ZPLANE));
        if (powered != masterState.getValue(POWERED)) {
            setOpen(level, master, masterState, powered, powered, true);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            updatePower(level, masterPos(state, pos));
        }
    }

    // --- breaking ---

    /** Breaking any part drops the door item once (also with the wrong tool), like in 1.12. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative() && !isMaster(state)) {
            BlockPos master = masterPos(state, pos);
            BlockState masterState = level.getBlockState(master);
            if (masterState.is(this)) {
                dropResources(masterState, level, master, null, player, player.getMainHandItem());
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** Removing any part (mining, explosion, ...) removes the whole door without further drops. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            boolean zplane = state.getValue(ZPLANE);
            BlockPos master = masterPos(state, pos);
            for (int col = 0; col < 3; col++) {
                for (int row = 0; row < 3; row++) {
                    BlockPos other = partPos(master, zplane, col, row);
                    if (!other.equals(pos)) {
                        BlockState otherState = level.getBlockState(other);
                        if (otherState.is(this) && otherState.getValue(ZPLANE) == zplane && masterPos(otherState, other).equals(master)) {
                            level.destroyBlock(other, false);
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        // the door plane turns with the block; the column order flips when the growing direction would become negative
        Direction turned = rotation.rotate(planeDirection(state.getValue(ZPLANE)));
        boolean zplane = turned.getAxis() == Direction.Axis.Z;
        state = state.setValue(ZPLANE, zplane);
        return turned == planeDirection(zplane) ? state : state.setValue(COL, 2 - state.getValue(COL));
    }
}
