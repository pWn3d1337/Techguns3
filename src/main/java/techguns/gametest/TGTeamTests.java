package techguns.gametest;

import java.util.UUID;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.compat.team.TGTeams;

/**
 * Team sources behind {@link TGTeams}. Own batch: a scoreboard team makes the team settings available to every test
 * running at the same time.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGTeamTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA, batch = "teams")
    public static void vanillaTeamsMakeMembers(GameTestHelper helper) {
        // offline players as UUID score holders (the test server has no profile cache, and a placed mock player would
        // log in and get mod login packets the test can't take)
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        Scoreboard scoreboard = helper.getLevel().getServer().getScoreboard();
        PlayerTeam team = scoreboard.addPlayerTeam("tg_gametest");
        try {
            scoreboard.addPlayerToTeam(a.toString(), team);
            scoreboard.addPlayerToTeam(b.toString(), team);
            helper.assertTrue(TGTeams.hasTeams(), "A scoreboard team turns the team settings on");
            helper.assertTrue(TGTeams.isMember(a, b) && TGTeams.isAlly(a, b),
                    "Same scoreboard team: members and allies");
            helper.assertFalse(TGTeams.isMember(a, c), "Not on the team: no member");
            helper.assertFalse(TGTeams.shouldAttack(a, b, TGTeams.PVP_NOT_TEAM), "Turrets spare team members");
            helper.assertTrue(TGTeams.shouldAttack(a, c, TGTeams.PVP_NOT_TEAM), "Turrets attack others");
            helper.assertTrue(TGTeams.canAccess(a, b, TGTeams.SECURITY_TEAM)
                    && !TGTeams.canAccess(a, c, TGTeams.SECURITY_TEAM), "Team security lets members in only");
            helper.assertTrue(TGTeams.nextSecurity(TGTeams.SECURITY_PUBLIC) == TGTeams.SECURITY_ALLIES
                    && TGTeams.nextPvpSetting(TGTeams.PVP_NONE) == TGTeams.PVP_ENEMIES, "With teams all settings cycle");
        } finally {
            scoreboard.removePlayerTeam(team);
        }
        helper.assertFalse(TGTeams.hasTeams() && !modTeams(), "Without scoreboard teams (and team mods) the team settings are off");
        helper.assertTrue(TGTeams.nextSecurity(TGTeams.SECURITY_PUBLIC) == (modTeams() ? TGTeams.SECURITY_ALLIES : TGTeams.SECURITY_OWNER),
                "Without teams security toggles public / owner");
        helper.succeed();
    }

    /** The team mod providers load and answer (with {@code -Pcompat_mods=true}); strangers are on no team together. */
    @GameTest(template = ARENA, batch = "teams")
    public static void teamModProvidersAnswer(GameTestHelper helper) {
        int expected = 1 + (ModList.get().isLoaded("ftbteams") ? 1 : 0) + (ModList.get().isLoaded("openpartiesandclaims") ? 1 : 0);
        helper.assertTrue(TGTeams.providers().size() == expected, "Vanilla teams plus one provider per team mod, got " + TGTeams.providers());
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        for (var provider : TGTeams.providers()) {
            helper.assertFalse(provider.isMember(a, b) || provider.isAlly(a, b) || provider.isEnemy(a, b),
                    provider.getClass().getSimpleName() + ": strangers are neither members, allies nor enemies");
        }
        helper.succeed();
    }

    private static boolean modTeams() {
        return ModList.get().isLoaded("ftbteams") || ModList.get().isLoaded("openpartiesandclaims");
    }
}
