package techguns.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import techguns.TGIds;
import techguns.multiblock.MultiblockMasterBlockEntity;
import techguns.multiblock.MultiblockSchematic;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;
import techguns.recipe.ReactionChamberRecipe;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGItems;
import techguns.registry.TGRecipes;
import techguns.registry.TGSounds;
import techguns.registry.TGTags;

/**
 * Reaction Chamber (Techguns 2 {@code ReactionChamberTileEntMaster} and {@code ReactionChamberOperation}): an input
 * item reacts under a beam focus in the chamber's tank. Liquid level and intensity are set with buttons; the tank
 * fills only up to the liquid level. Every 60 ticks a reaction tick uses the recipe's power and counts as completion
 * at the required intensity (which may drift with the recipe's instability). Enough completion gives the outputs and
 * uses some liquid; running out of time fails with the recipe's risk: the item is lost, or the chamber explodes.
 * Every reaction tick without progress irradiates the area around the controller (Techguns 2 {@code doRadiation}).
 */
public class ReactionChamberBlockEntity extends MultiblockMasterBlockEntity {
    public static final int SLOT_INPUT = ReactionChamberRecipe.SLOT_INPUT;
    public static final int SLOT_FOCUS = ReactionChamberRecipe.SLOT_FOCUS;
    private static final float RADIATION_FACTOR_FAILURE = 0.5f;
    private static final float RADIATION_FACTOR_SUCCESS = 0.0f;
    private static final double RADIATION_RADIUS = 6.0;
    private static final double RADIATION_INNER_RADIUS = 4.0;
    private static final int RADIATION_DURATION = 62;
    public static final int SLOT_OUTPUT_FIRST = 2;
    public static final int SLOT_OUTPUT_LAST = 5;

    public static final int ENERGY_CAPACITY = 1000000;
    public static final int TANK_CAPACITY = ReactionChamberRecipe.MAX_LEVEL * ReactionChamberRecipe.BUCKET;
    /** Game ticks per reaction tick (Techguns 2 {@code RECIPE_TICKRATE}). */
    public static final int REACTION_TICK = 60;

    public static final int BUTTON_LIQUID_UP = BUTTON_MACHINE;
    public static final int BUTTON_LIQUID_DOWN = BUTTON_MACHINE + 1;
    public static final int BUTTON_INTENSITY_UP = BUTTON_MACHINE + 2;
    public static final int BUTTON_INTENSITY_DOWN = BUTTON_MACHINE + 3;
    public static final int BUTTON_DUMP = BUTTON_MACHINE + 4;

    public static final int DATA_FLUID = DATA_COUNT;
    public static final int DATA_AMOUNT = DATA_COUNT + 1;
    public static final int DATA_LIQUID_LEVEL = DATA_COUNT + 2;
    public static final int DATA_INTENSITY = DATA_COUNT + 3;
    public static final int DATA_COMPLETION = DATA_COUNT + 4;
    public static final int DATA_REQUIRED_COMPLETION = DATA_COUNT + 5;
    public static final int DATA_REQUIRED_INTENSITY = DATA_COUNT + 6;
    public static final int DATA_REACTING = DATA_COUNT + 7;

    private final ReactionTank tank = new ReactionTank();
    private int intensity;
    private int liquidLevel;

    /** The running reaction (server), or null. */
    @Nullable
    private ResourceLocation reactionId;
    private int completion;
    private int requiredIntensity;
    private int nextTick;
    private boolean fluidsChanged;
    /** A recipe matches but the buffer lacks one reaction tick of power: retry every tick. */
    private boolean waitingForPower;
    /** Client: a reaction runs; the item being worked on. */
    private boolean reacting;
    private ItemStack reactingItem = ItemStack.EMPTY;
    /** Client: the input and focus slots for the renderer (the menu syncs the real slots). */
    private ItemStack clientInput = ItemStack.EMPTY;
    private ItemStack clientFocus = ItemStack.EMPTY;

