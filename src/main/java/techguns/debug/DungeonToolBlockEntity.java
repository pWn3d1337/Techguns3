package techguns.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Settings shared by the Techguns 2 dungeon scanner and generator: the ghost (the renderer's outline of the area)
 * and the menu buttons; the settings go to clients for the renderer.
 */
public abstract class DungeonToolBlockEntity extends BlockEntity implements MenuProvider {
    public static final int BUTTON_GHOST = 0;
    private static final double USE_RANGE_SQUARED = 64.0;

    protected boolean showGhost = true;

    protected DungeonToolBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean showGhost() {
        return showGhost;
    }

    public boolean isUsableBy(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this && TGDebug.enabled()
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= USE_RANGE_SQUARED;
    }

    /** Settings for the menu: the ghost flag first. */
    public abstract ContainerData getData();

    /** A button without text; false if not handled. */
    public boolean pressButton(Player player, int id) {
        if (!isUsableBy(player)) {
            return false;
        }
        if (id == BUTTON_GHOST) {
            showGhost = !showGhost;
        } else if (!pressToolButton(player, id)) {
            return false;
        }
        sendUpdate();
        return true;
    }

    protected abstract boolean pressToolButton(Player player, int id);

    /** A button that carries the template name typed in the screen (server). */
    public abstract void templateAction(Player player, int action, String name);

    protected void sendUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("show_ghost", showGhost);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        showGhost = !tag.contains("show_ghost") || tag.getBoolean("show_ghost");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
