package techguns.machine;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import techguns.TGConfig;
import techguns.armor.TGArmorItem;
import techguns.recipe.MachineInput;
import techguns.recipe.UpgradeBenchRecipe;
import techguns.registry.TGMenus;
import techguns.registry.TGRecipes;

/**
 * Upgrade Bench menu (Techguns 2 {@code UpgradeBenchContainer}, same positions): armor and upgrade item in, the
 * upgraded armor out for {@code upgradeXpCost} experience points per level gained, like an anvil. The inputs go back
 * to the player when the menu closes.
 */
public class UpgradeBenchMenu extends TGMachineMenu<UpgradeBenchBlockEntity> {
    public static final int SLOT_ARMOR = 0;
    public static final int SLOT_UPGRADE = 1;
    public static final int SLOT_RESULT = 2;
    private static final int PLAYER_START = 3;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final Player player;
    private final Container inputs = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };
    private final ResultContainer result = new ResultContainer();
    /** Experience points the result costs; computed by the server, synced to the screen. */
    private final DataSlot xpCost = DataSlot.standalone();

    /** Client constructor (menu type factory). */
    public UpgradeBenchMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<UpgradeBenchBlockEntity>machineAt(inventory, buf));
    }

    public UpgradeBenchMenu(int id, Inventory inventory, UpgradeBenchBlockEntity machine) {
        super(TGMenus.UPGRADE_BENCH.get(), id, machine);
        this.player = inventory.player;
        addSlot(new Slot(inputs, SLOT_ARMOR, 17, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof TGArmorItem;
            }
        });
        addSlot(new Slot(inputs, SLOT_UPGRADE, 53, 47));
        addSlot(new Slot(result, 0, 116, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return hasItem() && (player.getAbilities().instabuild || player.totalExperience >= xpCost.get());
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                takeResult(player);
                super.onTake(player, stack);
            }
        });
        addPlayerInventory(inventory);
        for (int i = 0; i < RepairBenchBlockEntity.ARMOR_SLOTS.size(); i++) {
            addSlot(PlayerEquipmentSlot.armor(inventory, RepairBenchBlockEntity.ARMOR_SLOTS.get(i), 17 + i * 18, 18));
        }
        addDataSlot(xpCost);
    }

    public int xpCost() {
        return xpCost.get();
    }

    public boolean hasResult() {
        return !result.getItem(0).isEmpty();
    }

    private Optional<UpgradeBenchRecipe> recipe(Level level) {
        MachineInput input = MachineInput.of(inputs.getItem(SLOT_ARMOR), inputs.getItem(SLOT_UPGRADE));
        return level.getRecipeManager().getAllRecipesFor(TGRecipes.UPGRADE_BENCH.get()).stream().map(RecipeHolder::value)
                .filter(r -> r.matches(input, level)).findFirst();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        Level level = player.level();
        if (container != inputs || level.isClientSide) {
            return;
        }
        ItemStack armor = inputs.getItem(SLOT_ARMOR);
        Optional<UpgradeBenchRecipe> recipe = recipe(level);
        result.setItem(0, recipe.map(r -> r.assemble(MachineInput.of(armor, inputs.getItem(SLOT_UPGRADE)), level.registryAccess()))
                .orElse(ItemStack.EMPTY));
        xpCost.set(recipe.map(r -> r.levelsGained(armor, level.registryAccess()) * TGConfig.UPGRADE_XP_COST.get()).orElse(0));
        broadcastChanges();
    }

    /** Techguns 2 output {@code onTake}: uses up the armor and one upgrade item, takes the experience. */
    private void takeResult(Player player) {
        if (!player.getAbilities().instabuild) {
            player.giveExperiencePoints(-xpCost.get());
        }
        inputs.setItem(SLOT_ARMOR, ItemStack.EMPTY);
        inputs.removeItem(SLOT_UPGRADE, 1);
        xpCost.set(0);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_DIAMOND, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index <= SLOT_RESULT) {
            if (index == SLOT_RESULT && !slot.mayPickup(player)) {
                return ItemStack.EMPTY;
            }
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else {
            int target = stack.getItem() instanceof TGArmorItem ? SLOT_ARMOR : SLOT_UPGRADE;
            if (!moveItemStackTo(stack, target, target + 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        clearContainer(player, inputs);
    }
}
