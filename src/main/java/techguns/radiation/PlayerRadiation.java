package techguns.radiation;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import techguns.TGConfig;
import techguns.core.radiation.RadiationRules;
import techguns.network.PlayerPayloads;
import techguns.registry.TGAttachments;

/**
 * A player's radiation level (Techguns 2 {@code TGExtendedPlayer.radlevel}), 0 to {@link RadiationRules#MAX}: saved,
 * kept through death (minus {@link RadiationRules#LOST_ON_DEATH}) and sent to the player's client when it changes.
 * The server remembers what each client got and resends when that differs, which also covers a client that wasn't
 * ready for the first packet (login, respawn and dimension change forget the remembered value).
 */
public final class PlayerRadiation {
    /** Server: the level last sent to each player's client. */
    private static final Map<Player, Integer> SENT = Collections.synchronizedMap(new WeakHashMap<>());

    private PlayerRadiation() {
    }

    public static int get(Player player) {
        return player.getData(TGAttachments.RADIATION);
    }

    /** Sets the level (clamped); the server sends a change to the client. */
    public static void set(Player player, int level) {
        int clamped = RadiationRules.add(level, 0);
        if (clamped != get(player)) {
            player.setData(TGAttachments.RADIATION, clamped);
            sync(player);
        }
    }

    /** Adds (or with a negative amount removes) radiation; nothing is added while the radiation system is off. */
    public static void add(Player player, int amount) {
        if (amount > 0 && !TGConfig.RADIATION_ENABLED.get()) {
            return;
        }
        set(player, RadiationRules.add(get(player), amount));
    }

    public static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            int level = get(player);
            SENT.put(player, level);
            PacketDistributor.sendToPlayer(serverPlayer, new PlayerPayloads.SyncRadiation(level));
        }
    }

    /** Sends the level if the client has another one. */
    public static void syncIfChanged(Player player) {
        Integer sent = SENT.get(player);
        if (sent == null || sent != get(player)) {
            sync(player);
        }
    }

    /** The client lost or never got the level; it is sent again with the next check. */
    public static void forgetSent(Player player) {
        SENT.remove(player);
    }
}
