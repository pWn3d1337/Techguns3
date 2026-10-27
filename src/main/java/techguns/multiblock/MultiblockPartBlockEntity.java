package techguns.multiblock;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import techguns.registry.TGBlockEntities;

/**
 * A linked multiblock part (Techguns 2 {@code MultiBlockMachineTileEntSlave}): knows its master and which of the
 * master's capabilities it exposes ({@link MultiblockPart.Connector}).
 */
public class MultiblockPartBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos masterPos;
    private MultiblockPart.Connector connector = MultiblockPart.Connector.NONE;

    public MultiblockPartBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.MULTIBLOCK_PART.get(), pos, state);
    }

    public void link(BlockPos master, MultiblockPart.Connector connector) {
        this.masterPos = master.immutable();
        this.connector = connector;
        changed();
    }

    public void unlink() {
        this.masterPos = null;
        this.connector = MultiblockPart.Connector.NONE;
        changed();
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    public BlockPos getMasterPos() {
        return masterPos;
    }

    public MultiblockPart.Connector getConnector() {
        return connector;
    }

    /** The formed master this part belongs to, or null. */
    @Nullable
    public MultiblockMasterBlockEntity master() {
        if (level == null || masterPos == null) {
            return null;
        }
        return level.getBlockEntity(masterPos) instanceof MultiblockMasterBlockEntity master && master.isFormed() ? master : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveLink(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        masterPos = tag.contains("master") ? BlockPos.of(tag.getLong("master")) : null;
        connector = MultiblockPart.Connector.values()[Math.min(tag.getByte("connector"), MultiblockPart.Connector.values().length - 1)];
    }

    private void saveLink(CompoundTag tag) {
        if (masterPos != null) {
            tag.putLong("master", masterPos.asLong());
            tag.putByte("connector", (byte) connector.ordinal());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveLink(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
