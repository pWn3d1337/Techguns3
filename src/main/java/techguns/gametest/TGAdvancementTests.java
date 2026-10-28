package techguns.gametest;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;
import techguns.data.TGAdvancementProvider;

/** The Techguns advancement tree: every entry is loaded, has its display and leads back to the root. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGAdvancementTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void advancementTreeLoads(GameTestHelper helper) {
        var advancements = helper.getLevel().getServer().getAdvancements();
        for (String[] entry : TGAdvancementProvider.ENTRIES) {
            AdvancementHolder holder = advancements.get(TGIds.id(entry[0]));
            helper.assertTrue(holder != null && holder.value().display().isPresent(), entry[0] + " is a displayed advancement");
            AdvancementHolder node = holder;
            int depth = 0;
            while (node.value().parent().isPresent() && depth++ < 10) {
                node = advancements.get(node.value().parent().get());
                helper.assertTrue(node != null, entry[0] + ": parent exists");
            }
            helper.assertTrue(node.id().equals(TGIds.id("root")), entry[0] + " leads back to the root, got " + node.id());
        }
        helper.succeed();
    }
}
