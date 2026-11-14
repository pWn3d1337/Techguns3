package techguns.machine;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import techguns.TGConfig;
import techguns.registry.TGBlockEntities;

/**
 * Electric Furnace (Techguns 3): smelts vanilla smelting recipes with FE in half the furnace time
 * ({@code electricFurnaceFePerTick} per tick and item); the stack upgrade smelts several items at once. The experience of
 * the smelted items collects in the furnace and goes to the player taking results from the output slot.
 */
public class ElectricFurnaceBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_UPGRADE = 2;
    public static final int ENERGY_CAPACITY = 20000;

    @Nullable
    private RecipeHolder<SmeltingRecipe> lastRecipe;
    private float storedExperience;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.ELECTRIC_FURNACE.get(), pos, state, 3, ENERGY_CAPACITY);
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
    protected boolean canPlace(int slot, ItemStack stack) {
        return slot == SLOT_INPUT && (level == null || recipeFor(stack).isPresent());
    }

    private Optional<RecipeHolder<SmeltingRecipe>> recipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        SingleRecipeInput input = new SingleRecipeInput(stack);
        if (lastRecipe != null && lastRecipe.value().matches(input, level)) {
            return Optional.of(lastRecipe);
        }
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level);
        recipe.ifPresent(r -> lastRecipe = r);
        return recipe;
    }

    /** Smelting time: half of the furnace time. */
    public static int smeltTime(SmeltingRecipe recipe) {
        return Math.max(1, recipe.getCookingTime() / 2);
    }

    @Override
    protected void checkAndStartOperation() {
        ItemStack in = inventory.getStackInSlot(SLOT_INPUT);
        Optional<RecipeHolder<SmeltingRecipe>> holder = recipeFor(in);
        if (holder.isEmpty() || level == null) {
            setLit(false);
            return;
        }
        SmeltingRecipe recipe = holder.get().value();
        ItemStack out = recipe.assemble(new SingleRecipeInput(in), level.registryAccess());
        if (out.isEmpty() || !canOutput(out, SLOT_OUTPUT)) {
            setLit(false);
            return;
        }
        int multiplier = 1;
        for (int i = Math.min(maxStackMultiplier(), in.getCount()); i > 1; i--) {
            if (canOutput(out.copyWithCount(out.getCount() * i), SLOT_OUTPUT)) {
                multiplier = i;
                break;
            }
        }
        consume(SLOT_INPUT, multiplier);
        storedExperience += recipe.getExperience() * multiplier;
        contentsChanged = false;
        startOperation(new MachineOperation(List.of(out), multiplier, smeltTime(recipe), TGConfig.ELECTRIC_FURNACE_FE_PER_TICK.get()));
        setLit(true);
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
        deliver(SLOT_OUTPUT, operation.output(0));
        setLit(false);
    }

    private void setLit(boolean lit) {
        BlockState state = getBlockState();
        if (level != null && !level.isClientSide && state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, lit), Block.UPDATE_ALL);
        }
    }

    /** Gives the collected experience to {@code player} (vanilla {@code AbstractFurnaceBlockEntity#createExperience}). */
    public void awardExperience(Player player) {
        if (!(level instanceof ServerLevel server) || storedExperience <= 0) {
            return;
        }
        int whole = Mth.floor(storedExperience);
        float fraction = Mth.frac(storedExperience);
        if (fraction != 0.0f && Math.random() < fraction) {
            whole++;
        }
        storedExperience = 0;
        setChanged();
        ExperienceOrb.award(server, player.position(), whole);
    }

    public float getStoredExperience() {
        return storedExperience;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("experience", storedExperience);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storedExperience = tag.getFloat("experience");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ElectricFurnaceMenu(id, inventory, this);
    }
}
