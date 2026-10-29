package techguns.block;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import techguns.compat.team.TGTeams;
import techguns.machine.RedstoneMode;
import techguns.registry.TGBlockEntities;

/**
 * Techguns 2 {@code Door3x3TileEntity}, on the master block of a 3x3 door.
 * <p>
 * Movement: the game tick the current movement started at, for the animation on the client and for the end of the
 * movement and the auto close on the server. Both sides take the start from the master's block state changes, so the
 * timing needs no packet of its own.
 * <p>
 * Settings (the door screen, sneak + use with a wrench): the {@link Mode} (Techguns 2 {@code doormode}), auto close of
 * manual doors, the redstone behaviour of redstone doors, and owner and security like the machines.
 */
public class Door3x3BlockEntity extends BlockEntity implements MenuProvider {
    /** Who opens the door (Techguns 2 {@code doormode} 0, 1, 2). */
    public enum Mode {
        /** Opened and closed by using it (players allowed by the security setting). */
        MANUAL,
        /** Follows a redstone signal at any of its blocks, as set by its {@link RedstoneMode}. */
        REDSTONE,
        /** Opens for allowed players near it and closes a second after they left. */
        PLAYER_DETECTOR;

        public Mode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public String translationKey() {
            return "techguns.container.door3x3.mode." + name().toLowerCase(Locale.ROOT);
        }

        public static Mode byId(int id) {
            return id >= 0 && id < values().length ? values()[id] : MANUAL;
        }
    }

    public static final int DATA_MODE = 0;
    public static final int DATA_AUTO_CLOSE = 1;
    public static final int DATA_REDSTONE = 2;
    public static final int DATA_SECURITY = 3;
    public static final int DATA_SIGNAL = 4;
    public static final int DATA_COUNT = 5;

    /** Menu button ids ({@link #pressButton}); redstone and security as for the machines. */
    public static final int BUTTON_REDSTONE = 0;
    public static final int BUTTON_SECURITY = 1;
    public static final int BUTTON_MODE = 2;
    public static final int BUTTON_AUTO_CLOSE = 3;

    /** Ticks between the player detector checks (Techguns 2 {@code BLOCK_UPDATE_DELAY}). */
    public static final int DETECTOR_INTERVAL = 5;
    /** Use range measured from the master (the door is three blocks wide). */
    private static final double USE_RANGE_SQUARED = 81.0;