    public ReactionChamberBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.REACTION_CHAMBER.get(), pos, state, 6, ENERGY_CAPACITY);
    }

    @Override
    public MultiblockSchematic schematic() {
        return ReactionChamberSchematic.INSTANCE;
    }

    /** The structure direction: from the controller into the chamber. */
    public Direction chamberDirection() {
        return ReactionChamberSchematic.structureDirection(getDirection());
    }

    /** The bottom centre block of the chamber. */
    public BlockPos centerPos() {
        return worldPosition.relative(chamberDirection());
    }

    @Override
    public VoxelShape partShape(BlockPos pos) {
        return ReactionChamberSchematic.partShape(worldPosition, chamberDirection(), pos);
    }

    @Override
    public IFluidHandler getMultiblockFluidHandler() {
        return tank;
    }

    public ReactionTank getTank() {
        return tank;
    }

    // --- slots ---

    public static boolean isFocus(ItemStack stack) {
        return stack.is(TGTags.Items.REACTION_CHAMBER_FOCUSES);
    }

    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        return slot == SLOT_INPUT ? !isFocus(stack) : slot == SLOT_FOCUS && isFocus(stack);
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot >= SLOT_OUTPUT_FIRST && slot <= SLOT_OUTPUT_LAST;
    }

    public int targetSlot(ItemStack stack) {
        return isFocus(stack) ? SLOT_FOCUS : SLOT_INPUT;
    }

    // --- reaction ---

    @Override
    protected void checkAndStartOperation() {
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
    }

    @Nullable
    private ReactionChamberRecipe reaction() {
        if (reactionId == null || level == null) {
            return null;
        }
        return level.getRecipeManager().byKey(reactionId).map(RecipeHolder::value)
                .filter(ReactionChamberRecipe.class::isInstance).map(ReactionChamberRecipe.class::cast).orElse(null);
    }

    public boolean isReacting() {
        return level != null && level.isClientSide ? reacting : reactionId != null;
    }

    @Override
    public boolean isWorking() {
        return isReacting();
    }

    /** The reaction runs here instead of the generic job (Techguns 2 {@code update}). */
    @Override
    protected boolean idleTick() {
        if (fluidsChanged) {
            fluidsChanged = false;
            sendUpdate();
        }
        ReactionChamberRecipe recipe = reaction();
        if (reactionId != null && recipe == null) {
            endReaction();
        }
        if (recipe == null) {
            if (contentsChanged || waitingForPower) {
                boolean changed = contentsChanged;
                contentsChanged = false;
                tryStart();
                if (changed) {
                    // the renderer shows the input and the focus
                    sendUpdate();
                }
            }
            return true;
        }
        progress++;
        if (--nextTick > 0 || progress > totalTime) {
            return true;
        }
        nextTick = REACTION_TICK;
        reactionTick(recipe);
        if (completion >= recipe.requiredCompletion()) {
            succeed(recipe);
        } else if (progress >= totalTime) {
            fail(recipe);
        }
        setChanged();
        sendUpdate();
        return true;
    }

    /** Techguns 2 {@code checkAndStartOperation}: needs the recipe's power for one reaction tick. */
    private void tryStart() {
        if (level == null) {
            return;
        }
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        ItemStack focus = inventory.getStackInSlot(SLOT_FOCUS);
        Optional<RecipeHolder<ReactionChamberRecipe>> found = level.getRecipeManager().getAllRecipesFor(TGRecipes.REACTION_CHAMBER.get())
                .stream().filter(h -> h.value().canStart(input, focus, tank.getFluid(), liquidLevel, intensity)).findFirst();
        waitingForPower = false;
        if (found.isEmpty()) {
            return;
        }
        ReactionChamberRecipe recipe = found.get().value();
        if (!consumePowerCheck(recipe.powerPerTick())) {
            waitingForPower = true;
            return;
        }
        reactionId = found.get().id();
        reactingItem = input.copyWithCount(1);
        consume(SLOT_INPUT, 1);
        contentsChanged = false;
        completion = 0;
        requiredIntensity = recipe.intensity();
        nextTick = REACTION_TICK;
        progress = 0;
        totalTime = recipe.ticks() * REACTION_TICK;
        setChanged();
        sendUpdate();
    }

    private boolean consumePowerCheck(int amount) {
        return techguns.TGConfig.MACHINES_NEED_NO_POWER.get() || energy.getEnergyStored() >= amount;
    }

    /** Techguns 2 {@code ReactionChamberOperation.tick}. */
    private void reactionTick(ReactionChamberRecipe recipe) {
        boolean powered = consumePower(recipe.powerPerTick());
        boolean focusMatches = recipe.focus().test(inventory.getStackInSlot(SLOT_FOCUS));
        boolean progressing = powered && focusMatches && requiredIntensity == intensity && recipe.liquidLevel() == liquidLevel;
        if (progressing) {
            completion++;
        }
        radiate(progressing);
        if (!focusMatches) {
            // a removed or wrong focus ends the reaction at once, without completion
            progress = totalTime;
            completion = 0;
        } else if (recipe.instability() > 0.0f && level.random.nextFloat() < recipe.instability()) {
            int change = 1 + (recipe.intensityMargin() > 1 ? level.random.nextInt(recipe.intensityMargin()) : 0);
            requiredIntensity = Math.clamp(requiredIntensity + (level.random.nextBoolean() ? change : -change), 0, ReactionChamberRecipe.MAX_LEVEL);
        }
    }

    /**
     * Techguns 2 {@code doRadiation} with the {@code ReactionBeamFocus} defaults every focus used: level
     * ceil(intensity / 2) on ticks without progress (none on good ticks) for 62 ticks, full within 4 blocks of the
     * controller, fading to nothing at 6.
     */
    private void radiate(boolean progressing) {
        int radiation = (int) Math.ceil((progressing ? RADIATION_FACTOR_SUCCESS : RADIATION_FACTOR_FAILURE) * intensity);
        if (radiation > 0) {
            techguns.radiation.RadiationSources.irradiate(level, worldPosition.getCenter(), RADIATION_RADIUS, RADIATION_DURATION, radiation - 1,
                    RADIATION_INNER_RADIUS, 0);
        }
    }

    /** Techguns 2 {@code mergeOutput}: outputs into the four output slots, the rest drops; uses the recipe's liquid. */
    private void succeed(ReactionChamberRecipe recipe) {
        insertAll(SLOT_OUTPUT_FIRST, SLOT_OUTPUT_LAST, recipe.outputs(), false).forEach(rest ->
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, rest));
        if (recipe.liquidConsumption() > 0) {
            tank.removeInternal(recipe.liquidConsumption());
        }
        endReaction();
        tryStart();
    }

    private void fail(ReactionChamberRecipe recipe) {
        endReaction();
        switch (recipe.risk()) {
            case EXPLOSION_LOW -> explode(false);
            case EXPLOSION_MEDIUM -> explode(true);
            default -> tryStart();
        }
    }

    private void endReaction() {
        reactionId = null;
        reactingItem = ItemStack.EMPTY;
        progress = 0;
        totalTime = 0;
        completion = 0;
    }

    /**
     * Techguns 2 {@code explode}: the chamber unforms; a small explosion blows the centre glass and its four
     * neighbours away and spills the liquid, a medium one destroys all glass with a real explosion.
     */
    private void explode(boolean medium) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction dir = chamberDirection();
        BlockState fluidBlock = tank.getFluid().isEmpty() ? null : tank.getFluid().getFluid().defaultFluidState().createLegacyBlock();
        BlockPos center = centerPos().above();
        unform();
        List<BlockPos> remove = new ArrayList<>();
        remove.add(center);
        if (!medium) {
            for (Direction horizontal : Direction.Plane.HORIZONTAL) {
                remove.add(center.relative(horizontal));
            }
        } else {
            Direction left = dir.getClockWise();
            BlockPos.betweenClosed(worldPosition.relative(left).above(), worldPosition.relative(left.getOpposite()).relative(dir, 2).above(2))
                    .forEach(pos -> remove.add(pos.immutable()));
        }
        remove.forEach(pos -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3));
        double x = center.getX() + 0.5;
        double y = center.getY() + 0.5;
        double z = center.getZ() + 0.5;
        if (!medium) {
            TGNetwork.sendFxNear(serverLevel, x, y, z, 50.0, new FxPayloads.SpawnFx("FragGrenadeExplosion", x, y, z));
            level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0f,
                    (1.0f + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f) * 0.7f);
        } else {
            level.explode(null, x, y, z, 4.0f, Level.ExplosionInteraction.BLOCK);
        }
        if (fluidBlock != null) {
            level.setBlock(center, fluidBlock, 3);
            if (medium) {
                level.setBlock(center.above(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(center.below(), Blocks.GRAVEL.defaultBlockState(), 3);
            }
        }
        energy.setEnergy(0);
        tank.setFluid(FluidStack.EMPTY);
        ItemStack content = inventory.getStackInSlot(SLOT_INPUT);
        if (!content.isEmpty()) {
            inventory.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
            Containers.dropItemStack(level, x, y, z, content);
        }
        setChanged();
        sendUpdate();
    }

    // --- client effects ---

    @Override
    protected void clientIdleTick() {
        if (!reacting || level == null || !isFormed()) {
            return;
        }
        progress++;
        // like Techguns 2, the client plays a reaction tick one tick after the server's reaction tick update (which
        // resets next_tick) arrived, so the beep or warning already sees a required intensity that just changed
        if (--nextTick != REACTION_TICK - 1) {
            return;
        }
        // Techguns 2 playReactionTickSound: focus sound, spell particles, beep at the right intensity or warning
        BlockPos pos = centerPos();
        ItemStack focus = clientFocus;
        if (!focus.isEmpty()) {
            level.playLocalSound(pos, focus.is(TGItems.UV_EMITTER.get()) ? TGSounds.CHEM_LAB_WORK.get() : TGSounds.REACTION_CHAMBER_HEATRAY_WORK.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f, true);
        }
        for (int i = 0; i < 4; i++) {
            level.addParticle(ParticleTypes.EFFECT, pos.getX() + level.random.nextFloat(), pos.getY() + level.random.nextFloat(),
                    pos.getZ() + level.random.nextFloat(), 0, 1, 0);
        }
        level.playLocalSound(pos, requiredIntensity == intensity ? TGSounds.REACTION_CHAMBER_BEEP.get() : TGSounds.REACTION_CHAMBER_WARNING.get(),
                SoundSource.BLOCKS, 1.0f, 1.0f, true);
    }

    // --- buttons, buckets ---

    @Override
    protected boolean pressMachineButton(Player player, int id) {
        switch (id) {
            case BUTTON_LIQUID_UP -> liquidLevel = Math.min(ReactionChamberRecipe.MAX_LEVEL, liquidLevel + 1);
            case BUTTON_LIQUID_DOWN -> liquidLevel = Math.max(0, liquidLevel - 1);
            case BUTTON_INTENSITY_UP -> intensity = Math.min(ReactionChamberRecipe.MAX_LEVEL, intensity + 1);
            case BUTTON_INTENSITY_DOWN -> intensity = Math.max(0, intensity - 1);
            case BUTTON_DUMP -> tank.setFluid(FluidStack.EMPTY);
            default -> {
                return false;
            }
        }
        return true;
    }

    public void setLiquidLevel(int liquidLevel) {
        this.liquidLevel = liquidLevel;
        contentsChanged = true;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
        contentsChanged = true;
    }

    @Override
    public boolean canUseItem(ItemStack stack) {
        return stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    @Override
    public boolean useItem(Player player, InteractionHand hand) {
        return FluidUtil.interactWithFluidHandler(player, hand, tank);
    }

    // --- getters, sync ---

    public int getIntensity() {
        return intensity;
    }

    public int getLiquidLevel() {
        return liquidLevel;
    }

    public int getCompletion() {
        return completion;
    }

    public int getRequiredIntensity() {
        return requiredIntensity;
    }

    /** The item being worked on, else the input slot (client: for the renderer). */
    public ItemStack getShownItem() {
        if (isReacting() && !reactingItem.isEmpty()) {
            return reactingItem;
        }
        return level != null && level.isClientSide ? clientInput : inventory.getStackInSlot(SLOT_INPUT);
    }

    /** The focus in use (client: for the renderer and sounds). */
    public ItemStack getFocus() {
        return level != null && level.isClientSide ? clientFocus : inventory.getStackInSlot(SLOT_FOCUS);
    }

    @Override
    protected int extraDataCount() {
        return 8;
    }

    @Override
    protected int getExtraData(int index) {
        ReactionChamberRecipe recipe = reaction();
        return switch (index) {
            case DATA_FLUID -> ChemLabBlockEntity.fluidId(tank.getFluid());
            case DATA_AMOUNT -> tank.getFluidAmount();
            case DATA_LIQUID_LEVEL -> liquidLevel;
            case DATA_INTENSITY -> intensity;
            case DATA_COMPLETION -> completion;
            case DATA_REQUIRED_COMPLETION -> recipe == null ? 0 : recipe.requiredCompletion();
            case DATA_REQUIRED_INTENSITY -> requiredIntensity;
            case DATA_REACTING -> recipe == null ? 0 : 1;
            default -> 0;
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("techguns.container.reactionchamber");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        if (reactionId != null) {
            tag.putString("reaction", reactionId.toString());
            tag.putInt("completion", completion);
            tag.putInt("next_tick", nextTick);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tank.readFromNBT(registries, tag.getCompound("tank"));
        reactionId = tag.contains("reaction") ? TGIds.tryParse(tag.getString("reaction")) : null;
        // the reacting item is client data, but the server sends it on: keep it over a reload
        reactingItem = ItemStack.parseOptional(registries, tag.getCompound("reacting_item"));
        completion = tag.getInt("completion");
        nextTick = tag.contains("next_tick") ? tag.getInt("next_tick") : REACTION_TICK;
    }

    /** Liquid, levels, the reaction state and the shown items for the renderer (Techguns 2 client data). */
    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        tag.putInt("intensity", intensity);
        tag.putInt("liquid_level", liquidLevel);
        tag.putInt("required_intensity", requiredIntensity);
        tag.putBoolean("reacting", reactionId != null);
        tag.putInt("next_tick", nextTick);
        if (level != null) {
            tag.put("tank_client", tank.writeToNBT(level.registryAccess(), new CompoundTag()));
            if (!reactingItem.isEmpty()) {
                tag.put("reacting_item", reactingItem.save(level.registryAccess()));
            }
            ItemStack focus = inventory.getStackInSlot(SLOT_FOCUS);
            if (!focus.isEmpty()) {
                tag.put("focus", focus.save(level.registryAccess()));
            }
            ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
            if (!input.isEmpty()) {
                tag.put("input", input.save(level.registryAccess()));
            }
        }
    }

    @Override
    protected void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        intensity = tag.getInt("intensity");
        liquidLevel = tag.getInt("liquid_level");
        requiredIntensity = tag.getInt("required_intensity");
        reacting = tag.getBoolean("reacting");
        nextTick = tag.getInt("next_tick");
        if (level != null && level.isClientSide) {
            HolderLookup.Provider registries = level.registryAccess();
            tank.readFromNBT(registries, tag.getCompound("tank_client"));
            reactingItem = ItemStack.parseOptional(registries, tag.getCompound("reacting_item"));
            clientFocus = ItemStack.parseOptional(registries, tag.getCompound("focus"));
            clientInput = ItemStack.parseOptional(registries, tag.getCompound("input"));
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ReactionChamberMenu(id, inventory, this);
    }

    /**
     * Techguns 2 {@code ReactionChamberFluidTank}: fills only up to the liquid level, drains only what is above it,
     * one fluid.
     */
    public final class ReactionTank extends net.neoforged.neoforge.fluids.capability.templates.FluidTank {
        ReactionTank() {
            super(TANK_CAPACITY);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || (!fluid.isEmpty() && !FluidStack.isSameFluidSameComponents(fluid, resource))) {
                return 0;
            }
            int filled = Math.min(liquidLevel * ReactionChamberRecipe.BUCKET - fluid.getAmount(), resource.getAmount());
            if (filled <= 0) {
                return 0;
            }
            if (action.execute()) {
                if (fluid.isEmpty()) {
                    fluid = resource.copyWithAmount(filled);
                } else {
                    fluid.grow(filled);
                }
                onContentsChanged();
            }
            return filled;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int drained = Math.min(maxDrain, Math.max(0, fluid.getAmount() - liquidLevel * ReactionChamberRecipe.BUCKET));
            if (drained <= 0) {
                return FluidStack.EMPTY;
            }
            FluidStack stack = fluid.copyWithAmount(drained);
            if (action.execute()) {
                fluid.shrink(drained);
                onContentsChanged();
            }
            return stack;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, fluid) ? FluidStack.EMPTY : drain(resource.getAmount(), action);
        }

        void removeInternal(int amount) {
            fluid.shrink(Math.min(amount, fluid.getAmount()));
            onContentsChanged();
        }

        @Override
        protected void onContentsChanged() {
            contentsChanged = true;
            fluidsChanged = true;
            setChanged();
        }
    }
}
