package techguns.debug;

import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGMenus;

/**
 * Menu of the dungeon scanner and generator (Techguns 2 {@code DungeonScannerContainer}): no slots; the settings come
 * as data slots, plain buttons through {@link #clickMenuButton}, buttons with the typed template name through the
 * {@code TemplateAction} payload. The client also gets the known dungeon template names to pick from.
 */
// MIGRATION(26.x): menu APIs (MenuType, AbstractContainerMenu) change in 1.21.2+.
public class DungeonToolMenu extends AbstractContainerMenu {
    private final DungeonToolBlockEntity tool;
    private final ContainerData data;
    private final List<String> templates;

    /** Client constructor (menu type factory): the tool's position and the template names. */
    public DungeonToolMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, (DungeonToolBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()), buf.readList(FriendlyByteBuf::readUtf));
    }

    public DungeonToolMenu(int id, DungeonToolBlockEntity tool, List<String> templates) {
        super(TGMenus.DUNGEON_TOOL.get(), id);
        this.tool = tool;
        this.templates = templates;
        this.data = tool.getLevel() != null && tool.getLevel().isClientSide ? new SimpleContainerData(tool.getData().getCount()) : tool.getData();
        addDataSlots(data);
    }

    public DungeonToolBlockEntity tool() {
        return tool;
    }

    /** Synced setting {@code index} (the tool's data indices). */
    public int data(int index) {
        return data.get(index);
    }

    public List<String> templates() {
        return templates;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return tool.pressButton(player, id);
    }

    /** Server: a button with the typed template name. */
    public void templateAction(Player player, int action, String name) {
        tool.templateAction(player, action, name);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return tool.isUsableBy(player);
    }
}
