package techguns.slot.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;
import techguns.slot.ExtraSlots;
import techguns.slot.FallbackSlots;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/**
 * Screen for the fallback extra slots (face, back, hand) next to the player inventory; used when Curios is absent.
 */
// MIGRATION(26.x): menu/slot APIs (Slot constructor, quickMoveStack, MenuType) change in 1.21.2+.
public class ExtraSlotsMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = TGSlotType.values().length;
    private static final int INV_START = SLOT_COUNT;
    private static final int HOTBAR_START = INV_START + 27;
    private static final int END = HOTBAR_START + 9;

    private final Player player;

    public ExtraSlotsMenu(int id, Inventory inventory) {
        super(TGMenus.EXTRA_SLOTS.get(), id);
        this.player = inventory.player;
        Container container = new ExtraSlotsContainer(player);
        for (TGSlotType type : TGSlotType.values()) {
            addSlot(new GearSlot(container, type, 77, 8 + type.ordinal() * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + (row + 1) * 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < INV_START) {
            if (!moveItemStackTo(stack, INV_START, END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof TGSlotItem item && moveItemStackTo(stack, item.slot().ordinal(), item.slot().ordinal() + 1, false)) {
            // moved into its gear slot
        } else if (index < HOTBAR_START) {
            if (!moveItemStackTo(stack, HOTBAR_START, END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
            return ItemStack.EMPTY;
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
    public boolean stillValid(Player player) {
        return !ExtraSlots.usesCurios() && !player.isRemoved();
    }

    /** The three fallback stacks as a vanilla container; changes are synced to all trackers on the server. */
    private static final class ExtraSlotsContainer implements Container {
        private final Player player;
        private final FallbackSlots.Inventory inventory;

        ExtraSlotsContainer(Player player) {
            this.player = player;
            this.inventory = FallbackSlots.inventory(player);
        }

        @Override
        public int getContainerSize() {
            return SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            return inventory.stacks().stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int index) {
            return inventory.stacks().get(index);
        }

        @Override
        public ItemStack removeItem(int index, int amount) {
            ItemStack result = inventory.stacks().get(index).split(amount);
            if (!result.isEmpty()) {
                setChanged();
            }
            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack stack = inventory.stacks().get(index);
            inventory.stacks().set(index, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            inventory.stacks().set(index, stack);
            setChanged();
        }

        @Override
        public void setChanged() {
            if (!player.level().isClientSide) {
                ExtraSlots.markChanged(player);
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            inventory.stacks().clear();
        }
    }

    /** Takes only gear of the matching slot type, one item. */
    public static final class GearSlot extends Slot {
        private final TGSlotType type;

        public GearSlot(Container container, TGSlotType type, int x, int y) {
            super(container, type.ordinal(), x, y);
            this.type = type;
        }

        public TGSlotType type() {
            return type;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof TGSlotItem item && item.slot() == type;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
