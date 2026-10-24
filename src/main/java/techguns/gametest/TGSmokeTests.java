package techguns.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;

/**
 * Basic tests proving the mod loads and the GameTest setup works. Run with {@code gradlew runGameTestServer}.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGSmokeTests {
    @GameTest(template = "empty")
    public static void modLoads(GameTestHelper helper) {
        helper.assertTrue(TGIds.id("test").getNamespace().equals(Techguns.MODID), "TGIds must use the techguns namespace");
        helper.succeed();
    }
}
