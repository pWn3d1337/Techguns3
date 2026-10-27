package techguns.machine;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import techguns.armor.RepairMaterial;
import techguns.registry.TGBlockEntities;

/**
 * Repair Bench (Techguns 2 {@code RepairBenchTileEnt}): nine material slots and a repair slot; a button per worn armor
 * piece, the offhand and the repair slot fully repairs that item with its {@link RepairMaterial}s from the material
 * slots. No power, no redstone.
 */
public class RepairBenchBlockEntity extends TGMachineBlockEntity {
    public static final int SLOT_MATERIAL_FIRST = 0;
    public static final int SLOT_MATERIAL_LAST = 8;
    public static final int SLOT_REPAIR = 9;

    /** Repair buttons: worn armor head, chest, legs, feet, then the offhand and the repair slot. */
    public static final int BUTTON_REPAIR_ARMOR = BUTTON_MACHINE;
    public static final int BUTTON_REPAIR_OFFHAND = BUTTON_MACHINE + 4;
    public static final int BUTTON_REPAIR_SLOT = BUTTON_MACHINE + 5;
    /** Worn armor in button order. */
    public static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    public RepairBenchBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.REPAIR_BENCH.get(), pos, state, 10, 0);
    }

    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        return slot != SLOT_REPAIR || stack.getItem() instanceof RepairMaterial.Repairable;
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

    /** The item a repair button repairs. */
    public ItemStack target(Player player, int button) {
        if (button >= BUTTON_REPAIR_ARMOR && button < BUTTON_REPAIR_OFFHAND) {
            return player.getItemBySlot(ARMOR_SLOTS.get(button - BUTTON_REPAIR_ARMOR));
        }
        return button == BUTTON_REPAIR_OFFHAND ? player.getOffhandItem() : inventory.getStackInSlot(SLOT_REPAIR);
    }

    @Override
    protected boolean pressMachineButton(Player player, int id) {
        if (id < BUTTON_REPAIR_ARMOR || id > BUTTON_REPAIR_SLOT) {
            return false;
        }
        repair(target(player, id));
        return true;
    }

    /** Fully repairs {@code stack} if all its materials are in the material slots (Techguns 2 {@code doRepair}). */
    public boolean repair(ItemStack stack) {
        if (!(stack.getItem() instanceof RepairMaterial.Repairable repairable)) {
            return false;
        }
        List<RepairMaterial> cost = repairable.repairCost(stack);
        if (cost.isEmpty() || !cost.stream().allMatch(this::hasMaterial)) {
            return false;
        }
        cost.forEach(this::consumeMaterial);
        stack.setDamageValue(0);
        if (stack == inventory.getStackInSlot(SLOT_REPAIR)) {
            setChanged();
        }
        return true;
    }

    /** Whether the material slots hold {@code material} (the screen marks missing materials red). */
    public boolean hasMaterial(RepairMaterial material) {
        int found = 0;
        for (int slot = SLOT_MATERIAL_FIRST; slot <= SLOT_MATERIAL_LAST; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (material.ingredient().test(stack)) {
                found += stack.getCount();
            }
        }
        return found >= material.count();
    }

    private void consumeMaterial(RepairMaterial material) {
        int left = material.count();
        for (int slot = SLOT_MATERIAL_FIRST; slot <= SLOT_MATERIAL_LAST && left > 0; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (material.ingredient().test(stack)) {
                int taken = Math.min(left, stack.getCount());
                consume(slot, taken);
                left -= taken;
            }
        }
    }

    public int targetSlot(ItemStack stack) {
        if (inventory.isItemValid(SLOT_REPAIR, stack) && inventory.getStackInSlot(SLOT_REPAIR).isEmpty()) {
            return SLOT_REPAIR;
        }
        return stack.getItem() instanceof RepairMaterial.Repairable ? -1 : SLOT_MATERIAL_FIRST;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RepairBenchMenu(id, inventory, this);
    }
}
