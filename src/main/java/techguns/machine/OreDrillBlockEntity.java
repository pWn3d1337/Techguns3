package techguns.machine;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import techguns.TGConfig;
import techguns.item.OreDrillHeadItem;
import techguns.multiblock.MultiblockMasterBlockEntity;
import techguns.multiblock.MultiblockSchematic;
import techguns.recipe.OreClusterRecipe;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGRecipes;
import techguns.registry.TGSounds;
import techguns.registry.TGTags;

/**
 * Ore Drill (Techguns 2 {@code OreDrillTileEntMaster}): drills the ore cluster at the end of its rods. The cluster's
 * {@link OreClusterRecipe} gives a random ore (or a bucket of oil) per operation; bigger drills, bigger clusters and
 * better heads mine faster. A head too weak for the cluster only gives cobblestone. Power comes from furnace fuel or
 * liquid fuel in the input tank (an internal fuel buffer worth {@code oreDrillFuelMultiplier} FE per unit), else FE.
 */
public class OreDrillBlockEntity extends MultiblockMasterBlockEntity {
    public static final int SLOT_DRILL = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT_FIRST = 2;
    public static final int SLOT_OUTPUT_LAST = 10;

    public static final int ENERGY_CAPACITY = 500000;
    public static final int INPUT_TANK_CAPACITY = 16000;
    public static final int OUTPUT_TANK_CAPACITY = 32000;
    /** Techguns 2: lava gives the fuel of a lava bucket per bucket. */
    public static final float LAVA_FUEL_VALUE = 20.0f;
    private static final float SOUND_VOLUME = 0.5f;
    /** Work sound length (3.1 s). */
    private static final int SOUND_LOOP = 62;

    public static final int DATA_INPUT_FLUID = DATA_COUNT;
    public static final int DATA_INPUT_AMOUNT = DATA_COUNT + 1;
    public static final int DATA_OUTPUT_FLUID = DATA_COUNT + 2;
    public static final int DATA_OUTPUT_AMOUNT = DATA_COUNT + 3;
    public static final int DATA_FUEL_LOW = DATA_COUNT + 4;
    public static final int DATA_FUEL_HIGH = DATA_COUNT + 5;
    public static final int DATA_FUEL_MAX_LOW = DATA_COUNT + 6;
    public static final int DATA_FUEL_MAX_HIGH = DATA_COUNT + 7;
    public static final int DATA_POWER_LOW = DATA_COUNT + 8;
    public static final int DATA_POWER_HIGH = DATA_COUNT + 9;
    /** The head's mining level on this drill, -1 without a fitting head. */
    public static final int DATA_HEAD_LEVEL = DATA_COUNT + 10;

    private final FluidTank inputTank = new FluidTank(INPUT_TANK_CAPACITY, stack -> fuelValue(stack) > 0) {
        @Override
        protected void onContentsChanged() {
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

    /** The drill's shape, set when formed. */
    private int engines;
    private int rods;
    private int radius;
    private Direction drillDirection = Direction.DOWN;

    private int fuelBuffer;
    private int fuelBufferMax;
    /** Fluid result of the running operation, added to the output tank when it finishes. */
    private FluidStack pendingFluid = FluidStack.EMPTY;
    /** The head the running operation was started with; another head cancels it (Techguns 2 {@code checkDrill}). */
    private ItemStack operationDrill = ItemStack.EMPTY;
    /** Client: the head's mining level for the renderer (0 = no head). */
    private int drillType;
    private int soundDelay;

    public OreDrillBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.ORE_DRILL.get(), pos, state, 11, ENERGY_CAPACITY);
    }

    @Override
    public MultiblockSchematic schematic() {
        return OreDrillSchematic.INSTANCE;
    }

    // --- shape ---

    @Override
    public boolean tryForm(Player player, Direction side) {
        Optional<OreDrillSchematic.Layout> layout = level == null ? Optional.empty() : OreDrillSchematic.layout(level, worldPosition);
        if (!super.tryForm(player, side)) {
            return false;
        }
        layout.ifPresent(l -> {
            engines = l.engines();
            rods = l.rods();
            radius = l.radius();
            drillDirection = l.direction();
        });
        contentsChanged = true;
        sendUpdate();
        return true;
    }

