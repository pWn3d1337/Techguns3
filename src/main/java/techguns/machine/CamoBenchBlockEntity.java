package techguns.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import techguns.camo.CamoData;
import techguns.camo.TGCamo;
import techguns.recipe.CamoChangeRecipe;
import techguns.recipe.MachineInput;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGRecipes;

/**
 * Camo Bench (Techguns 2 {@code CamoBenchTileEnt}): one slot for a camo item (guns, armor, shields, gear with named
 * camos) or a block of a {@link CamoChangeRecipe} group. The menu lists every look of the item (Fabric-style) and
 * picks one, the Techguns 2 arrows cycle through them, and the worn armor has its own cycle buttons.
 */
public class CamoBenchBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_INPUT = 0;

    public static final int BUTTON_NEXT = BUTTON_MACHINE;
    public static final int BUTTON_PREVIOUS = BUTTON_MACHINE + 1;
    /** Worn armor head..feet: {@code BUTTON_ARMOR + 2 * i} next camo, {@code + 1} previous. */
    public static final int BUTTON_ARMOR = BUTTON_MACHINE + 2;
    /** Picks entry {@code id - BUTTON_VARIANT} of {@link #variants}. */
    public static final int BUTTON_VARIANT = BUTTON_ARMOR + 8;

    public CamoBenchBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.CAMO_BENCH.get(), pos, state, 1, 0);
    }

    /** Every look of {@code stack}, in camo order; empty if it can't change its camo. Same on client and server. */
    public static List<ItemStack> variants(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        List<TGCamo> camos = CamoData.camos(level.registryAccess(), stack);
        List<ItemStack> variants = new ArrayList<>();
        if (!camos.isEmpty()) {
            for (TGCamo camo : camos) {
                ItemStack variant = stack.copy();
                CamoData.set(variant, camo);
                variants.add(variant);
            }
        } else {
            group(level, stack).ifPresent(recipe -> recipe.entries().forEach(item -> variants.add(stack.transmuteCopy(item, stack.getCount()))));
        }
        return variants;
    }

    /** Index of {@code stack}'s current look in {@link #variants}, or -1. */
    public static int currentVariant(Level level, ItemStack stack) {
        Optional<TGCamo> camo = CamoData.current(level.registryAccess(), stack);
        if (camo.isPresent()) {
            return CamoData.camos(level.registryAccess(), stack).indexOf(camo.get());
        }
        return group(level, stack).map(recipe -> recipe.entries().indexOf(stack.getItem())).orElse(-1);
    }

    private static Optional<CamoChangeRecipe> group(Level level, ItemStack stack) {
        MachineInput input = MachineInput.of(stack);
        return level.getRecipeManager().getAllRecipesFor(TGRecipes.CAMO_CHANGE.get()).stream().map(RecipeHolder::value)
                .filter(r -> r.matches(input, level)).findFirst();
    }

    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        return level == null || !variants(level, stack).isEmpty();
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return false;
    }

    @Override
    protected void checkAndStartOperation() {
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
    }

    @Override
    protected boolean pressMachineButton(Player player, int id) {
        if (level == null) {
            return false;
        }
        if (id >= BUTTON_ARMOR && id < BUTTON_VARIANT) {
            int piece = (id - BUTTON_ARMOR) / 2;
            ItemStack armor = player.getItemBySlot(RepairBenchBlockEntity.ARMOR_SLOTS.get(piece));
            return CamoData.cycle(level.registryAccess(), armor, (id - BUTTON_ARMOR) % 2 == 1).isPresent();
        }
        ItemStack stack = inventory.getStackInSlot(SLOT_INPUT);
        List<ItemStack> variants = variants(level, stack);
        if (variants.isEmpty()) {
            return false;
        }
        int index;
        if (id == BUTTON_NEXT || id == BUTTON_PREVIOUS) {
            index = Math.floorMod(currentVariant(level, stack) + (id == BUTTON_NEXT ? 1 : -1), variants.size());
        } else {
            index = id - BUTTON_VARIANT;
            if (index < 0 || index >= variants.size()) {
                return false;
            }
        }
        inventory.setStackInSlot(SLOT_INPUT, variants.get(index));
        return true;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CamoBenchMenu(id, inventory, this);
    }
}
