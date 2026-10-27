package techguns.machine;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.mojang.authlib.GameProfile;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import techguns.TGConfig;
import techguns.block.ExplosiveChargeBlock;
import techguns.network.TGNetwork;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGSounds;

/**
 * Mining charge (Techguns 2 {@code ExplosiveChargeTileEnt} and {@code ExplosiveChargeAdvTileEnt}): radius, length and
 * fuse are set in the menu, arming starts the fuse (a tick sound every second). The blast removes, with drops, every
 * block of a radius x radius cross-section and up to length blocks deep into the wall, each line stopping at the first
 * block too hard to break. Blocks break in the owner's name (block protection applies).
 */
public class ExplosiveChargeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int BUTTON_ARM = 0;
    public static final int BUTTON_RADIUS_UP = 1;
    public static final int BUTTON_RADIUS_DOWN = 2;
    public static final int BUTTON_LENGTH_UP = 3;
    public static final int BUTTON_LENGTH_DOWN = 4;
    public static final int BUTTON_FUSE_UP = 5;
    public static final int BUTTON_FUSE_DOWN = 6;

    public static final int DATA_RADIUS = 0;
    public static final int DATA_LENGTH = 1;
    public static final int DATA_FUSE = 2;
    public static final int DATA_COUNT = 3;

    public static final int MIN_FUSE = 60;
    public static final int MAX_FUSE = 200;
    public static final int FUSE_STEP = 20;
    private static final double USE_RANGE_SQUARED = 64.0;
    private static final double FX_RANGE = 50.0;

    /** Techguns 2 charge types: blast limits, sounds and the hardest block they break. */
    public enum Kind {
        TNT(3, 3, 3, 2, 5, 3, TGSounds.TNT_PLANT, TGSounds.TNT_TICK, TGSounds.TNT_INIT),
        ADVANCED(3, 7, 5, 2, 10, 10, TGSounds.C4_PLANT, TGSounds.C4_TICK, TGSounds.C4_INIT);

        public final int minRadius;
        public final int maxRadius;
        public final int defaultRadius;
        public final int minLength;
        public final int maxLength;
        public final int defaultLength;
        private final Supplier<SoundEvent> plant;
        private final Supplier<SoundEvent> tick;
        private final Supplier<SoundEvent> init;

        Kind(int minRadius, int maxRadius, int defaultRadius, int minLength, int maxLength, int defaultLength, Supplier<SoundEvent> plant,
             Supplier<SoundEvent> tick, Supplier<SoundEvent> init) {
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.defaultRadius = defaultRadius;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.defaultLength = defaultLength;
            this.plant = plant;
            this.tick = tick;
            this.init = init;
        }

        public SoundEvent plantSound() {
            return plant.get();
        }

        /** Techguns 2 {@code canBreakBlockHardness}: normal charges below, advanced up to their config hardness. */
        public boolean canBreak(float hardness) {
            return hardness >= 0 && (this == TNT ? hardness < TGConfig.EXPLOSIVE_CHARGE_MAX_HARDNESS.get()
                    : hardness <= TGConfig.EXPLOSIVE_CHARGE_ADVANCED_MAX_HARDNESS.get());
        }
    }

    private int radius;
    private int length;
    private int fuse = 100;
    private boolean armed;
    @Nullable
    private UUID owner;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_RADIUS -> radius;
                case DATA_LENGTH -> length;
                case DATA_FUSE -> fuse;
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

    public ExplosiveChargeBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.EXPLOSIVE_CHARGE.get(), pos, state);
        Kind kind = kind(state);
        radius = kind.defaultRadius;
        length = kind.defaultLength;
    }

    private static Kind kind(BlockState state) {
        return state.getBlock() instanceof ExplosiveChargeBlock block ? block.kind() : Kind.TNT;
    }

    public Kind kind() {
        return kind(getBlockState());
    }

    public void setOwner(Player player) {
        owner = player.getUUID();
        setChanged();
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public int getRadius() {
        return radius;
    }

    public int getLength() {
        return length;
    }

    public int getFuse() {
        return fuse;
    }

    public boolean isArmed() {
        return armed;
    }

    public ContainerData getData() {
        return data;
    }

    public boolean isUsableBy(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= USE_RANGE_SQUARED;
    }

    /** Techguns 2 {@code buttonClicked}: settings until armed; arming starts the fuse. */
    public boolean pressButton(Player player, int id) {
        if (armed || !isUsableBy(player)) {
            return false;
        }
        Kind kind = kind();
        switch (id) {
            case BUTTON_ARM -> arm();
            case BUTTON_RADIUS_UP -> radius = Math.min(kind.maxRadius, radius + 1);
            case BUTTON_RADIUS_DOWN -> radius = Math.max(kind.minRadius, radius - 1);
            case BUTTON_LENGTH_UP -> length = Math.min(kind.maxLength, length + 1);
            case BUTTON_LENGTH_DOWN -> length = Math.max(kind.minLength, length - 1);
            case BUTTON_FUSE_UP -> fuse = Math.min(MAX_FUSE, fuse + FUSE_STEP);
            case BUTTON_FUSE_DOWN -> fuse = Math.max(MIN_FUSE, fuse - FUSE_STEP);
            default -> {
                return false;
            }
        }
        setChanged();
        return true;
    }

    public void arm() {
        if (armed || level == null) {
            return;
        }
        armed = true;
        level.playSound(null, worldPosition, kind().init.get(), SoundSource.BLOCKS, 4.0f, 1.0f);
        level.setBlock(worldPosition, getBlockState().setValue(ExplosiveChargeBlock.ARMED, true), Block.UPDATE_ALL);
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExplosiveChargeBlockEntity charge) {
        if (!charge.armed) {
            return;
        }
        charge.fuse--;
        if (charge.fuse > 0 && charge.fuse % 20 == 0) {
            level.playSound(null, pos, charge.kind().tick.get(), SoundSource.BLOCKS, 4.0f, 1.0f);
        }
        if (charge.fuse <= 0) {
            charge.explode((ServerLevel) level);
        }
    }

    /** Techguns 2 {@code explode}: one line into the wall for every position of the cross-section. */
    private void explode(ServerLevel level) {
        Kind kind = kind();
        Direction facing = getBlockState().getValue(ExplosiveChargeBlock.FACING);
        BlockPos pos = worldPosition;
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0f,
                (1.0f + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f) * 0.7f);
        level.removeBlock(pos, false);
        Player player = breaker(level);
        Direction side1 = facing.getAxis() == Direction.Axis.Z ? Direction.EAST : Direction.NORTH;
        Direction side2 = facing.getAxis() == Direction.Axis.Y ? Direction.EAST : Direction.UP;
        int half = radius / 2;
        for (int a = -half; a <= half; a++) {
            for (int b = -half; b <= half; b++) {
                BlockPos.MutableBlockPos line = pos.relative(side1, a).relative(side2, b).mutable();
                for (int i = 0; i < length; i++) {
                    line.move(facing);
                    if (!explodeBlock(level, line, kind, player)) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Who breaks the blocks (protection mods see this player): the owner if online, else a fake player with the
     * owner's id. Techguns 2 broke nothing while the owner was offline.
     */
    private Player breaker(ServerLevel level) {
        Player online = owner == null ? null : level.getPlayerByUUID(owner);
        if (online != null) {
            return online;
        }
        return owner == null ? FakePlayerFactory.getMinecraft(level) : FakePlayerFactory.get(level, new GameProfile(owner, "[Techguns]"));
    }

    /** Breaks the block with drops if the charge and the owner may; false when the line is blocked. */
    private static boolean explodeBlock(ServerLevel level, BlockPos pos, Kind kind, Player player) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        if (!kind.canBreak(state.getDestroySpeed(level, pos)) || NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled()) {
            return false;
        }
        Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, ItemStack.EMPTY);
        level.removeBlock(pos, false);
        if (level.random.nextFloat() > 0.5f) {
            TGNetwork.sendFxNear(level, Vec3.atCenterOf(pos), FX_RANGE, "MiningChargeBlockExplosion");
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ExplosiveChargeMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("radius", (short) radius);
        tag.putShort("length", (short) length);
        tag.putShort("fuse", (short) fuse);
        tag.putBoolean("armed", armed);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("radius")) {
            radius = tag.getShort("radius");
            length = tag.getShort("length");
            fuse = tag.getShort("fuse");
        }
        armed = tag.getBoolean("armed");
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
