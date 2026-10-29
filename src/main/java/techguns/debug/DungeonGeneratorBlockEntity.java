package techguns.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import techguns.registry.TGBlockEntities;
import techguns.world.structure.StructureScans;
import techguns.world.structure.dungeon.DungeonDebug;

/**
 * Techguns 2 {@code DungeonGeneratorTileEnt}: generates a maze dungeon of a template (or the Nether preset) in the
 * 48 x 26 x 48 box next to it (one block east and south); clear empties the box.
 */
public class DungeonGeneratorBlockEntity extends DungeonToolBlockEntity {
    public static final int BUTTON_CLEAR = 1;
    public static final int ACTION_GENERATE = 0;
    /** Techguns 2 {@code sizeX, sizeY, sizeZ}. */
    public static final Vec3i SIZE = new Vec3i(48, 26, 48);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 && showGhost ? 1 : 0;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public DungeonGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.DUNGEON_GENERATOR.get(), pos, state);
    }

    /** The corner of the dungeon box. */
    public BlockPos origin() {
        return worldPosition.offset(1, 0, 1);
    }

    @Override
    public ContainerData getData() {
        return data;
    }

    @Override
    protected boolean pressToolButton(Player player, int id) {
        if (id != BUTTON_CLEAR) {
            return false;
        }
        for (BlockPos pos : BlockPos.betweenClosed(origin(), origin().offset(SIZE.getX() - 1, SIZE.getY() - 1, SIZE.getZ() - 1))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    @Override
    public void templateAction(Player player, int action, String name) {
        if (action != ACTION_GENERATE || !isUsableBy(player) || !(level instanceof ServerLevel server)) {
            return;
        }
        String used = DungeonDebug.generate(server, origin(), SIZE.getX(), SIZE.getY(), SIZE.getZ(), StructureScans.cleanName(name), server.random);
        player.displayClientMessage(Component.translatable("techguns.debug.dungeon.generated", used), false);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("techguns.container.dungeongenerator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DungeonToolMenu(id, this, java.util.List.of());
    }
}
