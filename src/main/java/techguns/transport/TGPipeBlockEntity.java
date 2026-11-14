package techguns.transport;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import techguns.core.transport.Lane;
import techguns.core.transport.SideMode;
import techguns.registry.TGBlockEntities;

/**
 * The shared pipe block's data: up to one {@link PipeSegment} per {@link Lane} (so cable, fluid and item pipe in one
 * block). Registers with the level's {@link TransportManager}, offers the push ports of its sides as capabilities and
 * hands the model its segments.
 */
public class TGPipeBlockEntity extends BlockEntity {
    /** Model data for the pipe model: the segments as an immutable snapshot. */
    public static final ModelProperty<RenderState> RENDER_STATE = new ModelProperty<>();

    /** What the model draws of one segment. */
    public record SegmentView(PipeKind kind, int tier, int links, int ends, SideMode[] modes) {
    }

    public record RenderState(List<SegmentView> segments) {
    }

    private final PipeSegment[] segments = new PipeSegment[Lane.values().length];
    @Nullable
    private VoxelShape shape;
    @Nullable
    private List<PipeParts.Part> parts;

    public TGPipeBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.PIPE.get(), pos, state);
    }

    // --- segments ---

    @Nullable
    public PipeSegment segment(PipeKind kind) {
        PipeSegment segment = segments[kind.lane().ordinal()];
        return segment != null && segment.kind() == kind ? segment : null;
    }

    @Nullable
    public PipeSegment segmentAt(Lane lane) {
        return segments[lane.ordinal()];
    }

    public List<PipeSegment> segments() {
        List<PipeSegment> list = new ArrayList<>();
        for (PipeSegment segment : segments) {
            if (segment != null) {
                list.add(segment);
            }
        }
        return list;
    }

    public boolean isEmpty() {
        return segments().isEmpty();
    }

    /** Whether the lane of {@code kind} is free (or holds that kind). */
    public boolean canHold(PipeKind kind) {
        PipeSegment present = segments[kind.lane().ordinal()];
        return present == null || present.kind() == kind;
    }

    /** Adds a segment of {@code kind}; false if its lane is taken. */
    public boolean addSegment(PipeKind kind, int tier) {
        if (segments[kind.lane().ordinal()] != null) {
            return false;
        }
        segments[kind.lane().ordinal()] = new PipeSegment(kind, tier);
        changed(true);
        return true;
    }

    /** Removes the segment of {@code kind} and returns it, or null. */
    @Nullable
    public PipeSegment removeSegment(PipeKind kind) {
        PipeSegment segment = segment(kind);
        if (segment != null) {
            segments[kind.lane().ordinal()] = null;
            changed(true);
        }
        return segment;
    }

    /** Sets the tier of the segment of {@code kind}; returns the old tier, or -1. */
    public int setTier(PipeKind kind, int tier) {
        PipeSegment segment = segment(kind);
        if (segment == null) {
            return -1;
        }
        int old = segment.tier();
        segment.setTier(tier);
        changed(true);
        return old;
    }

    public void setMode(PipeKind kind, Direction side, SideMode mode) {
        PipeSegment segment = segment(kind);
        if (segment != null && segment.mode(side) != mode) {
            segment.setMode(side, mode);
            changed(true);
        }
    }

    /** Server: the connections the network found; synced to clients when they changed. */
    void applyConnections(PipeKind kind, int links, int ends) {
        PipeSegment segment = segment(kind);
        if (segment != null && segment.setConnections(links, ends)) {
            changed(false);
        }
    }

    /**
     * Something about the segments changed: save, sync, renew the shape and the side capabilities; with
     * {@code topology} the networks are rebuilt too.
     */
    private void changed(boolean topology) {
        shape = null;
        parts = null;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.invalidateCapabilities(worldPosition);
            if (topology && level instanceof ServerLevel server) {
                TransportManager.get(server).markDirty(worldPosition);
            }
        }
    }

    /** Server: the network of {@code kind} through this pipe. */
    @Nullable
    public PipeNetwork network(PipeKind kind) {
        return level instanceof ServerLevel server && segment(kind) != null ? TransportManager.get(server).network(worldPosition, kind) : null;
    }

    /** The capability a block on {@code side} may push into: only on sides of a segment that accept pushes. */
    @Nullable
    public Object port(PipeKind kind, @Nullable Direction side) {
        PipeSegment segment = segment(kind);
        if (side == null || segment == null || !segment.mode(side).acceptsPush() || level == null || level.isClientSide) {
            return null;
        }
        return kind.logic().port(this, side);
    }

    // --- shape ---

    /** All parts of all segments, in block coordinates. */
    public List<PipeParts.Part> parts() {
        if (parts == null) {
            List<PipeParts.Part> list = new ArrayList<>();
            for (PipeSegment segment : segments()) {
                list.addAll(PipeParts.parts(segment.kind().lane(), segment.links(), segment.ends()));
            }
            parts = list;
        }
        return parts;
    }

    public VoxelShape shape() {
        if (shape == null) {
            shape = isEmpty() ? Shapes.box(0.375, 0.375, 0.375, 0.625, 0.625, 0.625) : PipeParts.shape(parts());
        }
        return shape;
    }

    // --- loading ---

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            TransportManager.get(server).add(this);
        } else if (level != null && level.isClientSide) {
            // the chunk was meshed before the block entity existed: mesh again with its model data
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel server) {
            TransportManager.get(server).remove(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level instanceof ServerLevel server) {
            TransportManager.get(server).remove(this);
        }
    }

    // --- saving and syncing ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeSegments(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readSegments(tag);
    }

    private void writeSegments(CompoundTag tag) {
        ListTag list = new ListTag();
        for (PipeSegment segment : segments()) {
            list.add(segment.save());
        }
        tag.put("segments", list);
    }

    private void readSegments(CompoundTag tag) {
        java.util.Arrays.fill(segments, null);
        for (Tag entry : tag.getList("segments", Tag.TAG_COMPOUND)) {
            PipeSegment segment = PipeSegment.load((CompoundTag) entry);
            if (segment != null) {
                segments[segment.kind().lane().ordinal()] = segment;
            }
        }
        shape = null;
        parts = null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        writeSegments(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }

    /** Client: new segments; the chunk section is re-meshed with the new model data. */
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        readSegments(tag);
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    // MIGRATION(26.x): ModelData / IDynamicBakedModel are replaced by BlockStateModel parts in 1.21.5+.
    @Override
    public ModelData getModelData() {
        List<SegmentView> views = new ArrayList<>();
        for (PipeSegment segment : segments()) {
            SideMode[] modes = new SideMode[6];
            for (Direction side : Direction.values()) {
                modes[side.get3DDataValue()] = segment.mode(side);
            }
            views.add(new SegmentView(segment.kind(), segment.tier(), segment.links(), segment.ends(), modes));
        }
        return ModelData.builder().with(RENDER_STATE, new RenderState(List.copyOf(views))).build();
    }
}
