package techguns.machine;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import techguns.multiblock.MultiblockMasterBlockEntity;
import techguns.multiblock.MultiblockSchematic;
import techguns.recipe.FabricatorRecipe;
import techguns.recipe.MachineInput;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGRecipes;
import techguns.registry.TGSounds;
import techguns.registry.TGTags;

/**
 * Fabricator (Techguns 2 {@code FabricatorTileEntMaster}): a 2x2x2 multiblock that builds advanced parts from an
 * input, wires, powder and a plate. 100 ticks at 80 FE per tick and operation; works only when formed.
 */
public class FabricatorBlockEntity extends MultiblockMasterBlockEntity {
    public static final int SLOT_INPUT = FabricatorRecipe.SLOT_INPUT;
    public static final int SLOT_WIRES = FabricatorRecipe.SLOT_WIRES;
    public static final int SLOT_POWDER = FabricatorRecipe.SLOT_POWDER;
    public static final int SLOT_PLATE = FabricatorRecipe.SLOT_PLATE;
    public static final int SLOT_OUTPUT = 4;
    public static final int SLOT_UPGRADE = 5;

    public static final int ENERGY_CAPACITY = 100000;
    public static final int POWER_PER_TICK = 80;
    public static final int TIME = 100;
    private static final float SOUND_VOLUME = 0.5f;

    /** Client: the plate (or input) being worked on and the result, for the renderer. */
    private ItemStack workPiece = ItemStack.EMPTY;
    private ItemStack workResult = ItemStack.EMPTY;

    public FabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.FABRICATOR.get(), pos, state, 6, ENERGY_CAPACITY);
    }

    @Override
    public MultiblockSchematic schematic() {
        return FabricatorSchematic.INSTANCE;
    }

    @Override
    protected int upgradeSlot() {
        return SLOT_UPGRADE;
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot == SLOT_OUTPUT;
    }

    /** Techguns 2 {@code getValidSlotForItemInMachine}: wires, powders and plates by tag, everything else is the input. */
    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        return slot <= SLOT_PLATE && slotFor(stack) == slot;
    }

    private static int slotFor(ItemStack stack) {
        if (stack.is(TGTags.Items.FABRICATOR_WIRES)) {
            return SLOT_WIRES;
        } else if (stack.is(TGTags.Items.FABRICATOR_POWDERS)) {
            return SLOT_POWDER;
        } else if (stack.is(TGTags.Items.FABRICATOR_PLATES)) {
            return SLOT_PLATE;
        }
        return SLOT_INPUT;
    }

    public int targetSlot(ItemStack stack) {
        return inventory.isItemValid(SLOT_UPGRADE, stack) ? SLOT_UPGRADE : slotFor(stack);
    }

    private Optional<FabricatorRecipe> recipe() {
        if (level == null) {
            return Optional.empty();
        }
        MachineInput input = MachineInput.of(inventory.getStackInSlot(SLOT_INPUT), inventory.getStackInSlot(SLOT_WIRES),
                inventory.getStackInSlot(SLOT_POWDER), inventory.getStackInSlot(SLOT_PLATE));
        return level.getRecipeManager().getAllRecipesFor(TGRecipes.FABRICATOR.get()).stream().map(RecipeHolder::value)
                .filter(r -> r.matches(input, level)).findFirst();
    }

    @Override
    protected void checkAndStartOperation() {
        Optional<FabricatorRecipe> found = recipe();
        if (found.isEmpty() || !canOutput(found.get().result(), SLOT_OUTPUT)) {
            return;
        }
        FabricatorRecipe recipe = found.get();
        int multiplier = maxStackMultiplier();
        for (int slot = SLOT_INPUT; slot <= SLOT_PLATE; slot++) {
            multiplier = Math.min(multiplier, recipe.slot(slot).times(inventory.getStackInSlot(slot)));
        }
        while (multiplier > 1 && !canOutput(recipe.result().copyWithCount(recipe.result().getCount() * multiplier), SLOT_OUTPUT)) {
            multiplier--;
        }
        ItemStack plate = inventory.getStackInSlot(SLOT_PLATE);
        workPiece = (plate.isEmpty() ? inventory.getStackInSlot(SLOT_INPUT) : plate).copyWithCount(1);
        workResult = recipe.result().copyWithCount(1);
        for (int slot = SLOT_INPUT; slot <= SLOT_PLATE; slot++) {
            consume(slot, recipe.slot(slot).count() * multiplier);
        }
        contentsChanged = false;
        startOperation(new MachineOperation(List.of(recipe.result()), multiplier, TIME, POWER_PER_TICK));
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
        deliver(SLOT_OUTPUT, operation.output(0));
        workPiece = ItemStack.EMPTY;
        workResult = ItemStack.EMPTY;
    }

    @Override
    protected void playWorkSound() {
        if (level != null && progress == 1) {
            level.playLocalSound(worldPosition, TGSounds.FABRICATOR_WORK.get(), SoundSource.BLOCKS, SOUND_VOLUME, 1.0f, true);
        }
    }

    /** Techguns 2 renderer: the plate (or input) until 80 %, then the result. Client. */
    public ItemStack getShownItem() {
        return getProgress() < 0.8f ? workPiece : workResult;
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        if (level != null) {
            HolderLookup.Provider registries = level.registryAccess();
            if (!workPiece.isEmpty()) {
                tag.put("work_piece", workPiece.save(registries));
            }
            if (!workResult.isEmpty()) {
                tag.put("work_result", workResult.save(registries));
            }
        }
    }

    @Override
    protected void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        if (level != null) {
            workPiece = ItemStack.parseOptional(level.registryAccess(), tag.getCompound("work_piece"));
            workResult = ItemStack.parseOptional(level.registryAccess(), tag.getCompound("work_result"));
        }
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("techguns.container.fabricator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FabricatorMenu(id, inventory, this);
    }
}
