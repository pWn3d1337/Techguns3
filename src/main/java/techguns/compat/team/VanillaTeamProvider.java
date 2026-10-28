package techguns.compat.team;

import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Vanilla scoreboard teams ({@code /team}): players on the same team are members (and allies); vanilla has no
 * alliances or enemies. Active once the world has a team.
 */
final class VanillaTeamProvider implements TeamProvider {
    @Override
    public boolean isMember(UUID a, UUID b) {
        PlayerTeam team = team(a);
        return team != null && team == team(b);
    }

    @Override
    public boolean isAlly(UUID a, UUID b) {
        return isMember(a, b);
    }

    @Override
    public boolean isEnemy(UUID a, UUID b) {
        return false;
    }

    @Override
    public boolean isActive() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null && !server.getScoreboard().getPlayerTeams().isEmpty();
    }

    /**
     * The team of a player: scoreboard teams hold player names (online or from the profile cache) and, for holders
     * that are no named player, the UUID string.
     */
    private static PlayerTeam team(UUID player) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        Optional<String> name = online != null ? Optional.of(online.getScoreboardName())
                : server.getProfileCache() == null ? Optional.empty() : server.getProfileCache().get(player).map(GameProfile::getName);
        PlayerTeam team = name.map(n -> server.getScoreboard().getPlayersTeam(n)).orElse(null);
        return team != null ? team : server.getScoreboard().getPlayersTeam(player.toString());
    }
}
