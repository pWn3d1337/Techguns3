package techguns.machine;

import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import techguns.recipe.AmmoPressRecipe;
import techguns.recipe.MachineInput;
import techguns.registry.TGBlockEntities;
import techguns.registry.TGRecipes;
import techguns.registry.TGSounds;

/**
 * Ammo Press (Techguns 2 {@code AmmoPressTileEnt}): one bullet metal, two casing metal and one powder make the rounds
 * of the selected build plan; 100 ticks at 5 FE per tick and operation.
 */
public class AmmoPressBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_BULLET_METAL = 0;
    public static final int SLOT_CASING_METAL = 1;
    public static final int SLOT_POWDER = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_UPGRADE = 4;
    /** Selects build plan {@code id - BUTTON_SELECT} (the screen's plan list). */
    public static final int BUTTON_SELECT = BUTTON_MACHINE;
    public static final int DATA_PLAN = DATA_COUNT;

    public static final int ENERGY_CAPACITY = 20000;
    public static final int POWER_PER_TICK = 5;
    public static final int TIME = 100;
    private static final float SOUND_VOLUME = 0.5f;

    private int plan;

    public AmmoPressBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.AMMO_PRESS.get(), pos, state, 5, ENERGY_CAPACITY);
    }

    /** Build plans in their display order. */
    public List<AmmoPressRecipe> plans() {
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager().getAllRecipesFor(TGRecipes.AMMO_PRESS.get()).stream().map(RecipeHolder::value)
                .sorted(Comparator.comparingInt(AmmoPressRecipe::order)).toList();
    }

    public int getPlan() {
        return plan;
    }

    @Override
    protected int upgradeSlot() {
        return SLOT_UPGRADE;
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return slot == SLOT_OUTPUT;
    }

    /** Techguns 2: each input slot takes what any plan accepts there. */
    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        if (level == null) {
            return slot <= SLOT_POWDER;
        }
        return switch (slot) {
            case SLOT_BULLET_METAL -> plans().stream().anyMatch(p -> p.bulletMetal().test(stack));
            case SLOT_CASING_METAL -> plans().stream().anyMatch(p -> p.casingMetal().test(stack));
            case SLOT_POWDER -> plans().stream().anyMatch(p -> p.powder().test(stack));
            default -> false;
        };
    }

    @Override
    protected void checkAndStartOperation() {
        List<AmmoPressRecipe> plans = plans();
        if (plans.isEmpty()) {
            return;
        }
        AmmoPressRecipe recipe = plans.get(Math.floorMod(plan, plans.size()));
        MachineInput input = MachineInput.of(inventory.getStackInSlot(SLOT_BULLET_METAL), inventory.getStackInSlot(SLOT_CASING_METAL),
                inventory.getStackInSlot(SLOT_POWDER));
        if (!recipe.matches(input, level) || !canOutput(recipe.result(), SLOT_OUTPUT)) {
            return;
        }
        ItemStack output = recipe.result();
        int multiplier = 1;
        for (int i = maxStackMultiplier(); i > 1; i--) {
            if (canOutput(output.copyWithCount(output.getCount() * i), SLOT_OUTPUT) && has(SLOT_BULLET_METAL, i)
                    && has(SLOT_CASING_METAL, AmmoPressRecipe.CASING_METAL_COUNT * i) && has(SLOT_POWDER, i)) {
                multiplier = i;
                break;
            }
        }
        consume(SLOT_BULLET_METAL, multiplier);
        consume(SLOT_CASING_METAL, AmmoPressRecipe.CASING_METAL_COUNT * multiplier);
        consume(SLOT_POWDER, multiplier);
        contentsChanged = false;
        startOperation(new MachineOperation(List.of(output), multiplier, TIME, POWER_PER_TICK));
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
        deliver(SLOT_OUTPUT, operation.output(0));
    }

    /** Techguns 2: two press sounds in each half of the operation. */
    @Override
    protected void playWorkSound() {
        int first = Math.round(totalTime * 0.05f);
        int second = Math.round(totalTime * 0.30f);
        int half = Math.round(totalTime * 0.5f);
        if (level == null) {
            return;
        }
        if (progress == first || progress == first + half) {
            level.playLocalSound(worldPosition, TGSounds.AMMO_PRESS_WORK1.get(), SoundSource.BLOCKS, SOUND_VOLUME, 1.0f, true);
        } else if (progress == second || progress == second + half) {
            level.playLocalSound(worldPosition, TGSounds.AMMO_PRESS_WORK2.get(), SoundSource.BLOCKS, SOUND_VOLUME, 1.0f, true);
        }
    }

    /** Shift click: the first input slot that takes the item, else the upgrade slot. */
    public int targetSlot(ItemStack stack) {
        for (int slot = SLOT_BULLET_METAL; slot <= SLOT_POWDER; slot++) {
            if (inventory.isItemValid(slot, stack)) {
                return slot;
            }
        }
        return inventory.isItemValid(SLOT_UPGRADE, stack) ? SLOT_UPGRADE : -1;
    }

    @Override
    protected int extraDataCount() {
        return 1;
    }

    @Override
    protected int getExtraData(int index) {
        return index == DATA_PLAN ? plan : 0;
    }

    @Override
    protected boolean pressMachineButton(Player player, int id) {
        int index = id - BUTTON_SELECT;
        if (index < 0 || index >= plans().size()) {
            return false;
        }
        plan = index;
        return true;
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        tag.putByte("plan", (byte) plan);
    }

    @Override
    protected void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        plan = tag.getByte("plan");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AmmoPressMenu(id, inventory, this);
    }
}
