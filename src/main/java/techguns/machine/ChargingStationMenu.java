package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/** Charging Station menu (Techguns 2 {@code ChargingStationContainer}, same slot positions). */
public class ChargingStationMenu extends TGMachineMenu<ChargingStationBlockEntity> {
    /** Client constructor (menu type factory). */
    public ChargingStationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<ChargingStationBlockEntity>machineAt(inventory, buf));
    }

    public ChargingStationMenu(int id, Inventory inventory, ChargingStationBlockEntity machine) {
        super(TGMenus.CHARGING_STATION.get(), id, machine);
        addMachineSlot(ChargingStationBlockEntity.SLOT_INPUT, 19, 17);
        addMachineSlot(ChargingStationBlockEntity.SLOT_OUTPUT, 68, 17);
        addMachineSlot(ChargingStationBlockEntity.SLOT_UPGRADE, 152, 60);
        addPlayerInventory(inventory);
    }

    @Override
    protected int targetSlot(ItemStack stack) {
        return machine.targetSlot(stack);
    }
}
