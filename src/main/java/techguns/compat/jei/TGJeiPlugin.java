package techguns.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import techguns.TGIds;
import techguns.client.gui.machine.AmmoPressScreen;
import techguns.client.gui.machine.BlastFurnaceScreen;
import techguns.client.gui.machine.ChargingStationScreen;
import techguns.client.gui.machine.FabricatorScreen;
import techguns.client.gui.machine.GrinderScreen;
import techguns.client.gui.machine.MetalPressScreen;
import techguns.registry.TGBlocks;
import techguns.registry.TGRecipes;

/**
 * Techguns 2 {@code TGJeiPlugin}: recipe categories for the Techguns machines, their catalysts and the click areas on
 * the machine screens. JEI finds it by the annotation; without JEI it is never loaded.
 */
// MIGRATION(26.x): JEI API of the new game version.
@JeiPlugin
public class TGJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = TGIds.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime runtime) {
        techguns.client.RecipeViewerHook.setShowCategory(id -> {
            if (id.equals("held")) {
                // the recipes using the held item (the special crafting examples of a gun or tool)
                var stack = Minecraft.getInstance().player.getMainHandItem();
                if (stack.isEmpty()) {
                    return false;
                }
                runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(
                        mezz.jei.api.recipe.RecipeIngredientRole.INPUT, mezz.jei.api.constants.VanillaTypes.ITEM_STACK, stack));
                return true;
            }
            return runtime.getJeiHelpers().getRecipeType(TGIds.id(id)).map(type -> {
                runtime.getRecipesGui().showTypes(java.util.List.of(type));
                return true;
            }).orElse(false);
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new MetalPressCategory(helper), new AmmoPressCategory(helper), new BlastFurnaceCategory(helper),
                new GrinderCategory(helper), new ChargingCategory(helper), new FabricatorCategory(helper), new ChemLabCategory(helper),
                new ReactionChamberCategory(helper), new OreDrillCategory(helper), new UpgradeBenchCategory(helper), new CamoBenchCategory(helper),
                new AmmoBenchCategory(helper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipes = Minecraft.getInstance().level.getRecipeManager();
        add(registration, recipes, MetalPressCategory.TYPE, TGRecipes.METAL_PRESS.get());
        add(registration, recipes, AmmoPressCategory.TYPE, TGRecipes.AMMO_PRESS.get());
        add(registration, recipes, BlastFurnaceCategory.TYPE, TGRecipes.BLAST_FURNACE.get());
        add(registration, recipes, GrinderCategory.TYPE, TGRecipes.GRINDER.get());
        add(registration, recipes, ChargingCategory.TYPE, TGRecipes.CHARGING.get());
        add(registration, recipes, FabricatorCategory.TYPE, TGRecipes.FABRICATOR.get());
        add(registration, recipes, ChemLabCategory.TYPE, TGRecipes.CHEM_LAB.get());
        add(registration, recipes, ReactionChamberCategory.TYPE, TGRecipes.REACTION_CHAMBER.get());
        add(registration, recipes, OreDrillCategory.TYPE, TGRecipes.ORE_CLUSTER.get());
        add(registration, recipes, UpgradeBenchCategory.TYPE, TGRecipes.UPGRADE_BENCH.get());
        add(registration, recipes, CamoBenchCategory.TYPE, TGRecipes.CAMO_CHANGE.get());
        add(registration, recipes, AmmoBenchCategory.TYPE, TGRecipes.AMMO_BENCH.get());
        // the ammo change and mining head recipes are special recipes: shown through examples
        registration.addRecipes(mezz.jei.api.constants.RecipeTypes.CRAFTING,
                SpecialCraftingExamples.all(Minecraft.getInstance().level.registryAccess()));
    }

    private static <I extends net.minecraft.world.item.crafting.RecipeInput, R extends Recipe<I>> void add(IRecipeRegistration registration, RecipeManager recipes, RecipeType<RecipeHolder<R>> type,
                                                 net.minecraft.world.item.crafting.RecipeType<R> vanillaType) {
        registration.addRecipes(type, recipes.getAllRecipesFor(vanillaType));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(TGBlocks.METAL_PRESS.get(), MetalPressCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.AMMO_PRESS.get(), AmmoPressCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.BLAST_FURNACE.get(), BlastFurnaceCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.GRINDER.get(), GrinderCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.CHARGING_STATION.get(), ChargingCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.FABRICATOR_CONTROLLER.get(), FabricatorCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.CHEM_LAB.get(), ChemLabCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.REACTION_CHAMBER_CONTROLLER.get(), ReactionChamberCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.ORE_DRILL_CONTROLLER.get(), OreDrillCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.UPGRADE_BENCH.get(), UpgradeBenchCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.CAMO_BENCH.get(), CamoBenchCategory.TYPE);
        registration.addRecipeCatalyst(TGBlocks.AMMO_BENCH.get(), AmmoBenchCategory.TYPE);
    }

    /** Techguns 2 click areas (on the progress drawings). */
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(MetalPressScreen.class, 119, 36, 19, 22, MetalPressCategory.TYPE);
        registration.addRecipeClickArea(AmmoPressScreen.class, 119, 36, 19, 22, AmmoPressCategory.TYPE);
        registration.addRecipeClickArea(BlastFurnaceScreen.class, 18, 52, 90, 12, BlastFurnaceCategory.TYPE);
        registration.addRecipeClickArea(GrinderScreen.class, 30, 38, 43, 23, GrinderCategory.TYPE);
        registration.addRecipeClickArea(ChargingStationScreen.class, 38, 18, 28, 12, ChargingCategory.TYPE);
        registration.addRecipeClickArea(FabricatorScreen.class, 20, 52, 96, 12, FabricatorCategory.TYPE);
        registration.addRecipeClickArea(techguns.client.gui.machine.ChemLabScreen.class, 80, 15, 52, 30, ChemLabCategory.TYPE);
        registration.addRecipeClickArea(techguns.client.gui.machine.ReactionChamberScreen.class, 66, 60, 102, 14, ReactionChamberCategory.TYPE);
        registration.addRecipeClickArea(techguns.client.gui.machine.UpgradeBenchScreen.class, 73, 40, 38, 30, UpgradeBenchCategory.TYPE);
    }
}
