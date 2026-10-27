package techguns.compat.team;

import java.util.UUID;

/**
 * Answers who belongs together (design doc §3, "TeamProvider"): Techguns 2 asked FTB Lib these questions; a team mod
 * integration implements them. Without one, every player is on their own.
 */
public interface TeamProvider {
    /** Same team. */
    boolean isMember(UUID a, UUID b);

    /** Allied team (members included). */
    boolean isAlly(UUID a, UUID b);

    /** Declared enemies. */
    boolean isEnemy(UUID a, UUID b);

    /** No team mod: only the same player is a member or ally, nobody is an enemy. */
    TeamProvider NONE = new TeamProvider() {
        @Override
        public boolean isMember(UUID a, UUID b) {
            return a.equals(b);
        }

        @Override
        public boolean isAlly(UUID a, UUID b) {
            return a.equals(b);
        }

        @Override
        public boolean isEnemy(UUID a, UUID b) {
            return false;
        }
    };
}
