package techguns.network;

import java.util.Map;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import techguns.TGIds;
import techguns.gun.GunStats;
import techguns.stats.ArmorStatOverride;

/** Datapack stat overrides, sent to clients so tooltips, fire rate, zoom and armor values match the server. */
public final class StatPayloads {
    private StatPayloads() {
    }

    /** Server to client: all gun and armor stat overrides (replaces the previous set). */
    public record SyncStatOverrides(Map<ResourceLocation, GunStats> guns, Map<ResourceLocation, ArmorStatOverride> armor)
            implements CustomPacketPayload {
        public static final Type<SyncStatOverrides> TYPE = new Type<>(TGIds.id("sync_stat_overrides"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncStatOverrides> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodecWithRegistries(Codec.unboundedMap(ResourceLocation.CODEC, GunStats.CODEC)), SyncStatOverrides::guns,
                ByteBufCodecs.fromCodecWithRegistries(Codec.unboundedMap(ResourceLocation.CODEC, ArmorStatOverride.CODEC)), SyncStatOverrides::armor,
                SyncStatOverrides::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
