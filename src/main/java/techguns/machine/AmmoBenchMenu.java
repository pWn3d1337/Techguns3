package techguns.machine;

import java.util.Comparator;
import java.util.List;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import techguns.registry.TGTags;
import techguns.recipe.AmmoBenchRecipe;
import techguns.recipe.MachineInput;
import techguns.registry.TGBlocks;
import techguns.registry.TGMenus;
import techguns.registry.TGRecipes;

/**
 * Ammo Bench menu (Fabric {@code AmmoBenchScreenHandler}, like the vanilla stonecutter): bullet core, casing and
 * powder in; every matching {@link AmmoBenchRecipe} is a choice, taking the result uses one of each input. The inputs
 * go back to the player when the menu closes.
 */
// MIGRATION(26.x): menu/slot APIs change in 1.21.2+ (see TGMachineMenu).
public class AmmoBenchMenu extends AbstractContainerMenu {
    public static final int SLOT_RESULT = 3;
    private static final int PLAYER_START = 4;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final ContainerLevelAccess access;
    private final Level level;
    private final DataSlot selected = DataSlot.standalone();
    private List<RecipeHolder<AmmoBenchRecipe>> recipes = List.of();
    private long lastSoundTime;
    private final Container inputs = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };
    private final ResultContainer result = new ResultContainer();

    /** Client constructor (menu type factory). */
    public AmmoBenchMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public AmmoBenchMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(TGMenus.AMMO_BENCH.get(), id);
        this.access = access;
        this.level = inventory.player.level();
        addSlot(new TagSlot(AmmoBenchRecipe.SLOT_CORE, 20, 15, TGTags.Items.BULLET_CORE));
        addSlot(new TagSlot(AmmoBenchRecipe.SLOT_CASING, 20, 34, TGTags.Items.BULLET_CASING));
        addSlot(new TagSlot(AmmoBenchRecipe.SLOT_POWDER, 20, 53, TGTags.Items.BULLET_POWDER));
        addSlot(new Slot(result, 0, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());
                for (int i = 0; i < 3; i++) {
                    inputs.removeItem(i, 1);
                }
                access.execute((level, pos) -> {
                    if (lastSoundTime != level.getGameTime()) {
                        level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0f, 1.0f);
                        lastSoundTime = level.getGameTime();
                    }
                });
                super.onTake(player, stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + (row + 1) * 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        addDataSlot(selected);
    }

    private final class TagSlot extends Slot {
        private final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag;

        TagSlot(int index, int x, int y, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
            super(inputs, index, x, y);
            this.tag = tag;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(tag);
        }
    }

    public List<ItemStack> choices() {
        return recipes.stream().map(holder -> holder.value().result()).toList();
    }

    public int selected() {
        return selected.get();
    }

    private MachineInput input() {
        return MachineInput.of(inputs.getItem(0), inputs.getItem(1), inputs.getItem(2));
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == inputs) {
            MachineInput input = input();
            List<RecipeHolder<AmmoBenchRecipe>> found = level.getRecipeManager().getAllRecipesFor(TGRecipes.AMMO_BENCH.get()).stream()
                    .filter(holder -> holder.value().matches(input, level)).sorted(Comparator.comparing(holder -> holder.id())).toList();
            if (!found.equals(recipes)) {
                recipes = found;
                selected.set(-1);
            }
            updateResult();
        }
    }

    private void updateResult() {
        int index = selected.get();
        result.setItem(0, index >= 0 && index < recipes.size() ? recipes.get(index).value().result().copy() : ItemStack.EMPTY);
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < recipes.size()) {
            selected.set(id);
            updateResult();
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == SLOT_RESULT) {
            stack.getItem().onCraftedBy(stack, player.level(), player);
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (index < SLOT_RESULT) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            int target = stack.is(TGTags.Items.BULLET_CORE) ? 0 : stack.is(TGTags.Items.BULLET_CASING) ? 1 : stack.is(TGTags.Items.BULLET_POWDER) ? 2 : -1;
            if (target < 0 || !moveItemStackTo(stack, target, target + 1, false)) {
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
        if (index == SLOT_RESULT) {
            player.drop(stack, false);
        }
        return copy;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != result && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        result.removeItemNoUpdate(0);
        access.execute((level, pos) -> clearContainer(player, inputs));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, TGBlocks.AMMO_BENCH.get());
    }
}
