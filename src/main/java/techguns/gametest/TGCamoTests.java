package techguns.gametest;

import java.util.List;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.camo.CamoData;
import techguns.camo.TGCamo;
import techguns.registry.TGArmors;
import techguns.registry.TGComponents;
import techguns.registry.TGGuns;

/**
 * Camos (Phase 4 batch 4d): the {@code techguns:camo} datapack registry lists each item's camos, the stack stores the
 * camo name, cycling wraps around, sneak + use switches armor camo, armor renders the camo's layer texture.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGCamoTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void gunCamosCycleAndWrap(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        ItemStack m4 = new ItemStack(TGGuns.M4.get());
        List<TGCamo> camos = CamoData.camos(registries, m4);

        helper.assertTrue(camos.size() >= 2, "The M4 should have camos, has " + camos.size());
        helper.assertTrue(CamoData.current(registries, m4).orElseThrow().order() == 0, "A new M4 should show its default camo");
        CamoData.cycle(registries, m4, false);
        helper.assertTrue(camos.get(1).name().equals(m4.get(TGComponents.CAMO.get())), "Cycling should store the next camo name");
        CamoData.cycle(registries, m4, true);
        helper.assertFalse(m4.has(TGComponents.CAMO.get()), "Back to the default camo should remove the component");
        CamoData.cycle(registries, m4, true);
        helper.assertTrue(CamoData.current(registries, m4).orElseThrow().equals(camos.getLast()), "Cycling back from the default should wrap");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void unknownCamoFallsBackToDefault(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        ItemStack m4 = new ItemStack(TGGuns.M4.get());
        m4.set(TGComponents.CAMO.get(), "does_not_exist");

        helper.assertTrue(CamoData.current(registries, m4).orElseThrow().order() == 0, "An unknown camo should show the default");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void armorIconTintsFollowCamo(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        ItemStack vest = new ItemStack(TGArmors.T2_COMBAT_CHESTPLATE.get());
        TGCamo urban = CamoData.current(registries, vest).orElseThrow();
        TGCamo desert = CamoData.camos(registries, vest).stream().filter(camo -> camo.name().equals("desert")).findFirst().orElseThrow();

        helper.assertTrue(urban.tints().size() == 3, "The T2 combat vest icon has three tinted layers, has " + urban.tints().size());
        helper.assertTrue(urban.tint(0) == 0xFF82A780, "The urban camo should tint the vest green, is " + Integer.toHexString(urban.tint(0)));
        helper.assertTrue(desert.tint(1) == 0xFFC29971, "The desert camo should tint layer 1 brown, is " + Integer.toHexString(desert.tint(1)));
        helper.assertTrue(urban.tint(3) == -1, "A layer without a tint should stay untinted");
        TGCamo hev = CamoData.current(registries, new ItemStack(TGArmors.T3_MINER_HELMET.get())).orElseThrow();
        helper.assertTrue(hev.tint(1) == -1 && hev.tint(2) == 0xFF8BC2FF, "The HEV helmet's middle layer stays untinted, the visor is blue");
        helper.assertTrue(CamoData.current(registries, new ItemStack(TGArmors.STEAM_HELMET.get())).orElseThrow().tints().isEmpty(),
                "Steam armor icons are 3D models without tints");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void sneakUseSwitchesArmorCamo(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack vest = new ItemStack(TGArmors.T2_COMBAT_CHESTPLATE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, vest);
        player.setShiftKeyDown(true);

        vest.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        TGCamo camo = CamoData.current(helper.getLevel().registryAccess(), vest).orElseThrow();
        helper.assertTrue(camo.order() == 1, "Sneak + use should switch to the second camo, is " + camo.name());
        helper.assertTrue(player.getItemBySlot(EquipmentSlot.CHEST).isEmpty(), "Sneak + use should not equip the armor");
        ResourceLocation texture = vest.getItem().getArmorTexture(vest, player, EquipmentSlot.CHEST, null, false);
        helper.assertTrue(texture != null && texture.getPath().equals("textures/models/armor/" + camo.layer().orElseThrow().getPath() + "_layer_1.png"),
                "The armor should render the camo's layer texture, was " + texture);
        helper.succeed();
    }
}
