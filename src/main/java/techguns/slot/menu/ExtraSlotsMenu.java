package techguns.slot.menu;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import techguns.machine.PlayerEquipmentSlot;
import techguns.network.TGNetwork;
import techguns.registry.TGMenus;
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;
import techguns.slot.TabSlots;

/**
 * The Techguns inventory tab (Techguns 2 {@code TGPlayerInventoryContainer}): armor, the face/back/hand gear and the
 * offhand around the player, the three auto-feed slots, the heal slot and two ammo box slots on the right, then the
 * player inventory. The gear slots go through {@link ExtraSlots}, so with Curios they mirror the Curios slots and both
 * screens show the same items. The mouth slot is left to Curios (and right-click equipping).
 * <p>
 * A crafting recipe book menu with the inventory's 2x2 grid, so the tab shows the same recipe book as the vanilla
 * inventory and shares its open state; the tab has no grid itself, so a clicked recipe is placed in the vanilla
 * inventory, which the client switches to ({@link #handlePlacement}).
 */
// MIGRATION(26.x): menu/slot APIs (Slot constructor, quickMoveStack, MenuType) change in 1.21.2+; RecipeBookMenu is reworked in 1.21.2.
public class ExtraSlotsMenu extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
    /** The gear slots of the tab, in menu order. */
    public static final List<TGSlotType> GEAR = List.of(TGSlotType.FACE, TGSlotType.BACK, TGSlotType.HAND);
    public static final int ARMOR_START = GEAR.size();
    public static final int OFFHAND = ARMOR_START + 4;
    public static final int TAB_START = OFFHAND + 1;
    public static final int INV_START = TAB_START + TabSlots.SIZE;
    private static final int HOTBAR_START = INV_START + 27;
    private static final int END = HOTBAR_START + 9;
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public ExtraSlotsMenu(int id, Inventory inventory) {
        super(TGMenus.EXTRA_SLOTS.get(), id);
        Player player = inventory.player;
        Container container = new TabContainer(player);
        for (int i = 0; i < GEAR.size(); i++) {
            addSlot(new GearSlot(container, i, player, GEAR.get(i), 77, 8 + i * 18));
        }
        for (int i = 0; i < ARMOR.length; i++) {
            addSlot(PlayerEquipmentSlot.armor(inventory, ARMOR[i], 8, 8 + i * 18));
        }
        addSlot(PlayerEquipmentSlot.offhand(inventory, 77, 62));
        for (int i = 0; i < TabSlots.SIZE; i++) {
            // food in the top row, heal item and ammo boxes in the bottom row
            int column = i < TabSlots.HEAL ? i : i - TabSlots.HEAL;
            int row = i < TabSlots.HEAL ? 0 : 1;
            addSlot(new TabSlot(container, GEAR.size() + i, i, 116 + column * 18, 17 + row * 20));
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
        } else if (!moveIntoTab(player, stack)) {
            if (index < HOTBAR_START) {
                if (!moveItemStackTo(stack, HOTBAR_START, END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
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

    /** Shift-click from the inventory: gear, armor, offhand items, food, heal items and ammo boxes into their slots. */
    private boolean moveIntoTab(Player player, ItemStack stack) {
        if (stack.getItem() instanceof TGSlotItem item && GEAR.contains(item.slot())) {
            int gear = GEAR.indexOf(item.slot());
            return moveItemStackTo(stack, gear, gear + 1, false);
        }
        EquipmentSlot equipment = player.getEquipmentSlotForItem(stack);
        if (equipment.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            int armor = ARMOR_START + List.of(ARMOR).indexOf(equipment);
            return moveItemStackTo(stack, armor, armor + 1, false);
        }
        if (equipment == EquipmentSlot.OFFHAND && moveItemStackTo(stack, OFFHAND, OFFHAND + 1, false)) {
            return true;
        }
        for (int i = 0; i < TabSlots.SIZE; i++) {
            if (TabSlots.accepts(i, stack) && moveItemStackTo(stack, TAB_START + i, TAB_START + i + 1, false) && stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return !player.isRemoved();
    }

    /** A recipe clicked in the tab's recipe book: the client switches to the vanilla inventory and places it there. */
    @Override
    public void handlePlacement(boolean placeAll, RecipeHolder<?> recipe, ServerPlayer player) {
        TGNetwork.sendPlaceRecipeInInventory(player, recipe.id(), placeAll);
    }

    // Recipe book of the vanilla inventory: crafting with a 2x2 grid (InventoryMenu); the tab has no grid slots

    @Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {
    }

    @Override
    public void clearCraftingContent() {
    }

    @Override
    public boolean recipeMatches(RecipeHolder<CraftingRecipe> recipe) {
        return false;
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }

    @Override
    public int getGridWidth() {
        return 2;
    }

    @Override
    public int getGridHeight() {
        return 2;
    }

    /** No crafting slots: clicks on the tab's slots don't clear a ghost recipe (there is none). */
    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    public boolean shouldMoveToInventory(int slotIndex) {
        return false;
    }

    /**
     * The gear slots (read and written through {@link ExtraSlots}, Curios or the fallback slots) followed by the tab slots
     * ({@link TabSlots}) as one vanilla container. On the client it writes the client's copies, which the menu sync fills.
     */
    private static final class TabContainer implements Container {
        private final Player player;

        TabContainer(Player player) {
            this.player = player;
        }

        private static boolean isGear(int index) {
            return index < GEAR.size();
        }

        private List<ItemStack> tab() {
            return TabSlots.of(player).stacks();
        }

        @Override
        public int getContainerSize() {
            return GEAR.size() + TabSlots.SIZE;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < getContainerSize(); i++) {
                if (!getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return isGear(index) ? ExtraSlots.get(player, GEAR.get(index)) : tab().get(index - GEAR.size());
        }

        @Override
        public ItemStack removeItem(int index, int amount) {
            ItemStack stack = getItem(index);
            ItemStack result = stack.split(amount);
            if (!result.isEmpty()) {
                // written back, so Curios sees the change like any other
                setItem(index, stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack stack = getItem(index);
            setItem(index, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            if (isGear(index)) {
                // also syncs the fallback slots to the players around
                ExtraSlots.set(player, GEAR.get(index), stack);
            } else {
                tab().set(index - GEAR.size(), stack);
            }
        }

        /** A stack changed in place (stacking onto it): the fallback slots are synced to the players around. */
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
            for (int i = 0; i < getContainerSize(); i++) {
                setItem(i, ItemStack.EMPTY);
            }
        }
    }

    /** Takes only gear of the matching slot type, one item; hidden if the player lacks the slot (Curios datapacks). */
    public static final class GearSlot extends Slot {
        private final Player player;
        private final TGSlotType type;

        public GearSlot(Container container, int index, Player player, TGSlotType type, int x, int y) {
            super(container, index, x, y);
            this.player = player;
            this.type = type;
        }

        public TGSlotType type() {
            return type;
        }

        @Override
        public boolean isActive() {
            return ExtraSlots.hasSlot(player, type);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof TGSlotItem item && item.slot() == type && isActive();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /** A food, heal or ammo box slot ({@link TabSlots#accepts}). */
    public static final class TabSlot extends Slot {
        private final int tabIndex;

        public TabSlot(Container container, int index, int tabIndex, int x, int y) {
            super(container, index, x, y);
            this.tabIndex = tabIndex;
        }

        /** The index in {@link TabSlots}. */
        public int tabIndex() {
            return tabIndex;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return TabSlots.accepts(tabIndex, stack);
        }
    }
}
