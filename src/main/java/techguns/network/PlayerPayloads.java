package techguns.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import techguns.TGIds;

/** Player settings: client toggles and the server's copy sent back on login and respawn. */
public final class PlayerPayloads {
    private PlayerPayloads() {
    }

    /** Client to server: the player toggled step assist. */
    public record SetStepAssist(boolean enabled) implements CustomPacketPayload {
        public static final Type<SetStepAssist> TYPE = new Type<>(TGIds.id("set_step_assist"));
        public static final StreamCodec<ByteBuf, SetStepAssist> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL,
                SetStepAssist::enabled, SetStepAssist::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server to client: the saved player settings. */
    public record SyncPlayerData(boolean stepAssist) implements CustomPacketPayload {
        public static final Type<SyncPlayerData> TYPE = new Type<>(TGIds.id("sync_player_data"));
        public static final StreamCodec<ByteBuf, SyncPlayerData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL,
                SyncPlayerData::stepAssist, SyncPlayerData::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
