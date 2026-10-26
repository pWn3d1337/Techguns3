package techguns.machine;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import techguns.recipe.MetalPressRecipe;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGRecipes;
import techguns.registry.TGSounds;

/**
 * Metal Press (Techguns 2 {@code MetalPressTileEnt}): presses one item from each input slot into the recipe result,
 * 100 ticks at 20 FE per tick and operation. With auto split on, a stack in one input slot is split over both slots
 * when it can be pressed with itself (two ingots make plates).
 */
public class MetalPressBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_INPUT1 = 0;
    public static final int SLOT_INPUT2 = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_UPGRADE = 3;
    public static final int BUTTON_AUTO_SPLIT = BUTTON_MACHINE;
    public static final int DATA_AUTO_SPLIT = DATA_COUNT;

    public static final int ENERGY_CAPACITY = 20000;
    public static final int POWER_PER_TICK = 20;
    public static final int TIME = 100;
    private static final float SOUND_VOLUME = 0.5f;

    private boolean autoSplit;

    public MetalPressBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.METAL_PRESS.get(), pos, state, 4, ENERGY_CAPACITY);
    }

    @Override
    protected int upgradeSlot() {
        return SLOT_UPGRADE;
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot == SLOT_OUTPUT;
    }

    @Override
    protected int powerPerTick() {
        return POWER_PER_TICK;
    }

    private List<RecipeHolder<MetalPressRecipe>> recipes() {
        return level == null ? List.of() : level.getRecipeManager().getAllRecipesFor(TGRecipes.METAL_PRESS.get());
    }

    private Optional<MetalPressRecipe> recipeFor(ItemStack a, ItemStack b) {
        return recipes().stream().map(RecipeHolder::value).filter(r -> r.matches(a, b)).findFirst();
    }

    private boolean isPressable(ItemStack a, ItemStack b) {
        return recipeFor(a, b).isPresent();
    }

    private boolean isRecipeInput(ItemStack stack) {
        return recipes().stream().anyMatch(r -> r.value().uses(stack));
    }

    /** Techguns 2 {@code isItemValidForSlot}: an input must still allow a recipe with the other input. */
    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        if (slot != SLOT_INPUT1 && slot != SLOT_INPUT2) {
            return false;
        }
        if (level == null) {
            return true;
        }
        ItemStack in1 = inventory.getStackInSlot(SLOT_INPUT1);
        ItemStack in2 = inventory.getStackInSlot(SLOT_INPUT2);
        if (in1.isEmpty() && in2.isEmpty()) {
            return isRecipeInput(stack);
        } else if (in1.isEmpty() && slot == SLOT_INPUT1) {
            return isPressable(stack, in2);
        } else if (in2.isEmpty() && slot == SLOT_INPUT2) {
            return isPressable(in1, stack);
        }
        return ItemStack.isSameItemSameComponents(inventory.getStackInSlot(slot), stack);
    }

    /** Techguns 2 {@code getValidSlotForItemInMachine} (shift click). */
    public int targetSlot(ItemStack stack) {
        ItemStack in1 = inventory.getStackInSlot(SLOT_INPUT1);
        ItemStack in2 = inventory.getStackInSlot(SLOT_INPUT2);
        if (!in1.isEmpty() && ItemStack.isSameItemSameComponents(in1, stack)) {
            return SLOT_INPUT1;
        } else if (!in2.isEmpty() && ItemStack.isSameItemSameComponents(in2, stack)) {
            return SLOT_INPUT2;
        } else if (in1.isEmpty() && isRecipeInput(stack)) {
            return SLOT_INPUT1;
        } else if (!in1.isEmpty() && in2.isEmpty() && isPressable(in1, stack)) {
            return SLOT_INPUT2;
        } else if (inventory.isItemValid(SLOT_UPGRADE, stack)) {
            return SLOT_UPGRADE;
        }
        return -1;
    }

    @Override
    protected void checkAndStartOperation() {
        if (autoSplit) {
            splitIfPressableWithItself(SLOT_INPUT1, SLOT_INPUT2);
            splitIfPressableWithItself(SLOT_INPUT2, SLOT_INPUT1);
        }
        ItemStack in1 = inventory.getStackInSlot(SLOT_INPUT1);
        ItemStack in2 = inventory.getStackInSlot(SLOT_INPUT2);
        Optional<MetalPressRecipe> recipe = recipeFor(in1, in2);
        if (recipe.isEmpty() || !canOutput(recipe.get().result(), SLOT_OUTPUT)) {
            return;
        }
        ItemStack output = recipe.get().result();
        int multiplier = 1;
        for (int i = maxStackMultiplier(); i > 1; i--) {
            if (canOutput(output.copyWithCount(output.getCount() * i), SLOT_OUTPUT) && in1.getCount() >= i && in2.getCount() >= i) {
                multiplier = i;
                break;
            }
        }
        inventory.setStackInSlot(SLOT_INPUT1, in1.copyWithCount(in1.getCount() - multiplier));
        inventory.setStackInSlot(SLOT_INPUT2, in2.copyWithCount(in2.getCount() - multiplier));
        contentsChanged = false;
        startOperation(new MachineOperation(List.of(output), multiplier, TIME));
    }

    /** Techguns 2 {@code splitSlot}: half of {@code from} moves to the empty {@code to} slot. */
    private void splitIfPressableWithItself(int from, int to) {
        ItemStack source = inventory.getStackInSlot(from);
        if (source.getCount() > 1 && inventory.getStackInSlot(to).isEmpty() && isPressable(source, source)) {
            int moved = source.getCount() / 2;
            inventory.setStackInSlot(to, source.copyWithCount(moved));
            inventory.setStackInSlot(from, source.copyWithCount(source.getCount() - moved));
        }
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
        deliver(SLOT_OUTPUT, operation.output(0));
    }

    /** Two press strokes per operation. */
    @Override
    protected void playWorkSound() {
        int first = Math.round(totalTime * 0.075f);
        int half = Math.round(totalTime * 0.5f);
        if (level != null && (progress == first || progress == first + half)) {
            level.playLocalSound(worldPosition, TGSounds.METAL_PRESS_WORK.get(), SoundSource.BLOCKS, SOUND_VOLUME, 1.0f, true);
        }
    }

    public boolean isAutoSplit() {
        return autoSplit;
    }

    @Override
    protected int extraDataCount() {
        return 1;
    }

    @Override
    protected int getExtraData(int index) {
        return index == DATA_AUTO_SPLIT && autoSplit ? 1 : 0;
    }

    @Override
    protected boolean pressMachineButton(Player player, int id) {
        if (id == BUTTON_AUTO_SPLIT) {
            autoSplit = !autoSplit;
            return true;
        }
        return false;
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        tag.putBoolean("auto_split", autoSplit);
    }

    @Override
    protected void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        autoSplit = tag.getBoolean("auto_split");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MetalPressMenu(id, inventory, this);
    }
}
