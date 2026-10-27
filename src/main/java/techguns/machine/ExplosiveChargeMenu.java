package techguns.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/** Mining charge settings (Techguns 2 {@code ExplosiveChargeContainer}): no slots, the settings come as data slots. */
public class ExplosiveChargeMenu extends AbstractContainerMenu {
    private final ExplosiveChargeBlockEntity charge;
    private final ContainerData data;

    /** Client constructor (menu type factory). */
    public ExplosiveChargeMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (ExplosiveChargeBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ExplosiveChargeMenu(int id, Inventory inventory, ExplosiveChargeBlockEntity charge) {
        super(TGMenus.EXPLOSIVE_CHARGE.get(), id);
        this.charge = charge;
        this.data = charge.getLevel() != null && charge.getLevel().isClientSide
                ? new SimpleContainerData(ExplosiveChargeBlockEntity.DATA_COUNT) : charge.getData();
        addDataSlots(data);
    }

    public ExplosiveChargeBlockEntity charge() {
        return charge;
    }

    public int radius() {
        return data.get(ExplosiveChargeBlockEntity.DATA_RADIUS);
    }

    public int length() {
        return data.get(ExplosiveChargeBlockEntity.DATA_LENGTH);
    }

    public int fuse() {
        return data.get(ExplosiveChargeBlockEntity.DATA_FUSE);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return charge.pressButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return charge.isUsableBy(player) && !charge.isArmed();
    }
}
