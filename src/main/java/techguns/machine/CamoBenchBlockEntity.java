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
import techguns.slot.ExtraSlots;
import techguns.slot.TGSlotType;
import techguns.camo.TGCamo;
import techguns.recipe.CamoChangeRecipe;
import techguns.recipe.MachineInput;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGRecipes;

/**
 * Camo Bench (Techguns 2 {@code CamoBenchTileEnt}): one slot for a camo item (guns, armor, shields, gear with named
 * camos) or a block of a {@link CamoChangeRecipe} group. The menu lists every look of the item (Fabric-style) and
 * picks one, the Techguns 2 arrows cycle through them. The screen's tabs pick the same way for the worn armor and the worn
 * face/back/hand gear ({@link #targetStack}).
 */
public class CamoBenchBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_INPUT = 0;

    public static final int BUTTON_NEXT = BUTTON_MACHINE;
    public static final int BUTTON_PREVIOUS = BUTTON_MACHINE + 1;
    /** Worn armor head..feet: {@code BUTTON_ARMOR + 2 * i} next camo, {@code + 1} previous. */
    public static final int BUTTON_ARMOR = BUTTON_MACHINE + 2;
    /**
     * Worn face/back/hand gear ({@link TGSlotType} order): {@code BUTTON_GEAR + 2 * i} next camo, {@code + 1} previous
     * (Techguns 2 had these buttons commented out).
     */
    public static final int BUTTON_GEAR = BUTTON_ARMOR + 8;
    /**
     * Picks entry {@code index} of {@link #variants} of {@link #targetStack target} {@code t}:
     * {@code BUTTON_VARIANT + t * VARIANT_STRIDE + index} ({@link #variantButton}).
     */
    public static final int BUTTON_VARIANT = BUTTON_GEAR + 2 * TGSlotType.values().length;
    public static final int VARIANT_STRIDE = 1000;

    /** Screen tabs: the input slot, then the worn armor head..feet, then the worn face/back/hand gear. */
    public static final int TARGET_INPUT = 0;
    public static final int TARGET_ARMOR = 1;
    public static final int TARGET_GEAR = TARGET_ARMOR + RepairBenchBlockEntity.ARMOR_SLOTS.size();
    public static final int TARGETS = TARGET_GEAR + TGSlotType.camoSlots().size();

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

    /** The item tab {@code target} works on: the input, a worn armor piece or a worn gear item. */
    public ItemStack targetStack(Player player, int target) {
        if (target < TARGET_ARMOR) {
            return inventory.getStackInSlot(SLOT_INPUT);
        }
        if (target < TARGET_GEAR) {
            return player.getItemBySlot(RepairBenchBlockEntity.ARMOR_SLOTS.get(target - TARGET_ARMOR));
        }
        return ExtraSlots.get(player, TGSlotType.camoSlots().get(target - TARGET_GEAR));
    }

    /** Button that picks entry {@code index} of target {@code target}'s variants. */
    public static int variantButton(int target, int index) {
        return BUTTON_VARIANT + target * VARIANT_STRIDE + index;
    }

    /** Button that cycles target {@code target} to its next (or with {@code back} previous) look. */
    public static int cycleButton(int target, boolean back) {
        int base;
        if (target < TARGET_ARMOR) {
            base = BUTTON_NEXT;
        } else if (target < TARGET_GEAR) {
            base = BUTTON_ARMOR + 2 * (target - TARGET_ARMOR);
        } else {
            base = BUTTON_GEAR + 2 * TGSlotType.camoSlots().get(target - TARGET_GEAR).ordinal();
        }
        return base + (back ? 1 : 0);
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
        if (id >= BUTTON_ARMOR && id < BUTTON_GEAR) {
            int piece = (id - BUTTON_ARMOR) / 2;
            ItemStack armor = player.getItemBySlot(RepairBenchBlockEntity.ARMOR_SLOTS.get(piece));
            return CamoData.cycle(level.registryAccess(), armor, (id - BUTTON_ARMOR) % 2 == 1).isPresent();
        }
        if (id >= BUTTON_GEAR && id < BUTTON_VARIANT) {
            TGSlotType slot = TGSlotType.values()[(id - BUTTON_GEAR) / 2];
            ItemStack gear = ExtraSlots.get(player, slot);
            if (CamoData.cycle(level.registryAccess(), gear, (id - BUTTON_GEAR) % 2 == 1).isEmpty()) {
                return false;
            }
            // set it again so Curios or the fallback slots send the change
            ExtraSlots.set(player, slot, gear);
            return true;
        }
        if (id >= BUTTON_VARIANT + VARIANT_STRIDE) {
            return pickWorn(player, (id - BUTTON_VARIANT) / VARIANT_STRIDE, (id - BUTTON_VARIANT) % VARIANT_STRIDE);
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

    /** Sets worn target {@code target} to camo {@code index} (worn items only have camos, no block groups). */
    private boolean pickWorn(Player player, int target, int index) {
        if (level == null || target >= TARGETS) {
            return false;
        }
        ItemStack stack = targetStack(player, target);
        List<TGCamo> camos = CamoData.camos(level.registryAccess(), stack);
        if (index >= camos.size()) {
            return false;
        }
        CamoData.set(stack, camos.get(index));
        if (target >= TARGET_GEAR) {
            // set it again so Curios or the fallback slots send the change
            ExtraSlots.set(player, TGSlotType.camoSlots().get(target - TARGET_GEAR), stack);
        }
        return true;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CamoBenchMenu(id, inventory, this);
    }
}
