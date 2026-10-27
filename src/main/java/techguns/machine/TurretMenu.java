package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/** Turret menu (Techguns 2 {@code TurretContainer}, same slot positions): ammo, empty magazines, gun and armor plate. */
public class TurretMenu extends TGMachineMenu<TurretBlockEntity> {
    /** Client constructor (menu type factory). */
    public TurretMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<TurretBlockEntity>machineAt(inventory, buf));
    }

    public TurretMenu(int id, Inventory inventory, TurretBlockEntity machine) {
        super(TGMenus.TURRET.get(), id, machine);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addMachineSlot(TurretBlockEntity.SLOT_INPUT_FIRST + i * 3 + j, 18 + 18 * i, 17 + 18 * j, "bullet");
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addMachineSlot(TurretBlockEntity.SLOT_OUTPUT_FIRST + i * 3 + j, 116 + 18 * i, 17 + 18 * j, "bullet_empty");
            }
        }
        addMachineSlot(TurretBlockEntity.SLOT_WEAPON, 85, 19, "turretgun");
        addMachineSlot(TurretBlockEntity.SLOT_ARMOR, 85, 42, "turretarmor");
        addPlayerInventory(inventory);
    }

    public boolean isDead() {
        return data.get(TurretBlockEntity.DATA_DEAD) != 0;
    }

    public int repairTime() {
        return data.get(TurretBlockEntity.DATA_REPAIR_TIME);
    }

    public float health() {
        return data.get(TurretBlockEntity.DATA_HEALTH) / 10.0f;
    }

    public boolean isAttackAnimals() {
        return data.get(TurretBlockEntity.DATA_ATTACK_ANIMALS) != 0;
    }

    public int pvpSetting() {
        return data.get(TurretBlockEntity.DATA_PVP);
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }

    @Override
    protected int targetSlotCount(int target) {
        return machine.targetSlotCount(target);
    }
}
