package techguns.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import techguns.TGConfig;
import techguns.compat.team.TGTeams;
import techguns.registry.TGItems;

/**
 * Base of the powered Techguns machines (Techguns 2 {@code BasicMachineTileEnt} and its parents): an inventory with
 * per-slot rules, an FE buffer, redstone mode, owner and security, and one running {@link MachineOperation} with its
 * progress. Subclasses decide what an operation is ({@link #checkAndStartOperation}) and where its results go
 * ({@link #finishOperation}).
 *
 * <p>Security: Techguns 2 levels public, allies, team and owner ({@link TGTeams}); without teams only public and owner.
 */
public abstract class TGMachineBlockEntity extends BlockEntity implements MenuProvider {
    /** {@link #data} indices synced to an open menu. */
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_TOTAL_TIME = 1;
    public static final int DATA_ENERGY_LOW = 2;
    public static final int DATA_ENERGY_HIGH = 3;
    public static final int DATA_REDSTONE = 4;
    public static final int DATA_SIGNAL = 5;
    public static final int DATA_SECURITY = 6;
    /** Progress and total time are synced as two 16 bit halves like the energy (ore drill jobs exceed a short). */
    public static final int DATA_PROGRESS_HIGH = 7;
    public static final int DATA_TOTAL_TIME_HIGH = 8;
    public static final int DATA_COUNT = 9;

    /** Menu button ids ({@link #pressButton}); machine buttons start at {@link #BUTTON_MACHINE}. */
    public static final int BUTTON_REDSTONE = 0;
    public static final int BUTTON_SECURITY = 1;
    public static final int BUTTON_MACHINE = 2;

    /** Techguns 2 use range (squared). */
    private static final double USE_RANGE_SQUARED = 64.0;

    protected final ItemStackHandler inventory;
    protected final MachineEnergy energy;
    private final IItemHandler automation;

    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    private boolean signal;
    @Nullable
    private UUID owner;
    private String ownerName = "";
    private int security = TGTeams.SECURITY_PUBLIC;

