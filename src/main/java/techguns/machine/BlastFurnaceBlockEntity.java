package techguns.machine;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import techguns.recipe.BlastFurnaceRecipe;
import techguns.recipe.MachineInput;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGRecipes;

/**
 * Blast Furnace (Techguns 2 {@code BlastFurnaceTileEnt}): smelts counted inputs from two fixed slots (iron + coal into
 * steel, steel + obsidian into obsidian steel, copper + tin into bronze); power and time come from the recipe.
 */
public class BlastFurnaceBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_INPUT1 = 0;
    public static final int SLOT_INPUT2 = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_UPGRADE = 3;

    public static final int ENERGY_CAPACITY = 40000;

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.BLAST_FURNACE.get(), pos, state, 4, ENERGY_CAPACITY);
    }

    private List<BlastFurnaceRecipe> recipes() {
        return level == null ? List.of()
                : level.getRecipeManager().getAllRecipesFor(TGRecipes.BLAST_FURNACE.get()).stream().map(RecipeHolder::value).toList();
    }

    @Override
    protected int upgradeSlot() {
        return SLOT_UPGRADE;
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot == SLOT_OUTPUT;
    }

    /** Techguns 2 {@code hasRecipeWithInputForSlot}. */
    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        if (level == null) {
            return slot <= SLOT_INPUT2;
        }
        return switch (slot) {
            case SLOT_INPUT1 -> recipes().stream().anyMatch(r -> r.first().ingredient().test(stack));
            case SLOT_INPUT2 -> recipes().stream().anyMatch(r -> r.second().ingredient().test(stack));
            default -> false;
        };
    }

    @Override
    protected void checkAndStartOperation() {
        ItemStack in1 = inventory.getStackInSlot(SLOT_INPUT1);
        ItemStack in2 = inventory.getStackInSlot(SLOT_INPUT2);
        MachineInput input = MachineInput.of(in1, in2);
        Optional<BlastFurnaceRecipe> found = recipes().stream().filter(r -> r.matches(input, level)).findFirst();
        if (found.isEmpty() || !canOutput(found.get().result(), SLOT_OUTPUT)) {
            return;
        }
        BlastFurnaceRecipe recipe = found.get();
        ItemStack output = recipe.result();
        int multiplier = 1;
        for (int i = maxStackMultiplier(); i > 1; i--) {
            if (canOutput(output.copyWithCount(output.getCount() * i), SLOT_OUTPUT) && has(SLOT_INPUT1, recipe.first().count() * i)
                    && has(SLOT_INPUT2, recipe.second().count() * i)) {
                multiplier = i;
                break;
            }
        }
        consume(SLOT_INPUT1, recipe.first().count() * multiplier);
        consume(SLOT_INPUT2, recipe.second().count() * multiplier);
        contentsChanged = false;
        startOperation(new MachineOperation(List.of(output), multiplier, recipe.time(), recipe.powerPerTick()));
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
        deliver(SLOT_OUTPUT, operation.output(0));
    }

    /** Techguns 2: fire crackle, fire sound and smoke + flames at the front opening. */
    @Override
    protected void playWorkSound() {
        if (level == null) {
            return;
        }
        if (progress % 35 == 0) {
            level.playLocalSound(worldPosition, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.75f, 1.0f, true);
        }
        if (progress % 20 == 0) {
            level.playLocalSound(worldPosition, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.75f, 0.5f + level.random.nextFloat() * 0.15f, true);
            Direction facing = getBlockState().getValue(TGMachineBlock.FACING);
            double x = worldPosition.getX() + 0.5 + facing.getStepX() * 0.52;
            double y = worldPosition.getY() + level.random.nextDouble() * 6.0 / 16.0 + 3.0 / 16.0;
            double z = worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.52;
            double side = level.random.nextDouble() * 0.6 - 0.3;
            x += facing.getStepZ() * side;
            z += facing.getStepX() * side;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    /** Shift click (Techguns 2 {@code getValidSlotForItemInMachine}). */
    public int targetSlot(ItemStack stack) {
        for (int slot : new int[]{SLOT_INPUT1, SLOT_INPUT2, SLOT_UPGRADE}) {
            if (inventory.isItemValid(slot, stack)) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BlastFurnaceMenu(id, inventory, this);
    }
}
