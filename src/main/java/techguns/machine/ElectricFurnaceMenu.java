package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import techguns.registry.TGMenus;

/** Electric Furnace menu: input, output (gives the collected experience when taken), upgrade. */
public class ElectricFurnaceMenu extends TGMachineMenu<ElectricFurnaceBlockEntity> {
    public static final int INPUT_X = 56;
    public static final int INPUT_Y = 35;
    public static final int OUTPUT_X = 116;
    public static final int OUTPUT_Y = 35;

    /** Client constructor (menu type factory). */
    public ElectricFurnaceMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, TGMachineMenu.<ElectricFurnaceBlockEntity>machineAt(inventory, buf));
    }

    public ElectricFurnaceMenu(int id, Inventory inventory, ElectricFurnaceBlockEntity machine) {
        super(TGMenus.ELECTRIC_FURNACE.get(), id, machine);
        addMachineSlot(ElectricFurnaceBlockEntity.SLOT_INPUT, INPUT_X, INPUT_Y);
        addMachineSlot(new SlotItemHandler(machine.getInventory(), ElectricFurnaceBlockEntity.SLOT_OUTPUT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                machine.awardExperience(player);
                super.onTake(player, stack);
            }
        });
        addMachineSlot(ElectricFurnaceBlockEntity.SLOT_UPGRADE, 152, 60);
        addPlayerInventory(inventory);
    }
}
