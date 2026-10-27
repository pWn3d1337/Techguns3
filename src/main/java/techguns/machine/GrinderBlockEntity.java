package techguns.machine;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import techguns.gun.GunItem;
import techguns.armor.TGArmorItem;
import techguns.recipe.GrinderRecipe;
import techguns.recipe.MachineInput;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGItems;
import techguns.registry.TGRecipes;
import techguns.registry.TGSounds;

/**
 * Grinder (Techguns 2 {@code GrinderTileEnt}): grinds guns, plates and rounds back into materials, into nine output
 * slots; outputs with a chance are rolled when the job starts. 100 ticks at 5 FE per tick and operation.
 */
public class GrinderBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_UPGRADE = 1;
    public static final int SLOT_OUTPUT_FIRST = 2;
    public static final int SLOT_OUTPUT_LAST = SLOT_OUTPUT_FIRST + 8;

    public static final int ENERGY_CAPACITY = 20000;
    public static final int POWER_PER_TICK = 5;
    public static final int TIME = 100;

    /** Client: the item being ground, for the renderer and the particles. */
    private ItemStack grinding = ItemStack.EMPTY;

    public GrinderBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.GRINDER.get(), pos, state, 11, ENERGY_CAPACITY);
    }

    private Optional<GrinderRecipe> recipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        MachineInput input = MachineInput.of(stack);
        Optional<GrinderRecipe> recipe = level.getRecipeManager().getAllRecipesFor(TGRecipes.GRINDER.get()).stream().map(RecipeHolder::value)
                .filter(r -> r.matches(input, level)).findFirst();
        return recipe.isPresent() ? recipe : armorRecycling(stack);
    }

    /**
     * Techguns 2 {@code GrinderRecipeGenericArmor}: Techguns armor gives back its repair materials for the durability
     * it has left.
     */
    private static Optional<GrinderRecipe> armorRecycling(ItemStack stack) {
        if (!(stack.getItem() instanceof TGArmorItem armor) || !armor.canRepairOnBench()) {
            return Optional.empty();
        }
        List<GrinderRecipe.Output> outputs = armor.repairCost(stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()).stream()
                .map(material -> GrinderRecipe.Output.of(material.display())).toList();
        return outputs.isEmpty() ? Optional.empty() : Optional.of(new GrinderRecipe(Ingredient.of(stack.getItem()), outputs));
    }

    @Override
    protected int upgradeSlot() {
        return SLOT_UPGRADE;
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot >= SLOT_OUTPUT_FIRST && slot <= SLOT_OUTPUT_LAST;
    }

    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        return slot == SLOT_INPUT && (level == null || recipeFor(stack).isPresent());
    }

    @Override
    protected void checkAndStartOperation() {
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        Optional<GrinderRecipe> found = recipeFor(input);
        if (found.isEmpty() || !canInsertAll(SLOT_OUTPUT_FIRST, SLOT_OUTPUT_LAST, found.get().maxOutputs(1))) {
            return;
        }
        GrinderRecipe recipe = found.get();
        int multiplier = 1;
        for (int i = maxStackMultiplier(); i > 1; i--) {
            if (has(SLOT_INPUT, i) && canInsertAll(SLOT_OUTPUT_FIRST, SLOT_OUTPUT_LAST, recipe.maxOutputs(i))) {
                multiplier = i;
                break;
            }
        }
        grinding = input.copyWithCount(1);
        consume(SLOT_INPUT, multiplier);
        contentsChanged = false;
        // outputs are rolled now and already include the multiplier; the multiplier still scales the power
        startOperation(new MachineOperation(recipe.roll(multiplier, level.random), multiplier, TIME, POWER_PER_TICK));
    }

    /** Delivers the rolled outputs; what does not fit drops (Techguns 2). */
    @Override
    protected void finishOperation(MachineOperation operation) {
        insertAll(SLOT_OUTPUT_FIRST, SLOT_OUTPUT_LAST, operation.outputs(), false).forEach(rest ->
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, rest));
        grinding = ItemStack.EMPTY;
    }

    /** Techguns 2: start and grind sounds, item crack particles (steel plate for guns). */
    @Override
    protected void playWorkSound() {
        if (level == null) {
            return;
        }
        if (progress % 2 == 0 && !grinding.isEmpty()) {
            ItemStack particle = grinding.getItem() instanceof GunItem ? new ItemStack(TGItems.STEEL_PLATE.get()) : grinding;
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, particle), worldPosition.getX() + 0.5, worldPosition.getY() + 0.6,
                    worldPosition.getZ() + 0.5, Math.random() * 0.1 - 0.05, Math.random() * 0.1 + 0.1, Math.random() * 0.1 - 0.05);
        }
        int work1 = Math.round(totalTime * 0.2f) + 1;
        int work2 = Math.round(totalTime * 0.6f) - 1;
        if (progress == 1) {
            level.playLocalSound(worldPosition, TGSounds.GRINDER_START.get(), SoundSource.BLOCKS, 1.0f, 1.0f, true);
        } else if (progress == work1 || progress == work2) {
            level.playLocalSound(worldPosition, TGSounds.GRINDER_WORK.get(), SoundSource.BLOCKS, 1.0f, 1.0f, true);
        }
    }

    /** The item being ground (client: for the renderer). */
    public ItemStack getGrinding() {
        return grinding;
    }

    public int targetSlot(ItemStack stack) {
        if (inventory.isItemValid(SLOT_UPGRADE, stack)) {
            return SLOT_UPGRADE;
        }
        return inventory.isItemValid(SLOT_INPUT, stack) ? SLOT_INPUT : -1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!grinding.isEmpty()) {
            tag.put("grinding", grinding.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        grinding = ItemStack.parseOptional(registries, tag.getCompound("grinding"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (!grinding.isEmpty()) {
            tag.put("grinding", grinding.save(registries));
        }
        return tag;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GrinderMenu(id, inventory, this);
    }
}
