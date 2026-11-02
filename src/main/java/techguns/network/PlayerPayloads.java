package techguns.network;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import techguns.TGIds;
import techguns.player.TGPlayerData;

/** Player settings and gear: client toggles, the server's copy sent back on login and respawn, extra slot contents. */
public final class PlayerPayloads {
    private static final StreamCodec<ByteBuf, TGPlayerData.Toggle> TOGGLE = ByteBufCodecs.idMapper(
            i -> TGPlayerData.Toggle.values()[i], TGPlayerData.Toggle::ordinal);

    private PlayerPayloads() {
    }

    /** Client to server: the player switched a toggle (step assist, night vision, jetpack). */
    public record SetToggle(TGPlayerData.Toggle toggle, boolean enabled) implements CustomPacketPayload {
        public static final Type<SetToggle> TYPE = new Type<>(TGIds.id("set_toggle"));
        public static final StreamCodec<ByteBuf, SetToggle> STREAM_CODEC = StreamCodec.composite(TOGGLE, SetToggle::toggle,
                ByteBufCodecs.BOOL, SetToggle::enabled, SetToggle::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server to client: the saved player toggles. */
    public record SyncPlayerData(boolean stepAssist, boolean nightVision, boolean jetpack, boolean safeMode) implements CustomPacketPayload {
        public static final Type<SyncPlayerData> TYPE = new Type<>(TGIds.id("sync_player_data"));
        public static final StreamCodec<ByteBuf, SyncPlayerData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, SyncPlayerData::stepAssist, ByteBufCodecs.BOOL, SyncPlayerData::nightVision,
                ByteBufCodecs.BOOL, SyncPlayerData::jetpack, ByteBufCodecs.BOOL, SyncPlayerData::safeMode, SyncPlayerData::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server to client: the player's radiation level. */
    public record SyncRadiation(int level) implements CustomPacketPayload {
        public static final Type<SyncRadiation> TYPE = new Type<>(TGIds.id("sync_radiation"));
        public static final StreamCodec<ByteBuf, SyncRadiation> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT,
                SyncRadiation::level, SyncRadiation::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client to server: the jetpack boost key (jump) was pressed or released. */
    public record JetpackBoost(boolean active) implements CustomPacketPayload {
        public static final Type<JetpackBoost> TYPE = new Type<>(TGIds.id("jetpack_boost"));
        public static final StreamCodec<ByteBuf, JetpackBoost> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL,
                JetpackBoost::active, JetpackBoost::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server to clients: another player's jetpack boost state (for the flames). */
    public record JetpackBoostState(int entityId, boolean active) implements CustomPacketPayload {
        public static final Type<JetpackBoostState> TYPE = new Type<>(TGIds.id("jetpack_boost_state"));
        public static final StreamCodec<ByteBuf, JetpackBoostState> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT,
                JetpackBoostState::entityId, ByteBufCodecs.BOOL, JetpackBoostState::active, JetpackBoostState::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client to server: open the Techguns inventory tab (gear and food slots). */
    public record OpenExtraSlots() implements CustomPacketPayload {
        public static final Type<OpenExtraSlots> TYPE = new Type<>(TGIds.id("open_extra_slots"));
        public static final StreamCodec<ByteBuf, OpenExtraSlots> STREAM_CODEC = StreamCodec.unit(new OpenExtraSlots());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server to clients: a player's fallback extra slots (face, back, hand), for rendering and the slot screen. */
    public record SyncExtraSlots(int entityId, List<ItemStack> stacks) implements CustomPacketPayload {
        public static final Type<SyncExtraSlots> TYPE = new Type<>(TGIds.id("sync_extra_slots"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncExtraSlots> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SyncExtraSlots::entityId,
                ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), SyncExtraSlots::stacks,
                SyncExtraSlots::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server to client: the player's own inventory tab slots (food, heal, ammo boxes), resent when they change. */
    public record SyncTabSlots(List<ItemStack> stacks) implements CustomPacketPayload {
        public static final Type<SyncTabSlots> TYPE = new Type<>(TGIds.id("sync_tab_slots"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncTabSlots> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), SyncTabSlots::stacks,
                SyncTabSlots::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Server to client: a recipe was clicked in the recipe book of the Techguns inventory tab, which has no crafting grid;
     * the client switches to the vanilla inventory and asks to place it there.
     */
    public record PlaceRecipeInInventory(ResourceLocation recipe, boolean placeAll) implements CustomPacketPayload {
        public static final Type<PlaceRecipeInInventory> TYPE = new Type<>(TGIds.id("place_recipe_in_inventory"));
        public static final StreamCodec<ByteBuf, PlaceRecipeInInventory> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, PlaceRecipeInInventory::recipe,
                ByteBufCodecs.BOOL, PlaceRecipeInInventory::placeAll,
                PlaceRecipeInInventory::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