    @Override
    public void unform() {
        super.unform();
        operation = null;
        progress = 0;
        totalTime = 0;
        pendingFluid = FluidStack.EMPTY;
        engines = 0;
        rods = 0;
        radius = 0;
        setChanged();
        sendUpdate();
    }

    public OreDrillSchematic.Layout layout() {
        return new OreDrillSchematic.Layout(drillDirection, engines, rods, radius);
    }

    /** Techguns 2 {@code getDrillRadius}: 0 for the mini drill, 1 for 3x3 drills ... 4 for 7x7 drills. */
    public int drillRadius() {
        return layout().drillRadius();
    }

    public int getEngines() {
        return engines;
    }

    public int getRods() {
        return rods;
    }

    public int getRadius() {
        return radius;
    }

    public Direction getDrillDirection() {
        return drillDirection;
    }

    /** Client: 0 without a head, else the head's mining level (steel 1, obsidian steel 2, carbon 3). */
    public int getDrillType() {
        return drillType;
    }

    // --- slots ---

    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_DRILL -> stack.getItem() instanceof OreDrillHeadItem;
            case SLOT_FUEL -> stack.getBurnTime(RecipeType.SMELTING) > 0;
            default -> false;
        };
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot >= SLOT_OUTPUT_FIRST && slot <= SLOT_OUTPUT_LAST;
    }

    public int targetSlot(ItemStack stack) {
        return stack.getItem() instanceof OreDrillHeadItem ? SLOT_DRILL : SLOT_FUEL;
    }

    /** The mining level of the head on this drill, -1 without a fitting head. */
    public int headMiningLevel() {
        return isFormed() ? OreDrillHeadItem.miningLevel(inventory.getStackInSlot(SLOT_DRILL), drillRadius()) : -1;
    }

    // --- operation ---

    @Nullable
    private OreClusterRecipe clusterRecipe(Block block) {
        if (level == null) {
            return null;
        }
        return level.getRecipeManager().getAllRecipesFor(TGRecipes.ORE_CLUSTER.get()).stream().map(RecipeHolder::value)
                .filter(recipe -> recipe.cluster() == block).findFirst().orElse(null);
    }

    /** Techguns 2 {@code getClusterSize}: connected blocks of the same cluster, up to {@code limit}. */
    private int clusterSize(BlockPos start, BlockState cluster, int limit) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> open = new ArrayDeque<>();
        open.add(start);
        visited.add(start);
        while (!open.isEmpty() && visited.size() < limit) {
            BlockPos pos = open.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!visited.contains(next) && level.getBlockState(next) == cluster) {
                    visited.add(next);
                    open.add(next);
                }
            }
        }
        return Math.min(visited.size(), limit);
    }

    /** Techguns 2 {@code checkAndStartOperation}. */
    @Override
    protected void checkAndStartOperation() {
        if (level == null || !isFormed()) {
            return;
        }
        BlockPos target = layout().clusterPos(worldPosition);
        BlockState cluster = level.getBlockState(target);
        OreClusterRecipe recipe = cluster.is(TGTags.Blocks.ORE_CLUSTERS) ? clusterRecipe(cluster.getBlock()) : null;
        if (recipe == null) {
            // the cluster is gone: the drill breaks apart
            unform();
            return;
        }
        int headLevel = headMiningLevel();
        int oldType = drillType;
        drillType = Math.max(headLevel, 0);
        if (headLevel <= 0) {
            if (oldType != drillType) {
                sendUpdate();
            }
            return;
        }
        int effectiveLevel = headLevel + drillRadius() - recipe.miningLevel();
        double powerMultiplier = TGConfig.ORE_DRILL_MULTIPLIER_POWER.get();
        ItemStack output;
        FluidStack fluid = FluidStack.EMPTY;
        int time;
        int power;
        if (effectiveLevel >= 0) {
            int size = clusterSize(target, cluster, layout().length());
            double orePerHour = (size * 3 + size * effectiveLevel * 0.5) * TGConfig.ORE_DRILL_MULTIPLIER_ORES.get() * recipe.oreMultiplier();
            OreClusterRecipe.Entry entry = recipe.pick(level.random);
            output = entry == null ? new ItemStack(Blocks.COBBLESTONE) : entry.itemOutput();
            if (entry != null && output.isEmpty()) {
                fluid = entry.fluidOutput();
            }
            time = Math.max(1, (int) (60.0 / orePerHour * 1200.0));
            power = (int) (8 * orePerHour * (1 + Math.max(drillRadius() - 1, 0) * 0.2) * recipe.powerMultiplier() * powerMultiplier);
        } else {
            // the head is too weak for this cluster
            output = new ItemStack(Blocks.COBBLESTONE);
            time = 20 * 20;
            power = (int) (24 * powerMultiplier);
        }
        // Techguns 2 dropped what did not fit; here the drill waits for room
        if (!canInsertAll(SLOT_OUTPUT_FIRST, SLOT_OUTPUT_LAST, List.of(output))
                || (!fluid.isEmpty() && outputTank.fill(fluid, IFluidHandler.FluidAction.SIMULATE) < fluid.getAmount())) {
            if (oldType != drillType) {
                sendUpdate();
            }
            return;
        }
        pendingFluid = fluid;
        operationDrill = inventory.getStackInSlot(SLOT_DRILL).copy();
        startOperation(new MachineOperation(List.of(output), 1, time, power));
    }

    @Override
    protected boolean keepOperation() {
        return isFormed() && ItemStack.isSameItemSameComponents(operationDrill, inventory.getStackInSlot(SLOT_DRILL));
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
        insertAll(SLOT_OUTPUT_FIRST, SLOT_OUTPUT_LAST, List.of(operation.output(0)), false).stream().filter(stack -> !stack.isEmpty())
                .forEach(stack -> Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5, stack));
        if (!pendingFluid.isEmpty()) {
            outputTank.fill(pendingFluid, IFluidHandler.FluidAction.EXECUTE);
            pendingFluid = FluidStack.EMPTY;
        }
    }

    /** Techguns 2 {@code consumePower}: the fuel buffer first, refilled from furnace fuel or the input tank, then FE. */
    @Override
    protected boolean consumePower(int amount) {
        if (TGConfig.MACHINES_NEED_NO_POWER.get() || amount <= 0) {
            return true;
        }
        float factor = TGConfig.ORE_DRILL_MULTIPLIER_FUEL.get().floatValue();
        if (fuelBuffer * factor >= amount) {
            useFuel(amount, factor);
            return true;
        }
        amount -= (int) (fuelBuffer * factor);
        fuelBuffer = 0;
        fuelBufferMax = 0;
        ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);
        int burnTime = fuel.isEmpty() ? 0 : fuel.getBurnTime(RecipeType.SMELTING);
        if (burnTime > 0) {
            ItemStack container = fuel.getCraftingRemainingItem();
            consume(SLOT_FUEL, 1);
            if (!container.isEmpty()) {
                insertAll(SLOT_OUTPUT_FIRST, SLOT_OUTPUT_LAST, List.of(container), false).stream().filter(stack -> !stack.isEmpty())
                        .forEach(stack -> Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                                worldPosition.getZ() + 0.5, stack));
            }
            fuelBuffer = burnTime;
            fuelBufferMax = burnTime;
        } else {
            float value = fuelValue(inputTank.getFluid());
            if (value > 0) {
                FluidStack drained = inputTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                fuelBuffer = (int) (drained.getAmount() * value);
                fuelBufferMax = fuelBuffer;
            }
        }
        setChanged();
        if (fuelBuffer * factor >= amount) {
            useFuel(amount, factor);
            return true;
        }
        return energy.consume(amount);
    }

    private void useFuel(int amount, float factor) {
        fuelBuffer -= (int) Math.max(amount / factor, 1);
        fuelBuffer = Math.max(fuelBuffer, 0);
        setChanged();
    }

    /** Fuel units per millibucket (Techguns 2 {@code getFuelValueFromInputTank}). */
    public static float fuelValue(FluidStack stack) {
        if (stack.isEmpty()) {
            return 0.0f;
        }
        if (stack.is(FluidTags.LAVA)) {
            return LAVA_FUEL_VALUE;
        }
        return stack.is(TGTags.Fluids.FUEL) ? TGConfig.ORE_DRILL_FUEL_VALUE_FUEL.get().floatValue() : 0.0f;
    }

    public int getFuelBuffer() {
        return fuelBuffer;
    }

    public int getFuelBufferMax() {
        return fuelBufferMax;
    }

    // --- client ---

    /** Techguns 2 {@code playAmbientSound}: the work loop by drill size, from the middle of the rods. */
    @Override
    protected void playWorkSound() {
        if (level == null || soundDelay-- > 0) {
            return;
        }
        soundDelay = SOUND_LOOP - 1;
        SoundEvent sound = (radius == 0 ? TGSounds.ORE_DRILL_WORK_SMALL : radius <= 2 ? TGSounds.ORE_DRILL_WORK_MEDIUM
                : TGSounds.ORE_DRILL_WORK_LARGE).get();
        BlockPos soundPos = worldPosition.relative(drillDirection, engines + rods / 2);
        level.playLocalSound(soundPos, sound, SoundSource.BLOCKS, SOUND_VOLUME, 1.0f, false);
    }

    // --- fluids ---

    public FluidTank getInputTank() {
        return inputTank;
    }

    public FluidTank getOutputTank() {
        return outputTank;
    }

    @Override
    public IFluidHandler getMultiblockFluidHandler() {
        return fluidHandler;
    }

    @Override
    public boolean canUseItem(ItemStack stack) {
        return stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    /** Buckets: drain the output tank (oil) into them, or empty fuel into the input tank. */
    @Override
    public boolean useItem(Player player, InteractionHand hand) {
        return FluidUtil.interactWithFluidHandler(player, hand, fluidHandler);
    }

    // --- menu data ---

    @Override
    protected int extraDataCount() {
        return 11;
    }

    @Override
    protected int getExtraData(int index) {
        int power = operation != null ? operation.powerPerTick() : 0;
        return switch (index) {
            case DATA_INPUT_FLUID -> ChemLabBlockEntity.fluidId(inputTank.getFluid());
            case DATA_INPUT_AMOUNT -> inputTank.getFluidAmount();
            case DATA_OUTPUT_FLUID -> ChemLabBlockEntity.fluidId(outputTank.getFluid());
            case DATA_OUTPUT_AMOUNT -> outputTank.getFluidAmount();
            case DATA_FUEL_LOW -> fuelBuffer & 0xFFFF;
            case DATA_FUEL_HIGH -> fuelBuffer >>> 16;
            case DATA_FUEL_MAX_LOW -> fuelBufferMax & 0xFFFF;
            case DATA_FUEL_MAX_HIGH -> fuelBufferMax >>> 16;
            case DATA_POWER_LOW -> power & 0xFFFF;
            case DATA_POWER_HIGH -> power >>> 16;
            case DATA_HEAD_LEVEL -> headMiningLevel();
            default -> 0;
        };
    }

    // --- saving and syncing ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("input_tank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("output_tank", outputTank.writeToNBT(registries, new CompoundTag()));
        if (!pendingFluid.isEmpty()) {
            tag.put("pending_fluid", pendingFluid.save(registries));
        }
        if (!operationDrill.isEmpty()) {
            tag.put("operation_drill", operationDrill.save(registries));
        }
        tag.putInt("fuel_buffer", fuelBuffer);
        tag.putInt("fuel_buffer_max", fuelBufferMax);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inputTank.readFromNBT(registries, tag.getCompound("input_tank"));
        outputTank.readFromNBT(registries, tag.getCompound("output_tank"));
        pendingFluid = FluidStack.parseOptional(registries, tag.getCompound("pending_fluid"));
        operationDrill = ItemStack.parseOptional(registries, tag.getCompound("operation_drill"));
        fuelBuffer = tag.getInt("fuel_buffer");
        fuelBufferMax = tag.getInt("fuel_buffer_max");
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        tag.putByte("engines", (byte) engines);
        tag.putByte("rods", (byte) rods);
        tag.putByte("radius", (byte) radius);
        tag.putByte("drill_direction", (byte) drillDirection.get3DDataValue());
        tag.putByte("drill_type", (byte) drillType);
    }

    @Override
    protected void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        engines = tag.getByte("engines");
        rods = tag.getByte("rods");
        radius = tag.getByte("radius");
        drillDirection = Direction.from3DDataValue(tag.getByte("drill_direction"));
        drillType = tag.getByte("drill_type");
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("techguns.container.oredrill");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new OreDrillMenu(id, inventory, this);
    }

    /** Techguns 2 {@code OreDrillFluidHandler}: fuel goes into the input tank, oil comes out of the output tank. */
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
            return tank == 0 && fuelValue(stack) > 0;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return inputTank.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return outputTank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return outputTank.drain(maxDrain, action);
        }
    }
}