    @Nullable
    protected MachineOperation operation;
    protected int progress;
    protected int totalTime;
    /** Client: whether a job runs; the server knows {@link #operation}. */
    private boolean working;
    /** The job waits for power or a redstone signal; the client stops animating. */
    private boolean stalled;
    /** Something in the inventory or a setting changed: look for a new job (Techguns 2 {@code contentsChanged}). */
    protected boolean contentsChanged = true;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progress & 0xFFFF;
                case DATA_TOTAL_TIME -> totalTime & 0xFFFF;
                case DATA_PROGRESS_HIGH -> progress >>> 16;
                case DATA_TOTAL_TIME_HIGH -> totalTime >>> 16;
                case DATA_ENERGY_LOW -> energy.getEnergyStored() & 0xFFFF;
                case DATA_ENERGY_HIGH -> energy.getEnergyStored() >>> 16;
                case DATA_REDSTONE -> redstoneMode.ordinal();
                case DATA_SIGNAL -> signal ? 1 : 0;
                case DATA_SECURITY -> security;
                default -> getExtraData(index);
            };
        }

        @Override
        public void set(int index, int value) {
            // server-side values only
        }

        @Override
        public int getCount() {
            return DATA_COUNT + extraDataCount();
        }
    };

    protected TGMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots, int energyCapacity) {
        super(type, pos, state);
        this.inventory = new ItemStackHandler(slots) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot == upgradeSlot() ? stack.is(TGItems.MACHINE_UPGRADE_STACK.get()) : canPlace(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                contentsChanged = true;
                setChanged();
            }
        };
        this.energy = new MachineEnergy(energyCapacity, this::setChanged);
        this.automation = new AutomationHandler();
    }

    // --- machine specifics ---

    /** Whether {@code stack} may go into the (non-upgrade) {@code slot} by hand or automation. */
    protected abstract boolean canPlace(int slot, ItemStack stack);

    /** Whether automation (hoppers, pipes) may take from {@code slot}. */
    protected abstract boolean isOutputSlot(int slot);

    /** Slot of the machine upgrade (stack operation), or -1. */
    protected int upgradeSlot() {
        return -1;
    }

    /** Starts a job if the inputs allow it (Techguns 2 {@code checkAndStartOperation}). Server only. */
    protected abstract void checkAndStartOperation();

    /** Delivers the results of a finished job. Server only. */
    protected abstract void finishOperation(MachineOperation operation);

    /** Whether the machine runs at all (multiblocks only when formed, Techguns 2 {@code enabled}). */
    protected boolean canWork() {
        return true;
    }

    /** Server, a job runs: whether it may go on (Ore Drill: the drill head is unchanged); false cancels it. */
    protected boolean keepOperation() {
        return true;
    }

    /** Server, no job running and redstone allows work: per-tick work besides jobs; true skips looking for a job. */
    protected boolean idleTick() {
        return false;
    }

    /** A player used a held item on the machine; true if the machine handled it (fluid containers). Server only. */
    public boolean useItem(Player player, net.minecraft.world.InteractionHand hand) {
        return false;
    }

    /** Client guess for {@link #useItem}: whether the item could be used on the machine at all. */
    public boolean canUseItem(ItemStack stack) {
        return false;
    }

    /** Client, no job running: effects of work besides jobs. */
    protected void clientIdleTick() {
    }

    /** Client: work sounds while the job advances. */
    protected void playWorkSound() {
    }

    /** Extra synced values of the machine's menu, after {@link #DATA_COUNT}. */
    protected int extraDataCount() {
        return 0;
    }

    protected int getExtraData(int index) {
        return 0;
    }

    /** A machine button ({@code id >= BUTTON_MACHINE}) was pressed by an allowed player. */
    protected boolean pressMachineButton(Player player, int id) {
        return false;
    }

    // --- ticking ---

    public static void serverTick(Level level, BlockPos pos, BlockState state, TGMachineBlockEntity machine) {
        machine.serverTick();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TGMachineBlockEntity machine) {
        if (!machine.canWork()) {
            return;
        }
        if (machine.working && !machine.stalled && machine.redstoneMode.allows(machine.signal) && machine.progress < machine.totalTime) {
            machine.progress++;
            machine.playWorkSound();
        } else if (!machine.working) {
            machine.clientIdleTick();
        }
    }

    private void serverTick() {
        if (!canWork()) {
            return;
        }
        if (!redstoneMode.allows(signal)) {
            setStalled(operation != null);
            return;
        }
        if (operation != null && !keepOperation()) {
            operation = null;
            progress = 0;
            totalTime = 0;
            contentsChanged = true;
            stalled = false;
            setChanged();
            sendUpdate();
        }
        if (operation != null) {
            if (consumePower(operation.powerPerTick() * operation.multiplier())) {
                setStalled(false);
                progress++;
                if (progress >= totalTime) {
                    MachineOperation finished = operation;
                    operation = null;
                    progress = 0;
                    totalTime = 0;
                    finishOperation(finished);
                    checkAndStartOperation();
                    sendUpdate();
                }
            } else {
                setStalled(true);
            }
        } else if (!idleTick() && contentsChanged) {
            contentsChanged = false;
            checkAndStartOperation();
        }
    }

    private void setStalled(boolean stalled) {
        if (this.stalled != stalled) {
            this.stalled = stalled;
            sendUpdate();
        }
    }

    /** Drains power for one tick; always true with {@code machinesNeedNoPower}. */
    protected boolean consumePower(int amount) {
        return TGConfig.MACHINES_NEED_NO_POWER.get() || energy.consume(amount);
    }

    /** Begins {@code operation}; the inputs must already be taken. */
    protected void startOperation(MachineOperation operation) {
        this.operation = operation;
        this.progress = 0;
        this.totalTime = operation.time();
        setChanged();
        sendUpdate();
    }

    /** Techguns 2 stack upgrade: 1 + the number of machine upgrades in the upgrade slot. */
    protected int maxStackMultiplier() {
        int slot = upgradeSlot();
        return slot < 0 ? 1 : 1 + (inventory.getStackInSlot(slot).is(TGItems.MACHINE_UPGRADE_STACK.get())
                ? inventory.getStackInSlot(slot).getCount() : 0);
    }

    /** Whether {@code output} fits into {@code slot} (Techguns 2 {@code canOutput}). */
    protected boolean canOutput(ItemStack output, int slot) {
        if (output.isEmpty()) {
            return true;
        }
        ItemStack present = inventory.getStackInSlot(slot);
        if (present.isEmpty()) {
            return output.getCount() <= Math.min(inventory.getSlotLimit(slot), output.getMaxStackSize());
        }
        return ItemStack.isSameItemSameComponents(present, output) && present.getCount() + output.getCount() <= present.getMaxStackSize();
    }

    /** Whether {@code slot} holds at least {@code count} items. */
    protected boolean has(int slot, int count) {
        return inventory.getStackInSlot(slot).getCount() >= count;
    }

    /** Removes {@code count} items from {@code slot}. */
    protected void consume(int slot, int count) {
        if (count > 0) {
            ItemStack stack = inventory.getStackInSlot(slot);
            inventory.setStackInSlot(slot, stack.copyWithCount(Math.max(0, stack.getCount() - count)));
        }
    }

    /** Whether all {@code outputs} fit into the slots {@code from..to} (inclusive), merged like {@link #insertAll}. */
    protected boolean canInsertAll(int from, int to, List<ItemStack> outputs) {
        return insertAll(from, to, outputs, true).stream().allMatch(ItemStack::isEmpty);
    }

    /**
     * Merges {@code outputs} into the slots {@code from..to} (inclusive), filling matching stacks first, then empty
     * slots (Techguns 2 {@code ItemStackHandlerPlus.mergeAll}); returns what did not fit.
     */
    protected List<ItemStack> insertAll(int from, int to, List<ItemStack> outputs, boolean simulate) {
        ItemStack[] slots = new ItemStack[to - from + 1];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = inventory.getStackInSlot(from + i).copy();
        }
        List<ItemStack> leftovers = new ArrayList<>();
        for (ItemStack output : outputs) {
            ItemStack rest = output.copy();
            for (int pass = 0; pass < 2 && !rest.isEmpty(); pass++) {
                for (int i = 0; i < slots.length && !rest.isEmpty(); i++) {
                    if (pass == 0 && !slots[i].isEmpty() && ItemStack.isSameItemSameComponents(slots[i], rest)) {
                        int moved = Math.min(rest.getCount(), slots[i].getMaxStackSize() - slots[i].getCount());
                        slots[i].grow(moved);
                        rest.shrink(moved);
                    } else if (pass == 1 && slots[i].isEmpty()) {
                        int moved = Math.min(rest.getCount(), rest.getMaxStackSize());
                        slots[i] = rest.copyWithCount(moved);
                        rest.shrink(moved);
                    }
                }
            }
            leftovers.add(rest);
        }
        if (!simulate) {
            for (int i = 0; i < slots.length; i++) {
                inventory.setStackInSlot(from + i, slots[i]);
            }
        }
        return leftovers;
    }

    /** Puts {@code output} into {@code slot}, ignoring the slot rules (outputs are not placeable by hand). */
    protected void deliver(int slot, ItemStack output) {
        if (output.isEmpty()) {
            return;
        }
        ItemStack present = inventory.getStackInSlot(slot);
        if (present.isEmpty()) {
            inventory.setStackInSlot(slot, output.copy());
        } else {
            inventory.setStackInSlot(slot, present.copyWithCount(present.getCount() + output.getCount()));
        }
    }

    // --- access, settings, buttons ---

    public void setOwner(Player player) {
        this.owner = player.getUUID();
        this.ownerName = player.getGameProfile().getName();
        this.security = TGTeams.SECURITY_PUBLIC;
        setChanged();
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
        boolean handled = switch (id) {
            case BUTTON_REDSTONE -> {
                redstoneMode = redstoneMode.next();
                yield true;
            }
            case BUTTON_SECURITY -> {
                if (!isOwner(player)) {
                    yield false;
                }
                security = TGTeams.nextSecurity(security);
                yield true;
            }
            default -> pressMachineButton(player, id);
        };
        if (handled) {
            contentsChanged = true;
            setChanged();
            sendUpdate();
        }
        return handled;
    }

    /** Neighbour update: redstone signal (Techguns 2 {@code onNeighborBlockChange}). */
    public void updateSignal() {
        if (level != null && !level.isClientSide) {
            boolean powered = level.hasNeighborSignal(worldPosition);
            if (powered != signal) {
                signal = powered;
                setChanged();
                sendUpdate();
            }
        }
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    // --- getters ---

    public IItemHandlerModifiable getInventory() {
        return inventory;
    }

    /** Capability view: automation inserts by the slot rules and extracts only from output slots. */
    public IItemHandler getAutomationHandler() {
        return automation;
    }

    public MachineEnergy getEnergy() {
        return energy;
    }

    public ContainerData getData() {
        return data;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public boolean hasSignal() {
        return signal;
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

    public boolean isWorking() {
        return level != null && level.isClientSide ? working : operation != null;
    }

    /** Client: the job waits for power or a redstone signal (renderers stop animating). */
    public boolean isStalled() {
        return stalled;
    }

    /** Job progress 0..1 (client: for the renderer). */
    public float getProgress() {
        return totalTime > 0 ? (float) progress / totalTime : 0.0f;
    }

    /** Client: the job progress between two ticks, so the renderers animate smoothly instead of at 20 steps a second. */
    public float getProgress(float partialTick) {
        if (totalTime <= 0) {
            return 0.0f;
        }
        boolean advancing = working && !stalled && redstoneMode.allows(signal) && progress < totalTime && canWork();
        return Math.min(1.0f, (progress + (advancing ? partialTick : 0.0f)) / totalTime);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    // --- saving and syncing ---

    protected void sendUpdate() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energy.getEnergyStored());
        if (operation != null) {
            MachineOperation.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), operation)
                    .ifSuccess(op -> tag.put("operation", op));
        }
        saveClientData(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energy.setEnergy(tag.getInt("energy"));
        operation = tag.contains("operation")
                ? MachineOperation.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("operation")).result().orElse(null)
                : null;
        loadClientData(tag);
    }

    /** Everything the client needs as well: settings, owner and the job state for the renderer. */
    protected void saveClientData(CompoundTag tag) {
        tag.putByte("redstone", (byte) redstoneMode.ordinal());
        tag.putBoolean("signal", signal);
        if (owner != null) {
            tag.putUUID("owner", owner);
            tag.putString("owner_name", ownerName);
        }
        tag.putByte("security", (byte) security);
        tag.putInt("progress", progress);
        tag.putInt("total_time", totalTime);
        tag.putBoolean("working", operation != null);
        tag.putBoolean("stalled", stalled);
    }

    protected void loadClientData(CompoundTag tag) {
        redstoneMode = RedstoneMode.byId(tag.getByte("redstone"));
        signal = tag.getBoolean("signal");
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        ownerName = tag.getString("owner_name");
        // before 8f1 there was only owner_only
        security = tag.contains("security") ? tag.getByte("security") : tag.getBoolean("owner_only") ? TGTeams.SECURITY_OWNER : TGTeams.SECURITY_PUBLIC;
        progress = tag.getInt("progress");
        totalTime = tag.getInt("total_time");
        working = tag.getBoolean("working");
        stalled = tag.getBoolean("stalled");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveClientData(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Client: reads an update packet. The default would run {@link #loadAdditional}, which resets the inventory the
     * open menu keeps in sync (the update tag has no inventory).
     */
    @Override
    public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        loadUpdateTag(packet.getTag(), registries);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadUpdateTag(tag, registries);
    }

    /** Reads what {@link #getUpdateTag} wrote. */
    protected void loadUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadClientData(tag);
    }

    /** Automation access to {@link #inventory}: the slot rules for inserting, output slots only for extracting. */
    private final class AutomationHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isOutputSlot(slot) ? stack : inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return isOutputSlot(slot) ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !isOutputSlot(slot) && inventory.isItemValid(slot, stack);
        }
    }
}
