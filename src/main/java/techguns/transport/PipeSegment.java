package techguns.transport;

import java.util.Arrays;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import techguns.core.transport.SideMode;

/**
 * One pipe kind inside a pipe block: its tier, the mode of each side and (computed, synced for the model) which sides
 * link to another pipe of the kind and which touch a block that has the kind's capability.
 */
public final class PipeSegment {
    private final PipeKind kind;
    private int tier;
    private final SideMode[] modes = new SideMode[6];
    /** Bits by {@link Direction#get3DDataValue()}: linked to the same kind in the neighbouring pipe block. */
    private int links;
    /** Bits: a block with the kind's capability (an endpoint). */
    private int ends;

    public PipeSegment(PipeKind kind, int tier) {
        this.kind = kind;
        this.tier = tier;
        Arrays.fill(modes, SideMode.NEUTRAL);
    }

    public PipeKind kind() {
        return kind;
    }

    public int tier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public SideMode mode(Direction side) {
        return modes[side.get3DDataValue()];
    }

    public void setMode(Direction side, SideMode mode) {
        modes[side.get3DDataValue()] = mode;
    }

    public boolean linked(Direction side) {
        return (links & 1 << side.get3DDataValue()) != 0;
    }

    public boolean endpoint(Direction side) {
        return (ends & 1 << side.get3DDataValue()) != 0;
    }

    /** Sets the computed connections; true if they changed. */
    public boolean setConnections(int links, int ends) {
        boolean changed = this.links != links || this.ends != ends;
        this.links = links;
        this.ends = ends;
        return changed;
    }

    public int links() {
        return links;
    }

    public int ends() {
        return ends;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", kind.name());
        tag.putByte("tier", (byte) tier);
        byte[] m = new byte[6];
        for (int i = 0; i < 6; i++) {
            m[i] = (byte) modes[i].ordinal();
        }
        tag.putByteArray("modes", m);
        tag.putByte("links", (byte) links);
        tag.putByte("ends", (byte) ends);
        return tag;
    }

    @Nullable
    public static PipeSegment load(CompoundTag tag) {
        PipeKind kind = TGPipeKinds.byName(tag.getString("kind"));
        if (kind == null) {
            return null;
        }
        PipeSegment segment = new PipeSegment(kind, Math.max(0, Math.min(kind.tierCount() - 1, tag.getByte("tier"))));
        byte[] m = tag.getByteArray("modes");
        for (int i = 0; i < 6 && i < m.length; i++) {
            segment.modes[i] = SideMode.byId(m[i]);
        }
        segment.links = tag.getByte("links") & 0x3F;
        segment.ends = tag.getByte("ends") & 0x3F;
        return segment;
    }
}
