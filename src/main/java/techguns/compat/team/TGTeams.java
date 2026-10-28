package techguns.compat.team;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import net.neoforged.fml.ModList;

/**
 * Team relations for turrets and owned machines (Techguns 2 {@code TGNpcFactions} player checks); game code only
 * talks to this facade, never to a team mod. All sources count at once (design doc §5.4): members or allies if any
 * source says so, enemies only if one says so and none makes them members or allies.
 */
public final class TGTeams {
    /** PvP settings of turrets (Techguns 2 {@code pvpsetting}); without teams only none and everyone. */
    public static final int PVP_NONE = 0;
    public static final int PVP_ENEMIES = 1;
    public static final int PVP_NEUTRALS = 2;
    public static final int PVP_NOT_TEAM = 3;
    public static final int PVP_EVERYONE = 4;

    /** Security of owned machines (Techguns 2 {@code security}); without teams only public and owner. */
    public static final int SECURITY_PUBLIC = 0;
    public static final int SECURITY_ALLIES = 1;
    public static final int SECURITY_TEAM = 2;
    public static final int SECURITY_OWNER = 3;

    private static final List<TeamProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private TGTeams() {
    }

    /** Vanilla scoreboard teams always, the team mods when they are installed (their classes load only then). */
    public static void registerProviders() {
        PROVIDERS.clear();
        PROVIDERS.add(new VanillaTeamProvider());
        if (ModList.get().isLoaded("ftbteams")) {
            PROVIDERS.add(new FtbTeamsProvider());
        }
        if (ModList.get().isLoaded("openpartiesandclaims")) {
            PROVIDERS.add(new OpenPartiesProvider());
        }
    }

    public static List<TeamProvider> providers() {
        return List.copyOf(PROVIDERS);
    }

    /** Whether any source has teams (all PvP and security settings make sense). */
    public static boolean hasTeams() {
        return PROVIDERS.stream().anyMatch(TeamProvider::isActive);
    }

    public static boolean isMember(UUID a, UUID b) {
        return a.equals(b) || PROVIDERS.stream().anyMatch(p -> p.isMember(a, b));
    }

    public static boolean isAlly(UUID a, UUID b) {
        return isMember(a, b) || PROVIDERS.stream().anyMatch(p -> p.isAlly(a, b));
    }

    public static boolean isEnemy(UUID a, UUID b) {
        return !isAlly(a, b) && PROVIDERS.stream().anyMatch(p -> p.isEnemy(a, b));
    }

    /** The next PvP setting: all five with teams, otherwise none / everyone. */
    public static int nextPvpSetting(int setting) {
        return hasTeams() ? (setting + 1) % 5 : setting == PVP_NONE ? PVP_EVERYONE : PVP_NONE;
    }

    /** The next security setting: all four with teams (Techguns 2 with FTB Lib), otherwise public / owner. */
    public static int nextSecurity(int security) {
        return hasTeams() ? (security + 1) % 4 : security == SECURITY_PUBLIC ? SECURITY_OWNER : SECURITY_PUBLIC;
    }

    /** Techguns 2 {@code canAccess}: whether {@code player} may use a machine of {@code owner}. */
    public static boolean canAccess(UUID owner, UUID player, int security) {
        if (owner.equals(player)) {
            return true;
        }
        return switch (security) {
            case SECURITY_PUBLIC -> true;
            case SECURITY_ALLIES -> isAlly(owner, player);
            case SECURITY_TEAM -> isMember(owner, player);
            default -> false;
        };
    }

    /** Techguns 2 {@code shouldAttack}: whether a turret of {@code owner} attacks {@code target}'s player or turrets. */
    public static boolean shouldAttack(UUID owner, UUID target, int pvpSetting) {
        if (owner.equals(target)) {
            return false;
        }
        return switch (pvpSetting) {
            case PVP_ENEMIES -> isEnemy(owner, target);
            case PVP_NEUTRALS -> !isAlly(owner, target);
            case PVP_NOT_TEAM -> !isMember(owner, target);
            case PVP_EVERYONE -> true;
            default -> false;
        };
    }

    /** Techguns 2 {@code isHostile}: whether a turret of {@code owner} fights back against {@code attacker}. */
    public static boolean isHostile(UUID owner, UUID attacker) {
        return !isAlly(owner, attacker);
    }
}
