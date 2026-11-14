package techguns.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import techguns.TGConfig;
import techguns.registry.TGBlockEntities;

/**
 * Combustion Generator (Techguns 3): burns furnace fuel, one burn tick per tick, for {@code combustionGeneratorFePerTick}
 * FE per tick (coal: 1600 ticks, 64000 FE at the default 40). Containers left by the fuel (the lava bucket's bucket)
 * stay in the fuel slot, where automation can take them.
 */
public class CombustionGeneratorBlockEntity extends TGGeneratorBlockEntity {
    public static final int SLOT_FUEL = 0;
    public static final int ENERGY_CAPACITY = 50000;
    public static final int OUTPUT_RATE = 200;

    public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.COMBUSTION_GENERATOR.get(), pos, state, 1, ENERGY_CAPACITY);
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }

    @Override
    protected int fePerTick() {
        return TGConfig.COMBUSTION_GENERATOR_FE_PER_TICK.get();
    }

    @Override
    protected int outputRate() {
        return OUTPUT_RATE;
    }

    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        return slot == SLOT_FUEL && isFuel(stack);
    }

    /** The fuel slot is an output while it holds a leftover container. */
    @Override
    protected boolean isOutputSlot(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        return !stack.isEmpty() && !isFuel(stack);
    }

    @Override
    protected boolean refuel() {
        ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);
        int burnTime = fuel.isEmpty() ? 0 : fuel.getBurnTime(RecipeType.SMELTING);
        if (burnTime <= 0) {
            return false;
        }
        ItemStack container = fuel.getCraftingRemainingItem();
        consume(SLOT_FUEL, 1);
        if (!container.isEmpty()) {
            if (inventory.getStackInSlot(SLOT_FUEL).isEmpty()) {
                inventory.setStackInSlot(SLOT_FUEL, container);
            } else if (level != null) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, container);
            }
        }
        burnRemaining = burnTime;
        burnTotal = burnTime;
        return true;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GeneratorMenu.Combustion(id, inventory, this);
    }
}
