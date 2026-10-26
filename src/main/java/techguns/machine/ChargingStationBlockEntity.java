package techguns.machine;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import techguns.recipe.ChargingRecipe;
import techguns.recipe.MachineInput;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGRecipes;
import techguns.registry.TGSounds;

/**
 * Charging Station (Techguns 2 {@code ChargingStationTileEnt}): charges empty energy cells and batteries by recipe
 * (800 FE per tick and item), and fills items with an FE capability (1600 FE per tick); a full item moves to the
 * output slot.
 */
public class ChargingStationBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_UPGRADE = 2;

    public static final int ENERGY_CAPACITY = 100000;
    public static final int CHARGE_RATE = 800;
    public static final int ITEM_CHARGE_RATE = 1600;
    private static final float SOUND_VOLUME = 0.35f;

    /** An FE item is being charged (not a recipe job). */
    private boolean charging;
    /** The item shown on the station (client: for the renderer). */
    private ItemStack shown = ItemStack.EMPTY;
    private int soundDelay;

    public ChargingStationBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.CHARGING_STATION.get(), pos, state, 3, ENERGY_CAPACITY);
    }

    private Optional<ChargingRecipe> recipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        MachineInput input = MachineInput.of(stack);
        return level.getRecipeManager().getAllRecipesFor(TGRecipes.CHARGING.get()).stream().map(RecipeHolder::value)
                .filter(r -> r.matches(input, level)).findFirst();
    }

    private static IEnergyStorage itemEnergy(ItemStack stack) {
        return stack.isEmpty() ? null : stack.getCapability(Capabilities.EnergyStorage.ITEM);
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
        return slot == SLOT_INPUT && (level == null || itemEnergy(stack) != null || recipeFor(stack).isPresent());
    }

    public int targetSlot(ItemStack stack) {
        if (inventory.isItemValid(SLOT_UPGRADE, stack)) {
            return SLOT_UPGRADE;
        }
        return inventory.isItemValid(SLOT_INPUT, stack) ? SLOT_INPUT : -1;
    }

    @Override
    protected void checkAndStartOperation() {
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        Optional<ChargingRecipe> found = recipeFor(input);
        if (found.isEmpty()) {
            return;
        }
        ChargingRecipe recipe = found.get();
        ItemStack output = recipe.result();
        int multiplier = 0;
        for (int i = Math.min(maxStackMultiplier(), input.getCount()); i >= 1; i--) {
            if (canOutput(output.copyWithCount(output.getCount() * i), SLOT_OUTPUT)) {
                multiplier = i;
                break;
            }
        }
        if (multiplier == 0) {
            return;
        }
        shown = input.copyWithCount(1);
        consume(SLOT_INPUT, multiplier);
        contentsChanged = false;
        startOperation(new MachineOperation(List.of(output), multiplier, Math.max(1, recipe.charge() / CHARGE_RATE), CHARGE_RATE));
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
        deliver(SLOT_OUTPUT, operation.output(0));
        shown = ItemStack.EMPTY;
    }

    /** Techguns 2: an FE item in the input slot is filled; when full it moves to the empty output slot. */
    @Override
    protected boolean idleTick() {
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        IEnergyStorage target = itemEnergy(input);
        if (target == null) {
            setCharging(false);
            return false;
        }
        if (target.receiveEnergy(ITEM_CHARGE_RATE, true) > 0) {
            int offered = Math.min(energy.getEnergyStored(), ITEM_CHARGE_RATE);
            int taken = target.receiveEnergy(offered, false);
            energy.consume(taken);
            inventory.setStackInSlot(SLOT_INPUT, input);
            setCharging(true);
        } else {
            if (inventory.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
                inventory.setStackInSlot(SLOT_OUTPUT, input);
                inventory.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
            }
            setCharging(false);
        }
        return true;
    }

    private void setCharging(boolean charging) {
        if (this.charging != charging) {
            this.charging = charging;
            shown = charging ? inventory.getStackInSlot(SLOT_INPUT).copyWithCount(1) : ItemStack.EMPTY;
            sendUpdate();
        }
    }

    public boolean isCharging() {
        return charging;
    }

    /** The item on the station while charging or working. */
    public ItemStack getShown() {
        return shown;
    }

    /** Techguns 2: flares on the open sides and the hum every second, for both jobs and FE charging. */
    @Override
    protected void playWorkSound() {
        chargeEffects();
    }

    /** FE charging is no job: the base does not animate it. */
    @Override
    protected void clientIdleTick() {
        if (charging) {
            chargeEffects();
        }
    }

    private void chargeEffects() {
        if (level == null) {
            return;
        }
        Direction facing = getBlockState().getValue(TGMachineBlock.FACING);
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.5;
        double z = worldPosition.getZ() + 0.5;
        // one flare at each side except the front (Techguns 2 offsets of 0.2 from the centre)
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side != facing) {
                MachineClientEffects.get().spawnFx("ChargeFlare2", level, x + side.getStepX() * 0.2, y, z + side.getStepZ() * 0.2);
            }
        }
        if (soundDelay > 0) {
            soundDelay--;
        } else {
            soundDelay = 20;
            level.playLocalSound(worldPosition, TGSounds.CHARGING_STATION_WORK.get(), SoundSource.BLOCKS, SOUND_VOLUME, 1.0f, false);
        }
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        tag.putBoolean("charging", charging);
    }

    @Override
    protected void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        charging = tag.getBoolean("charging");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!shown.isEmpty()) {
            tag.put("shown", shown.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        shown = ItemStack.parseOptional(registries, tag.getCompound("shown"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (!shown.isEmpty()) {
            tag.put("shown", shown.save(registries));
        }
        return tag;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ChargingStationMenu(id, inventory, this);
    }
}
