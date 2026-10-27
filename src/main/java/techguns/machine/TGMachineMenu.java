package techguns.machine;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menu of a {@link TGMachineBlockEntity} (Techguns 2 {@code BasicMachineContainer}): the machine slots, then the player
 * inventory; progress, power and settings are synced through the machine's {@link ContainerData}, the redstone and
 * security buttons go through {@link #clickMenuButton}.
 */
// MIGRATION(26.x): menu/slot APIs (Slot constructor, quickMoveStack, MenuType) change in 1.21.2+.
public abstract class TGMachineMenu<T extends TGMachineBlockEntity> extends AbstractContainerMenu {
    protected final T machine;
    protected final ContainerData data;
    private int machineSlots;
    /** End of the player inventory slots; slots after it (worn armor in benches) are not shift-click targets. */
    private int playerSlotsEnd;
    private final Map<Slot, String> slotIcons = new HashMap<>();

    /** Server side: the machine's live data; client side: the client's copy of the machine and a synced data mirror. */
    protected TGMachineMenu(MenuType<?> type, int id, T machine) {
        super(type, id);
        this.machine = machine;
        this.data = machine.getLevel() != null && machine.getLevel().isClientSide
                ? new SimpleContainerData(machine.getData().getCount()) : machine.getData();
        addDataSlots(data);
    }

    /** Reads the machine position the server wrote when it opened the menu. */
    @SuppressWarnings("unchecked")
    protected static <T extends TGMachineBlockEntity> T machineAt(Inventory inventory, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        return (T) inventory.player.level().getBlockEntity(pos);
    }

    protected Slot addMachineSlot(int index, int x, int y) {
        machineSlots++;
        return addSlot(new SlotItemHandler(machine.getInventory(), index, x, y));
    }

    /** A machine slot showing a Techguns 2 empty slot icon ({@code textures/gui/emptyslots/emptyslot_<icon>.png}). */
    protected Slot addMachineSlot(int index, int x, int y, String icon) {
        Slot slot = addMachineSlot(index, x, y);
        slotIcons.put(slot, icon);
        return slot;
    }

    /** Empty slot icons by slot, for the screen. */
    public Map<Slot, String> slotIcons() {
        return slotIcons;
    }

    /** The player inventory at the Techguns 2 position (top left 8, 84). */
    protected void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + (row + 1) * 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        playerSlotsEnd = slots.size();
    }

    public T machine() {
        return machine;
    }

    public int progress() {
        return data.get(TGMachineBlockEntity.DATA_PROGRESS);
    }

    public int totalTime() {
        return data.get(TGMachineBlockEntity.DATA_TOTAL_TIME);
    }

    public int energy() {
        // synced as two 16 bit halves (the data packet carries shorts)
        return (data.get(TGMachineBlockEntity.DATA_ENERGY_HIGH) & 0xFFFF) << 16 | (data.get(TGMachineBlockEntity.DATA_ENERGY_LOW) & 0xFFFF);
    }

    public int maxEnergy() {
        return machine.getEnergy().getMaxEnergyStored();
    }

    public RedstoneMode redstoneMode() {
        return RedstoneMode.byId(data.get(TGMachineBlockEntity.DATA_REDSTONE));
    }

    public boolean hasSignal() {
        return data.get(TGMachineBlockEntity.DATA_SIGNAL) != 0;
    }

    public boolean isOwnerOnly() {
        return data.get(TGMachineBlockEntity.DATA_OWNER_ONLY) != 0;
    }

    /** Machine slot for shift-clicked {@code stack}, or -1 (Techguns 2 {@code getValidSlotForItemInMachine}). */
    protected int targetSlot(ItemStack stack) {
        for (int i = 0; i < machineSlots; i++) {
            if (machine.getAutomationHandler().isItemValid(i, stack)) {
                return i;
            }
        }
        return -1;
    }

    /** Number of machine slots from {@code target} on that shift-clicked items may fill. */
    protected int targetSlotCount(int target) {
        return 1;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, playerSlotsEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else {
            int target = targetSlot(stack);
            if (target < 0 || !moveItemStackTo(stack, target, target + targetSlotCount(target), false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return machine.pressButton(player, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return machine.isUsableBy(player);
    }
}
