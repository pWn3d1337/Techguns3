package techguns.compat.team;

import java.util.UUID;

/**
 * Team relations for turrets and owned machines (Techguns 2 {@code TGNpcFactions} player checks); game code only
 * talks to this facade, never to a team mod.
 */
public final class TGTeams {
    /** PvP settings of turrets (Techguns 2 {@code pvpsetting}); without a team mod only none and everyone. */
    public static final int PVP_NONE = 0;
    public static final int PVP_ENEMIES = 1;
    public static final int PVP_NEUTRALS = 2;
    public static final int PVP_NOT_TEAM = 3;
    public static final int PVP_EVERYONE = 4;

    private static TeamProvider provider = TeamProvider.NONE;

    private TGTeams() {
    }

    public static void setProvider(TeamProvider teamProvider) {
        provider = teamProvider;
    }

    /** Whether a team mod answers the questions (all five PvP settings make sense). */
    public static boolean hasTeams() {
        return provider != TeamProvider.NONE;
    }

    /** The next PvP setting: all five with a team mod, otherwise none / everyone. */
    public static int nextPvpSetting(int setting) {
        return hasTeams() ? (setting + 1) % 5 : setting == PVP_NONE ? PVP_EVERYONE : PVP_NONE;
    }

    /** Techguns 2 {@code shouldAttack}: whether a turret of {@code owner} attacks {@code target}'s player or turrets. */
    public static boolean shouldAttack(UUID owner, UUID target, int pvpSetting) {
        if (owner.equals(target)) {
            return false;
        }
        return switch (pvpSetting) {
            case PVP_ENEMIES -> provider.isEnemy(owner, target);
            case PVP_NEUTRALS -> !provider.isAlly(owner, target);
            case PVP_NOT_TEAM -> !provider.isMember(owner, target);
            case PVP_EVERYONE -> true;
            default -> false;
        };
    }

    /** Techguns 2 {@code isHostile}: whether a turret of {@code owner} fights back against {@code attacker}. */
    public static boolean isHostile(UUID owner, UUID attacker) {
        return !provider.isAlly(owner, attacker);
    }
}
