package techguns.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import techguns.TGConfig;
import techguns.registry.TGBlockEntities;

/**
 * Liquid Fuel Generator (Techguns 3): burns the liquid fuels of {@link FluidFuel} from its tank, two burn ticks per
 * tick, for {@code liquidGeneratorFePerTick} FE per tick. Pipes and buckets fill the tank (fuel only, nothing can be
 * drained); a container in the input slot is emptied into the tank and goes to the output slot.
 */
public class LiquidGeneratorBlockEntity extends TGGeneratorBlockEntity {
    public static final int SLOT_CONTAINER_IN = 0;
    public static final int SLOT_CONTAINER_OUT = 1;
    public static final int DATA_FLUID = DATA_GENERATOR_COUNT;
    public static final int DATA_FLUID_AMOUNT = DATA_GENERATOR_COUNT + 1;
    public static final int ENERGY_CAPACITY = 100000;
    public static final int OUTPUT_RATE = 400;
    public static final int TANK_CAPACITY = 4000;
    public static final int BURN_PER_TICK = 2;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY, stack -> FluidFuel.burnPerMb(stack) > 0) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IFluidHandler fillOnly = new FillOnly();

    public LiquidGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.LIQUID_GENERATOR.get(), pos, state, 2, ENERGY_CAPACITY);
    }

    @Override
    protected int fePerTick() {
        return TGConfig.LIQUID_GENERATOR_FE_PER_TICK.get();
    }

    @Override
    protected int burnPerTick() {
        return BURN_PER_TICK;
    }

    @Override
    protected int outputRate() {
        return OUTPUT_RATE;
    }

    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        return slot == SLOT_CONTAINER_IN && stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot == SLOT_CONTAINER_OUT;
    }

    /** Burns one millibucket at a time. */
    @Override
    protected boolean refuel() {
        float value = FluidFuel.burnPerMb(tank.getFluid());
        if (value <= 0 || tank.drain(1, IFluidHandler.FluidAction.EXECUTE).isEmpty()) {
            return false;
        }
        burnRemaining = Math.max(1, Math.round(value));
        burnTotal = burnRemaining;
        return true;
    }

    @Override
    protected boolean idleTick() {
        emptyContainer();
        return super.idleTick();
    }

    /** Empties one container of the input slot into the tank if the result fits into the output slot. */
    private void emptyContainer() {
        ItemStack in = inventory.getStackInSlot(SLOT_CONTAINER_IN);
        if (in.isEmpty()) {
            return;
        }
        FluidActionResult simulated = FluidUtil.tryEmptyContainer(in.copyWithCount(1), fillOnly, Integer.MAX_VALUE, null, false);
        if (!simulated.isSuccess() || !canOutput(simulated.getResult(), SLOT_CONTAINER_OUT)) {
            return;
        }
        FluidActionResult result = FluidUtil.tryEmptyContainer(in.copyWithCount(1), fillOnly, Integer.MAX_VALUE, null, true);
        if (result.isSuccess()) {
            consume(SLOT_CONTAINER_IN, 1);
            deliver(SLOT_CONTAINER_OUT, result.getResult());
        }
    }

    public FluidTank getTank() {
        return tank;
    }

    /** Capability view: fill with fuel, never drain. */
    public IFluidHandler getFluidHandler() {
        return fillOnly;
    }

    @Override
    public boolean canUseItem(ItemStack stack) {
        return stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    @Override
    public boolean useItem(Player player, InteractionHand hand) {
        return FluidUtil.interactWithFluidHandler(player, hand, fillOnly);
    }

    @Override
    protected int extraDataCount() {
        return super.extraDataCount() + 2;
    }

    @Override
    protected int getExtraData(int index) {
        return switch (index) {
            case DATA_FLUID -> ChemLabBlockEntity.fluidId(tank.getFluid());
            case DATA_FLUID_AMOUNT -> tank.getFluidAmount();
            default -> super.getExtraData(index);
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tank.readFromNBT(registries, tag.getCompound("tank"));
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GeneratorMenu.Liquid(id, inventory, this);
    }

    private final class FillOnly implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            return tank.getFluid();
        }

        @Override
        public int getTankCapacity(int index) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return tank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return tank.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
