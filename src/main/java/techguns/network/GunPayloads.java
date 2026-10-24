package techguns.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import techguns.TGIds;

/**
 * Payloads of the gun system.
 */
public final class GunPayloads {
    private GunPayloads() {
    }

    static final StreamCodec<ByteBuf, InteractionHand> HAND = ByteBufCodecs.BOOL.map(
            offhand -> offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, hand -> hand == InteractionHand.OFF_HAND);

    /** Client → server: the local player fired the gun in {@code hand}. */
    public record Shoot(InteractionHand hand, boolean zooming) implements CustomPacketPayload {
        public static final Type<Shoot> TYPE = new Type<>(TGIds.id("shoot"));
        public static final StreamCodec<ByteBuf, Shoot> STREAM_CODEC = StreamCodec.composite(
                HAND, Shoot::hand, ByteBufCodecs.BOOL, Shoot::zooming, Shoot::new);

        @Override
        public Type<Shoot> type() {
            return TYPE;
        }
    }

    /** Client → server: the local player pressed the force reload key. */
    public record ForceReload(InteractionHand hand) implements CustomPacketPayload {
        public static final Type<ForceReload> TYPE = new Type<>(TGIds.id("force_reload"));
        public static final StreamCodec<ByteBuf, ForceReload> STREAM_CODEC = StreamCodec.composite(HAND, ForceReload::hand, ForceReload::new);

        @Override
        public Type<ForceReload> type() {
            return TYPE;
        }
    }

    /** Server → clients tracking the shooter: play recoil and muzzle flash. */
    public record GunFired(int entityId, InteractionHand hand, int recoilTicks, int muzzleFlashTicks) implements CustomPacketPayload {
        public static final Type<GunFired> TYPE = new Type<>(TGIds.id("gun_fired"));
        public static final StreamCodec<ByteBuf, GunFired> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, GunFired::entityId, HAND, GunFired::hand,
                ByteBufCodecs.VAR_INT, GunFired::recoilTicks, ByteBufCodecs.VAR_INT, GunFired::muzzleFlashTicks, GunFired::new);

        @Override
        public Type<GunFired> type() {
            return TYPE;
        }
    }

    /** Server → clients tracking the shooter: play the reload animation. */
    public record ReloadStarted(int entityId, InteractionHand hand, int reloadTicks) implements CustomPacketPayload {
        public static final Type<ReloadStarted> TYPE = new Type<>(TGIds.id("reload_started"));
        public static final StreamCodec<ByteBuf, ReloadStarted> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ReloadStarted::entityId, HAND, ReloadStarted::hand,
                ByteBufCodecs.VAR_INT, ReloadStarted::reloadTicks, ReloadStarted::new);

        @Override
        public Type<ReloadStarted> type() {
            return TYPE;
        }
    }
}
