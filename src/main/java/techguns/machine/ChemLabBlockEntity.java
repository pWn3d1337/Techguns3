package techguns.machine;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import techguns.recipe.ChemLabRecipe;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGRecipes;
import techguns.registry.TGSounds;

/**
 * Chemical Laboratory (Techguns 2 {@code ChemLabTileEnt}): two item inputs, a flask slot and an input tank make items
 * and/or a fluid in the output tank; 100 ticks, power from the recipe. Pipes and buckets fill the input tank and drain
 * the output tank, or the input tank when "drain input" is on; the tanks can be dumped from the screen.
 */
public class ChemLabBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_INPUT1 = 0;
    public static final int SLOT_INPUT2 = 1;
    public static final int SLOT_FLASK = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_UPGRADE = 4;
    public static final int BUTTON_DUMP_INPUT = BUTTON_MACHINE;
    public static final int BUTTON_DUMP_OUTPUT = BUTTON_MACHINE + 1;
    public static final int BUTTON_TOGGLE_DRAIN = BUTTON_MACHINE + 2;
    public static final int DATA_INPUT_FLUID = DATA_COUNT;
    public static final int DATA_INPUT_AMOUNT = DATA_COUNT + 1;
    public static final int DATA_OUTPUT_FLUID = DATA_COUNT + 2;
    public static final int DATA_OUTPUT_AMOUNT = DATA_COUNT + 3;
    public static final int DATA_DRAIN_INPUT = DATA_COUNT + 4;

    public static final int ENERGY_CAPACITY = 20000;
    public static final int INPUT_TANK_CAPACITY = 8000;
    public static final int OUTPUT_TANK_CAPACITY = 16000;
    public static final int TIME = 100;
    private static final float SOUND_VOLUME = 0.65f;

    private final FluidTank inputTank = new FluidTank(INPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            contentsChanged = true;
            setChanged();
        }
    };
    private final FluidTank outputTank = new FluidTank(OUTPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            contentsChanged = true;
            setChanged();
        }
    };
    private final IFluidHandler fluidHandler = new TankHandler();
    private boolean drainInput;
    /** Fluid result of the running job, added to the output tank when it finishes. */
    private FluidStack pendingFluid = FluidStack.EMPTY;

    public ChemLabBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.CHEM_LAB.get(), pos, state, 5, ENERGY_CAPACITY);
    }

    private List<ChemLabRecipe> recipes() {
        return level == null ? List.of()
                : level.getRecipeManager().getAllRecipesFor(TGRecipes.CHEM_LAB.get()).stream().map(RecipeHolder::value).toList();
    }

    private ChemLabRecipe.Input input() {
        return new ChemLabRecipe.Input(inventory.getStackInSlot(SLOT_INPUT1), inventory.getStackInSlot(SLOT_INPUT2),
                inventory.getStackInSlot(SLOT_FLASK), inputTank.getFluid());
    }

    private boolean isFlask(ItemStack stack) {
        return recipes().stream().anyMatch(r -> r.flask().map(f -> f.ingredient().test(stack)).orElse(false));
    }

    @Override
    protected int upgradeSlot() {
        return SLOT_UPGRADE;
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot == SLOT_OUTPUT;
    }

    /** Techguns 2 {@code isItemValidForSlot}: flasks only in the flask slot, item inputs must still allow a recipe. */
    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        if (level == null) {
            return slot <= SLOT_FLASK;
        }
        ItemStack in1 = inventory.getStackInSlot(SLOT_INPUT1);
        ItemStack in2 = inventory.getStackInSlot(SLOT_INPUT2);
        return switch (slot) {
            case SLOT_FLASK -> isFlask(stack);
            case SLOT_INPUT1 -> !isFlask(stack) && (!in1.isEmpty() ? ItemStack.isSameItemSameComponents(in1, stack)
                    : in2.isEmpty() ? recipes().stream().anyMatch(r -> r.usesItem(stack))
                    : recipes().stream().anyMatch(r -> r.acceptsPair(in2, stack)));
            case SLOT_INPUT2 -> !isFlask(stack) && (!in2.isEmpty() ? ItemStack.isSameItemSameComponents(in2, stack)
                    : !in1.isEmpty() && recipes().stream().anyMatch(r -> r.acceptsPair(in1, stack)));
            default -> false;
        };
    }

    /** Techguns 2 {@code getValidSlotForItemInMachine} (shift click). */
    public int targetSlot(ItemStack stack) {
        for (int slot : new int[]{SLOT_FLASK, SLOT_INPUT1, SLOT_INPUT2, SLOT_UPGRADE}) {
            if (inventory.isItemValid(slot, stack)) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    protected void checkAndStartOperation() {
        ChemLabRecipe.Input input = input();
        for (ChemLabRecipe recipe : recipes()) {
            ChemLabRecipe.Match match = recipe.match(input);
            if (match == ChemLabRecipe.Match.NONE || !canOutput(recipe, 1)) {
                continue;
            }
            boolean swapped = match == ChemLabRecipe.Match.SWAPPED;
            int multiplier = 1;
            for (int i = maxStackMultiplier(); i > 1; i--) {
                if (canOutput(recipe, i) && canConsume(recipe, swapped, i)) {
                    multiplier = i;
                    break;
                }
            }
            consume(swapped ? SLOT_INPUT2 : SLOT_INPUT1, ChemLabRecipe.count(recipe.first()) * multiplier);
            consume(swapped ? SLOT_INPUT1 : SLOT_INPUT2, ChemLabRecipe.count(recipe.second()) * multiplier);
            consume(SLOT_FLASK, ChemLabRecipe.count(recipe.flask()) * multiplier);
            int fluidAmount = recipe.fluid().map(f -> f.amount()).orElse(0) * multiplier;
            if (fluidAmount > 0) {
                inputTank.drain(fluidAmount, IFluidHandler.FluidAction.EXECUTE);
            }
            pendingFluid = recipe.fluidResult().copyWithAmount(recipe.fluidResult().getAmount() * multiplier);
            contentsChanged = false;
            startOperation(new MachineOperation(List.of(recipe.result()), multiplier, TIME, recipe.powerPerTick()));
            return;
        }
    }

    private boolean canOutput(ChemLabRecipe recipe, int multiplier) {
        ItemStack item = recipe.result().copyWithCount(recipe.result().getCount() * multiplier);
        if (!canOutput(item, SLOT_OUTPUT)) {
            return false;
        }
        FluidStack fluid = recipe.fluidResult();
        if (fluid.isEmpty()) {
            return true;
        }
        FluidStack scaled = fluid.copyWithAmount(fluid.getAmount() * multiplier);
        return outputTank.fill(scaled, IFluidHandler.FluidAction.SIMULATE) == scaled.getAmount();
    }

    private boolean canConsume(ChemLabRecipe recipe, boolean swapped, int multiplier) {
        return has(swapped ? SLOT_INPUT2 : SLOT_INPUT1, ChemLabRecipe.count(recipe.first()) * multiplier)
                && has(swapped ? SLOT_INPUT1 : SLOT_INPUT2, ChemLabRecipe.count(recipe.second()) * multiplier)
                && has(SLOT_FLASK, ChemLabRecipe.count(recipe.flask()) * multiplier)
                && inputTank.getFluidAmount() >= recipe.fluid().map(f -> f.amount()).orElse(0) * multiplier;
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
        deliver(SLOT_OUTPUT, operation.output(0));
        if (!pendingFluid.isEmpty()) {
            outputTank.fill(pendingFluid, IFluidHandler.FluidAction.EXECUTE);
            pendingFluid = FluidStack.EMPTY;
        }
    }

    /** Techguns 2: two bubbling sounds per job, spell particles over the bench. */
    @Override
    protected void playWorkSound() {
        if (level == null) {
            return;
        }
        int half = Math.round(totalTime * 0.5f);
        if (progress == 1 || progress == 1 + half) {
            level.playLocalSound(worldPosition, TGSounds.CHEM_LAB_WORK.get(), SoundSource.BLOCKS, SOUND_VOLUME, 1.0f, true);
        }
        if (progress % 10 == 0) {
            level.addParticle(ParticleTypes.EFFECT, worldPosition.getX() + 0.5 + level.random.nextFloat(),
                    worldPosition.getY() + 0.5 + level.random.nextFloat(), worldPosition.getZ() + 0.5 + level.random.nextFloat(), 0, 1, 0);
        }
    }

    // --- fluids ---

    public FluidTank getInputTank() {
        return inputTank;
    }

    public FluidTank getOutputTank() {
        return outputTank;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public boolean isDrainInput() {
        return drainInput;
    }

    @Override
    public boolean canUseItem(ItemStack stack) {
        return stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    /** Buckets and other containers: drain the output tank into them, or empty them into the input tank. */
    @Override
    public boolean useItem(Player player, InteractionHand hand) {
        return FluidUtil.interactWithFluidHandler(player, hand, fluidHandler);
    }

    @Override
    protected boolean pressMachineButton(Player player, int id) {
        switch (id) {
            case BUTTON_DUMP_INPUT -> inputTank.setFluid(FluidStack.EMPTY);
            case BUTTON_DUMP_OUTPUT -> outputTank.setFluid(FluidStack.EMPTY);
            case BUTTON_TOGGLE_DRAIN -> drainInput = !drainInput;
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    protected int extraDataCount() {
        return 5;
    }

    @Override
    protected int getExtraData(int index) {
        return switch (index) {
            case DATA_INPUT_FLUID -> fluidId(inputTank.getFluid());
            case DATA_INPUT_AMOUNT -> inputTank.getFluidAmount();
            case DATA_OUTPUT_FLUID -> fluidId(outputTank.getFluid());
            case DATA_OUTPUT_AMOUNT -> outputTank.getFluidAmount();
            case DATA_DRAIN_INPUT -> drainInput ? 1 : 0;
            default -> 0;
        };
    }

    /** Fluids are synced to the screen as registry ids (the data slots carry numbers only). */
    private static int fluidId(FluidStack stack) {
        return stack.isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(stack.getFluid());
    }

    public static Fluid fluidById(int id) {
        return id < 0 ? net.minecraft.world.level.material.Fluids.EMPTY : BuiltInRegistries.FLUID.byId(id);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("input_tank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("output_tank", outputTank.writeToNBT(registries, new CompoundTag()));
        if (!pendingFluid.isEmpty()) {
            tag.put("pending_fluid", pendingFluid.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inputTank.readFromNBT(registries, tag.getCompound("input_tank"));
        outputTank.readFromNBT(registries, tag.getCompound("output_tank"));
        pendingFluid = FluidStack.parseOptional(registries, tag.getCompound("pending_fluid"));
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        tag.putBoolean("drain_input", drainInput);
    }

    @Override
    protected void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        drainInput = tag.getBoolean("drain_input");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ChemLabMenu(id, inventory, this);
    }

    /** Techguns 2 {@code ChemLabFluidHandler}: fill the input tank, drain the output (or input) tank. */
    private final class TankHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? inputTank.getFluid() : outputTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? INPUT_TANK_CAPACITY : OUTPUT_TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return inputTank.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return (drainInput ? inputTank : outputTank).drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return (drainInput ? inputTank : outputTank).drain(maxDrain, action);
        }
    }
}
