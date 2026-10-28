package techguns.compat.team;

import java.util.UUID;

/**
 * Answers who belongs together (design doc §5.4, "TeamProvider"): Techguns 2 asked FTB Lib these questions; each team
 * source (vanilla scoreboard teams, a team mod) implements them and {@link TGTeams} combines all of them. Called on the
 * server only; a player without a team in this source is a member and ally of nobody.
 */
public interface TeamProvider {
    /** Same team. */
    boolean isMember(UUID a, UUID b);

    /** Allied team (members included). */
    boolean isAlly(UUID a, UUID b);

    /** Declared enemies. */
    boolean isEnemy(UUID a, UUID b);

    /** Whether this source has teams right now (turrets and machines then offer the team settings). */
    default boolean isActive() {
        return true;
    }
}
