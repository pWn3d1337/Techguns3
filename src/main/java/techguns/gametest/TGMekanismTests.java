package techguns.gametest;

import mekanism.api.radiation.capability.IRadiationShielding;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;
import techguns.compat.mekanism.MekanismRadiation;
import techguns.compat.mekanism.MekanismRadiationRules;
import techguns.machine.OreDrillBlockEntity;
import techguns.registry.TGArmors;
import techguns.registry.TGTags;

/**
 * 8f2: the Mekanism radiation bridge (with {@code -Pcompat_mods=true}) and the fuel tag. Mekanism classes are only
 * touched with Mekanism installed.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGMekanismTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void hazmatShieldsBothWays(GameTestHelper helper) {
        if (!ModList.get().isLoaded("mekanism")) {
            helper.succeed();
            return;
        }
        Mekanism.check(helper);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void radRegenerationDecaysMekanismRadiation(GameTestHelper helper) {
        helper.assertTrue(Math.abs(MekanismRadiationRules.decay(14) - 0.7) < 1.0e-9, "RadAway (XV) keeps 70% per second");
        helper.assertTrue(Math.pow(MekanismRadiationRules.decay(14), 20) < 0.001, "RadAway's 20 s clear Mekanism radiation");
        helper.assertTrue(MekanismRadiationRules.decay(100) == 0.0, "never negative");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void otherModsFuelsFillTanksAndDrills(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        helper.assertTrue(recipes.byKey(TGIds.id("chem_lab/fuel_tank_from_fuel")).isPresent(), "Chem Lab fuel tank from any fuel");
        var fuels = BuiltInRegistries.FLUID.getTag(TGTags.Fluids.FUEL);
        boolean fuelMod = ModList.get().isLoaded("modern_industrialization") || ModList.get().isLoaded("immersiveengineering")
                || ModList.get().isLoaded("oritech");
        helper.assertTrue(fuels.isPresent() && fuels.get().size() > 0 == fuelMod, "Fuels only from fuel mods, got "
                + fuels.map(tag -> tag.size()).orElse(0));
        fuels.ifPresent(tag -> tag.forEach(fluid -> helper.assertTrue(
                OreDrillBlockEntity.fuelValue(new FluidStack(fluid.value(), 1000)) > 0.0f, fluid.getRegisteredName() + " burns in the Ore Drill")));
        helper.assertTrue(helper.getLevel().registryAccess().registryOrThrow(Registries.FLUID).getTag(TGTags.Fluids.FUEL).isPresent()
                || !fuelMod, "fuel tag bound");
        helper.succeed();
    }

    private static final class Mekanism {
        private static final ItemCapability<IRadiationShielding, Void> SHIELDING = ItemCapability.createVoid(
                TGIds.of("mekanism", "radiation_shielding"), IRadiationShielding.class);

        static void check(GameTestHelper helper) {
            double total = 0.0;
            for (var item : new ItemStack[] {new ItemStack(TGArmors.HAZMAT_HELMET.get()), new ItemStack(TGArmors.HAZMAT_CHESTPLATE.get()),
                    new ItemStack(TGArmors.HAZMAT_LEGGINGS.get()), new ItemStack(TGArmors.HAZMAT_BOOTS.get())}) {
                IRadiationShielding shielding = item.getCapability(SHIELDING);
                helper.assertTrue(shielding != null, item + " has Mekanism shielding");
                total += shielding.getRadiationShielding();
            }
            helper.assertTrue(Math.abs(total - 1.0) < 1.0e-6, "A Techguns hazmat suit shields fully against Mekanism radiation, got " + total);
            ItemStack chest = new ItemStack(Items.IRON_CHESTPLATE);
            helper.assertTrue(chest.getCapability(SHIELDING) == null, "Plain armor gets no shielding");

            ItemStack mask = new ItemStack(BuiltInRegistries.ITEM.get(TGIds.of("mekanism", "hazmat_mask")));
            double resistance = MekanismRadiation.resistance(mask, EquipmentSlot.HEAD);
            helper.assertTrue(Math.abs(resistance - 1.0) < 1.0e-6, "Mekanism's hazmat mask gives Techguns radiation resistance 1, got " + resistance);
        }
    }
}
