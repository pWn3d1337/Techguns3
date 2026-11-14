package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.fluids.FluidStack;
import techguns.registry.TGMenus;

/** Menus of the generators: the burn state for the flame, the fuel slot or the tank and its container slots. */
public abstract class GeneratorMenu<T extends TGGeneratorBlockEntity> extends TGMachineMenu<T> {
    protected GeneratorMenu(MenuType<?> type, int id, T machine) {
        super(type, id, machine);
    }

    /** Fuel burn ticks left of the fuel burning now. */
    public int burnRemaining() {
        return (data.get(TGGeneratorBlockEntity.DATA_BURN_HIGH) & 0xFFFF) << 16 | (data.get(TGGeneratorBlockEntity.DATA_BURN_LOW) & 0xFFFF);
    }

    public int burnTotal() {
        return (data.get(TGGeneratorBlockEntity.DATA_BURN_TOTAL_HIGH) & 0xFFFF) << 16
                | (data.get(TGGeneratorBlockEntity.DATA_BURN_TOTAL_LOW) & 0xFFFF);
    }

    /** Combustion Generator: the fuel slot under the flame. */
    public static class Combustion extends GeneratorMenu<CombustionGeneratorBlockEntity> {
        public static final int FUEL_X = 80;
        public static final int FUEL_Y = 53;

        public Combustion(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
            this(id, inventory, TGMachineMenu.<CombustionGeneratorBlockEntity>machineAt(inventory, buf));
        }

        public Combustion(int id, Inventory inventory, CombustionGeneratorBlockEntity machine) {
            super(TGMenus.COMBUSTION_GENERATOR.get(), id, machine);
            addMachineSlot(CombustionGeneratorBlockEntity.SLOT_FUEL, FUEL_X, FUEL_Y);
            addPlayerInventory(inventory);
        }
    }

    /** Liquid Fuel Generator: the tank, a container input slot and the emptied container output. */
    public static class Liquid extends GeneratorMenu<LiquidGeneratorBlockEntity> {
        public static final int CONTAINER_X = 116;
        public static final int CONTAINER_IN_Y = 17;
        public static final int CONTAINER_OUT_Y = 53;

        public Liquid(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
            this(id, inventory, TGMachineMenu.<LiquidGeneratorBlockEntity>machineAt(inventory, buf));
        }

        public Liquid(int id, Inventory inventory, LiquidGeneratorBlockEntity machine) {
            super(TGMenus.LIQUID_GENERATOR.get(), id, machine);
            addMachineSlot(LiquidGeneratorBlockEntity.SLOT_CONTAINER_IN, CONTAINER_X, CONTAINER_IN_Y, "bucket");
            addMachineSlot(LiquidGeneratorBlockEntity.SLOT_CONTAINER_OUT, CONTAINER_X, CONTAINER_OUT_Y);
            addPlayerInventory(inventory);
        }

        public FluidStack fluid() {
            int id = data.get(LiquidGeneratorBlockEntity.DATA_FLUID);
            int amount = data.get(LiquidGeneratorBlockEntity.DATA_FLUID_AMOUNT);
            return amount <= 0 || id < 0 ? FluidStack.EMPTY : new FluidStack(ChemLabBlockEntity.fluidById(id), amount);
        }
    }
}
