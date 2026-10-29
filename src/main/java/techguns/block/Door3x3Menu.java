package techguns.block;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import techguns.machine.RedstoneMode;
import techguns.registry.TGMenus;

/**
 * Door settings (Techguns 2 {@code Door3x3Container}): no slots, the settings come as data slots, the buttons go through
 * {@link #clickMenuButton}.
 */
// MIGRATION(26.x): menu APIs (MenuType, AbstractContainerMenu) change in 1.21.2+.
public class Door3x3Menu extends AbstractContainerMenu {
    private final Door3x3BlockEntity door;
    private final ContainerData data;

    /** Client constructor (menu type factory). */
    public Door3x3Menu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (Door3x3BlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public Door3x3Menu(int id, Inventory inventory, Door3x3BlockEntity door) {
        super(TGMenus.DOOR_3X3.get(), id);
        this.door = door;
        this.data = door.getLevel() != null && door.getLevel().isClientSide
                ? new SimpleContainerData(Door3x3BlockEntity.DATA_COUNT) : door.getData();
        addDataSlots(data);
    }

    public Door3x3BlockEntity door() {
        return door;
    }

    public Door3x3BlockEntity.Mode mode() {
        return Door3x3BlockEntity.Mode.byId(data.get(Door3x3BlockEntity.DATA_MODE));
    }

    public boolean autoClose() {
        return data.get(Door3x3BlockEntity.DATA_AUTO_CLOSE) != 0;
    }

    public RedstoneMode redstoneMode() {
        return RedstoneMode.byId(data.get(Door3x3BlockEntity.DATA_REDSTONE));
    }

    /** {@link techguns.compat.team.TGTeams} security level, 0 public ... 3 owner. */
    public int security() {
        return data.get(Door3x3BlockEntity.DATA_SECURITY);
    }

    public boolean hasSignal() {
        return data.get(Door3x3BlockEntity.DATA_SIGNAL) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return door.pressButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return door.isUsableBy(player);
    }
}
