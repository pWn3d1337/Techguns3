package techguns.block;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import techguns.registry.TGBlockEntities;
import techguns.registry.TGSounds;

/**
 * The 3x3 tech and hangar doors (Techguns 2 {@code BlockTGDoor3x3}). One door is nine blocks of the same door block;
 * every block stores its column (along the door plane) and row, the centre block (column 1, row 1) is the master that
 * renders the whole door model, the other eight render nothing. Breaking any part removes the whole door and drops one
 * door item (not for creative players).
 * <p>
 * Who opens it is a setting of {@link Door3x3BlockEntity} (sneak + use with a wrench opens the door screen): manual
 * doors open and close when used (and close again five seconds after opening, unless auto close is off), redstone doors
 * follow a signal at any of their blocks, player detector doors open for players near them. The owner's security
 * setting decides who may do that.
 * <p>
 * Opening and closing take {@link #MOVE_TICKS} like the 1.12 {@code OPENING} / {@code CLOSING} states: the door is
 * {@link #MOVING} with {@link #OPEN} already set to where it goes (an opening door lets entities through at once, a
 * closing one blocks them at once, as in 1.12). Meanwhile the master shows only the frame and
 * {@code Door3x3Renderer} draws the moving door wings or segments; {@link Door3x3BlockEntity} times the movement. Using
 * the door or a change of power while it moves turns it around.
 */
public class Door3x3Block extends Block implements EntityBlock {
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
    /** The door is opening ({@link #OPEN}) or closing (not open). */
    public static final BooleanProperty MOVING = BooleanProperty.create("moving");

    /** Ticks an opening or closing door moves (DOOR_OPEN_TIME 1000 ms in 1.12). */
    public static final int MOVE_TICKS = 20;
    /** Ticks until a fully open manual door closes again (5000 ms in 1.12). */
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
                .setValue(OPEN, false).setValue(POWERED, false).setValue(MOVING, false));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ZPLANE, COL, ROW, OPEN, POWERED, MOVING);
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

    /** Manual doors open and close when an allowed player uses them (Techguns 2 {@code onBlockActivated}). */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockPos master = masterPos(state, pos);
            BlockState masterState = level.getBlockState(master);
            if (masterState.is(this) && level.getBlockEntity(master) instanceof Door3x3BlockEntity door) {
                if (!door.mayAccess(player)) {
                    denied(player, door);
                } else if (door.getMode() == Door3x3BlockEntity.Mode.MANUAL) {
                    setOpen(level, master, masterState, !masterState.getValue(OPEN), masterState.getValue(POWERED), true);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Opens the door screen for {@code player} (sneak + use with a wrench, Techguns 2 {@code TechgunsGuiHandler}) if
     * the security setting allows it.
     */
    public void openSettings(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        BlockPos master = masterPos(state, pos);
        if (level.getBlockEntity(master) instanceof Door3x3BlockEntity door) {
            if (door.mayAccess(player)) {
                serverPlayer.openMenu(door, buf -> buf.writeBlockPos(master));
            } else {
                denied(player, door);
            }
        }
    }

    private static void denied(Player player, Door3x3BlockEntity door) {
        player.displayClientMessage(Component.translatable("techguns.container.security.denied", door.getDisplayName())
                .withStyle(ChatFormatting.RED), true);
    }

    private SoundEvent sound(boolean opening) {
        return (opening ? TGSounds.TECHDOOR_OPEN : TGSounds.TECHDOOR_CLOSE).get();
    }

    /**
     * Sets the open and powered state of the whole door; a change of the open state starts the movement (or turns it
     * around) and plays the open/close sound. Hangar doors also move the hangar doors next to and above/below them that
     * were in the same state, so a row of hangar doors opens together.
     */
    public void setOpen(Level level, BlockPos master, BlockState masterState, boolean open, boolean powered, boolean syncHangarNeighbors) {
        boolean zplane = masterState.getValue(ZPLANE);
        boolean changed = masterState.getValue(OPEN) != open;
        setParts(level, master, zplane, open, powered, changed || masterState.getValue(MOVING));
        if (changed) {
            playSound(level, master, sound(open));
            if (syncHangarNeighbors && kind.isHangar()) {
                syncHangarNeighbors(level, master, zplane, open);
            }
        }
    }

    /** The end of the movement (1.12 {@code OPENING} to {@code OPENED}, {@code CLOSING} to {@code CLOSED}). */
    public void finishMoving(Level level, BlockPos master, BlockState masterState) {
        setParts(level, master, masterState.getValue(ZPLANE), masterState.getValue(OPEN), masterState.getValue(POWERED), false);
        playSound(level, master, TGSounds.TECHDOOR_STATE_FINISHED.get());
    }

    private void setParts(Level level, BlockPos master, boolean zplane, boolean open, boolean powered, boolean moving) {
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                BlockPos pos = partPos(master, zplane, col, row);
                BlockState part = level.getBlockState(pos);
                if (part.is(this)) {
                    // flags: clients only; the parts do not need neighbour updates and must not re-trigger the redstone check
                    level.setBlock(pos, part.setValue(OPEN, open).setValue(POWERED, powered).setValue(MOVING, moving),
                            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
    }

    private static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, (float) (0.9 + level.random.nextDouble() * 0.2),
                (float) (0.9 + level.random.nextDouble() * 0.2));
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

    // --- block entity: timing of the movement and the auto close ---

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isMaster(state) ? new Door3x3BlockEntity(pos, state) : null;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || type != TGBlockEntities.DOOR_3X3.get() ? null
                : (BlockEntityTicker<T>) (BlockEntityTicker<Door3x3BlockEntity>) Door3x3BlockEntity::serverTick;
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

    /**
     * Notes the redstone power of the door's blocks; a redstone door opens or closes as its redstone behaviour says
     * (Techguns 2 {@code updateRedstoneState}).
     */
    public void updatePower(Level level, BlockPos master) {
        BlockState masterState = level.getBlockState(master);
        if (!masterState.is(this) || !(level.getBlockEntity(master) instanceof Door3x3BlockEntity door)) {
            return;
        }
        boolean powered = isPowered(level, master, masterState.getValue(ZPLANE));
        Boolean wantsOpen = door.wantsOpen(powered);
        boolean open = wantsOpen != null ? wantsOpen : masterState.getValue(OPEN);
        if (open != masterState.getValue(OPEN) || powered != masterState.getValue(POWERED)) {
            setOpen(level, master, masterState, open, powered, true);
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