    private long moveStart;
    private Mode mode = Mode.MANUAL;
    private boolean autoClose = true;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    @Nullable
    private UUID owner;
    private String ownerName = "";
    private int security = TGTeams.SECURITY_PUBLIC;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_MODE -> mode.ordinal();
                case DATA_AUTO_CLOSE -> autoClose ? 1 : 0;
                case DATA_REDSTONE -> redstoneMode.ordinal();
                case DATA_SECURITY -> security;
                case DATA_SIGNAL -> getBlockState().getValue(Door3x3Block.POWERED) ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public Door3x3BlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.DOOR_3X3.get(), pos, state);
    }

    // --- movement ---

    /** Game ticks since the current (or last) movement started. */
    public long elapsed(Level level) {
        return level.getGameTime() - moveStart;
    }

    /** How far the door is open, 0 (closed) to 1 (open), between game ticks. */
    public float openness(float partialTick) {
        BlockState state = getBlockState();
        if (level == null || !state.getValue(Door3x3Block.MOVING)) {
            return state.getValue(Door3x3Block.OPEN) ? 1.0f : 0.0f;
        }
        float progress = Mth.clamp((elapsed(level) + partialTick) / Door3x3Block.MOVE_TICKS, 0.0f, 1.0f);
        return state.getValue(Door3x3Block.OPEN) ? progress : 1.0f - progress;
    }

    /**
     * Starts the movement timer when the door starts moving or turns around. Turning around mid-way keeps the door
     * where it is: the new movement starts as far along as the old one still had to go.
     */
    // MIGRATION(26.x): BlockEntity#setBlockState is deprecated for callers; check it is still called on state changes.
    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(BlockState state) {
        BlockState old = getBlockState();
        super.setBlockState(state);
        if (level == null || !old.is(state.getBlock()) || !state.getValue(Door3x3Block.MOVING)) {
            return;
        }
        boolean wasMoving = old.getValue(Door3x3Block.MOVING);
        if (!wasMoving) {
            moveStart = level.getGameTime();
        } else if (old.getValue(Door3x3Block.OPEN) != state.getValue(Door3x3Block.OPEN)) {
            long done = Mth.clamp(elapsed(level), 0, Door3x3Block.MOVE_TICKS);
            moveStart = level.getGameTime() - (Door3x3Block.MOVE_TICKS - done);
        }
    }

    /**
     * Ends the movement, closes a fully open manual door after {@link Door3x3Block#AUTO_CLOSE_TICKS} and runs the player
     * detector (Techguns 2 {@code updateTick}).
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, Door3x3BlockEntity door) {
        if (!(state.getBlock() instanceof Door3x3Block block)) {
            return;
        }
        long elapsed = door.elapsed(level);
        boolean open = state.getValue(Door3x3Block.OPEN);
        boolean moving = state.getValue(Door3x3Block.MOVING);
        if (moving && elapsed >= Door3x3Block.MOVE_TICKS) {
            block.finishMoving(level, pos, state);
            return;
        }
        switch (door.mode) {
            case MANUAL -> {
                if (door.autoClose && open && !moving && elapsed >= Door3x3Block.MOVE_TICKS + Door3x3Block.AUTO_CLOSE_TICKS) {
                    block.setOpen(level, pos, state, false, state.getValue(Door3x3Block.POWERED), true);
                }
            }
            case PLAYER_DETECTOR -> {
                if (level.getGameTime() % DETECTOR_INTERVAL != 0) {
                    return;
                }
                boolean playerNear = door.allowedPlayerNear(level);
                if (playerNear && !open) {
                    block.setOpen(level, pos, state, true, state.getValue(Door3x3Block.POWERED), true);
                } else if (!playerNear && open && !moving && elapsed >= 2L * Door3x3Block.MOVE_TICKS) {
                    // Techguns 2: closes once it has stood open for DOOR_OPEN_TIME
                    block.setOpen(level, pos, state, false, state.getValue(Door3x3Block.POWERED), true);
                }
            }
            case REDSTONE -> {
            }
        }
    }

    /** Techguns 2 {@code checkOpenForNearPlayers}: an allowed player within 2 blocks sideways, 1 up or down. */
    private boolean allowedPlayerNear(Level level) {
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(2.0, 1.0, 2.0),
                player -> !player.isSpectator() && mayAccess(player));
        return !players.isEmpty();
    }

    /**
     * Whether a redstone door should be open with {@code powered}; null when its redstone behaviour is "ignore" (the
     * door stays as it is, the screen warns about it like in Techguns 2).
     */
    @Nullable
    public Boolean wantsOpen(boolean powered) {
        if (mode != Mode.REDSTONE || redstoneMode == RedstoneMode.IGNORE) {
            return null;
        }
        return redstoneMode.allows(powered);
    }

    // --- access, settings, buttons ---

    public void setOwner(Player player) {
        owner = player.getUUID();
        ownerName = player.getGameProfile().getName();
        security = TGTeams.SECURITY_PUBLIC;
        sendUpdate();
    }

    /** Techguns 2 {@code isUseableByPlayer}: in range and allowed by the security setting. */
    public boolean isUsableBy(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this
                || player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) > USE_RANGE_SQUARED) {
            return false;
        }
        return mayAccess(player);
    }

    public boolean mayAccess(Player player) {
        return owner == null || TGTeams.canAccess(owner, player.getUUID(), security);
    }

    public boolean isOwner(Player player) {
        return owner != null && owner.equals(player.getUUID());
    }

    /** Handles a menu button; false if the player may not press it. */
    public boolean pressButton(Player player, int id) {
        if (!isUsableBy(player)) {
            return false;
        }
        switch (id) {
            case BUTTON_REDSTONE -> redstoneMode = redstoneMode.next();
            case BUTTON_SECURITY -> {
                if (!isOwner(player)) {
                    return false;
                }
                security = TGTeams.nextSecurity(security);
            }
            case BUTTON_MODE -> mode = mode.next();
            case BUTTON_AUTO_CLOSE -> autoClose = !autoClose;
            default -> {
                return false;
            }
        }
        sendUpdate();
        // a redstone door takes its new setting at once
        if (level != null && getBlockState().getBlock() instanceof Door3x3Block block) {
            block.updatePower(level, worldPosition);
        }
        return true;
    }

    private void sendUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public ContainerData getData() {
        return data;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public int getSecurity() {
        return security;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    // --- menu ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("techguns.container.door3x3");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new Door3x3Menu(id, inventory, this);
    }

    // --- saving and sync ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("move_start", moveStart);
        tag.putByte("mode", (byte) mode.ordinal());
        tag.putBoolean("auto_close", autoClose);
        tag.putByte("redstone", (byte) redstoneMode.ordinal());
        if (owner != null) {
            tag.putUUID("owner", owner);
            tag.putString("owner_name", ownerName);
        }
        tag.putByte("security", (byte) security);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        moveStart = tag.getLong("move_start");
        mode = Mode.byId(tag.getByte("mode"));
        autoClose = !tag.contains("auto_close") || tag.getBoolean("auto_close");
        redstoneMode = RedstoneMode.byId(tag.getByte("redstone"));
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        ownerName = tag.getString("owner_name");
        security = tag.getByte("security");
    }

    /** Clients that load the chunk mid-way see the door where it is; the screen shows the owner. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
