package techguns.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import techguns.registry.TGBlockEntities;
import techguns.world.structure.StructureScans;
import techguns.world.structure.dungeon.DungeonDebug;

/**
 * Techguns 2 {@code DungeonScannerTileEnt}: the segments of a dungeon template are built in a grid next to the
 * scanner (segment size {@code sizeXZ} x {@code sizeY}); scan saves them as a template, load places a template's
 * segments, clear empties the grid, copy places the straight segment into every other place to start from.
 */
public class DungeonScannerBlockEntity extends DungeonToolBlockEntity {
    public static final int BUTTON_SIZE_XZ_PLUS = 1;
    public static final int BUTTON_SIZE_XZ_MINUS = 2;
    public static final int BUTTON_SIZE_Y_PLUS = 3;
    public static final int BUTTON_SIZE_Y_MINUS = 4;
    public static final int BUTTON_CLEAR = 5;
    public static final int BUTTON_COPY_FIRST = 6;
    public static final int ACTION_SCAN = 0;
    public static final int ACTION_LOAD = 1;

    public static final int DATA_GHOST = 0;
    public static final int DATA_SIZE_XZ = 1;
    public static final int DATA_SIZE_Y = 2;
    public static final int DATA_COUNT = 3;

    /** Techguns 2 defaults. */
    private int sizeXZ = 5;
    private int sizeY = 4;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_GHOST -> showGhost ? 1 : 0;
                case DATA_SIZE_XZ -> sizeXZ;
                case DATA_SIZE_Y -> sizeY;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public DungeonScannerBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.DUNGEON_SCANNER.get(), pos, state);
    }

    public int sizeXZ() {
        return sizeXZ;
    }

    public int sizeY() {
        return sizeY;
    }

    @Override
    public ContainerData getData() {
        return data;
    }

    @Override
    protected boolean pressToolButton(Player player, int id) {
        ServerLevel server = (ServerLevel) level;
        switch (id) {
            case BUTTON_SIZE_XZ_PLUS -> sizeXZ++;
            case BUTTON_SIZE_XZ_MINUS -> sizeXZ = Math.max(1, sizeXZ - 1);
            case BUTTON_SIZE_Y_PLUS -> sizeY++;
            case BUTTON_SIZE_Y_MINUS -> sizeY = Math.max(1, sizeY - 1);
            case BUTTON_CLEAR -> DungeonDebug.clear(server, worldPosition, sizeXZ, sizeY);
            case BUTTON_COPY_FIRST -> DungeonDebug.copyFirst(server, worldPosition, sizeXZ, sizeY);
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public void templateAction(Player player, int action, String name) {
        String template = StructureScans.cleanName(name);
        if (!isUsableBy(player) || template.isEmpty() || !(level instanceof ServerLevel server)) {
            return;
        }
        if (action == ACTION_SCAN) {
            int saved = DungeonDebug.scan(server, worldPosition, sizeXZ, sizeY, template);
            player.displayClientMessage(Component.translatable("techguns.debug.dungeon.scanned", saved, template), false);
        } else if (action == ACTION_LOAD) {
            int placed = DungeonDebug.load(server, worldPosition, sizeXZ, template);
            player.displayClientMessage(Component.translatable("techguns.debug.dungeon.loaded", placed, template), false);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("techguns.container.dungeonscanner");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DungeonToolMenu(id, this, java.util.List.of());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("size_xz", sizeXZ);
        tag.putInt("size_y", sizeY);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        sizeXZ = Math.max(1, tag.contains("size_xz") ? tag.getInt("size_xz") : 5);
        sizeY = Math.max(1, tag.contains("size_y") ? tag.getInt("size_y") : 4);
    }
}
