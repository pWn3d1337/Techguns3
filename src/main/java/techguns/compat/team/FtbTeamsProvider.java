package techguns.compat.team;

import java.util.Optional;
import java.util.UUID;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamRank;

/**
 * FTB Teams (the successor of the FTB Lib teams Techguns 2 used): a player's team ranks the other player as member,
 * ally or enemy. Only loaded with FTB Teams installed.
 */
// MIGRATION(26.x): FTB Teams API; check the rank names and the manager access on the new version.
final class FtbTeamsProvider implements TeamProvider {
    @Override
    public boolean isMember(UUID a, UUID b) {
        return FTBTeamsAPI.api().isManagerLoaded() && FTBTeamsAPI.api().getManager().arePlayersInSameTeam(a, b);
    }

    @Override
    public boolean isAlly(UUID a, UUID b) {
        return rank(a, b).map(TeamRank::isAllyOrBetter).orElse(false);
    }

    @Override
    public boolean isEnemy(UUID a, UUID b) {
        return rank(a, b).map(TeamRank::isEnemyOrWorse).orElse(false);
    }

    /** How {@code a}'s team ranks {@code b}. */
    private static Optional<TeamRank> rank(UUID a, UUID b) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return Optional.empty();
        }
        return FTBTeamsAPI.api().getManager().getTeamForPlayerID(a).map((Team team) -> team.getRankForPlayer(b));
    }
}
