package techguns.compat.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import techguns.TGIds;
import techguns.recipe.MachineInput;
import techguns.recipe.UpgradeBenchRecipe;
import techguns.registry.TGArmors;
import techguns.registry.TGBlocks;

/**
 * Techguns 2 {@code UpgradeBenchJeiRecipeCategory}: an armor piece and an upgrade item give the armor with the
 * upgrade's enchantment; armor and result are linked, so focusing one armor shows its upgraded copy.
 */
public class UpgradeBenchCategory extends TGRecipeCategory<RecipeHolder<UpgradeBenchRecipe>> {
    public static final RecipeType<RecipeHolder<UpgradeBenchRecipe>> TYPE = RecipeType.createRecipeHolderType(TGIds.id("upgrade_bench"));

    public UpgradeBenchCategory(IGuiHelper helper) {
        super(helper, TYPE, "techguns.container.upgradebench", TGIds.id("textures/gui/upgrade_bench_gui.png"), TGBlocks.UPGRADE_BENCH.get(), false);
    }

    @Override
    protected int powerPerTick(RecipeHolder<UpgradeBenchRecipe> holder) {
        return 0;
    }

    @Override
    protected int time(RecipeHolder<UpgradeBenchRecipe> holder) {
        return 0;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<UpgradeBenchRecipe> holder, IFocusGroup focuses) {
        UpgradeBenchRecipe recipe = holder.value();
        HolderLookup.Provider registries = Minecraft.getInstance().level.registryAccess();
        ItemStack upgrade = recipe.upgrade().getItems().length > 0 ? recipe.upgrade().getItems()[0] : ItemStack.EMPTY;
        List<ItemStack> armors = new ArrayList<>();
        List<ItemStack> results = new ArrayList<>();
        for (var armor : TGArmors.armors()) {
            ItemStack stack = new ItemStack(armor.get());
            MachineInput input = MachineInput.of(stack, upgrade);
            if (recipe.matches(input, Minecraft.getInstance().level)) {
                armors.add(stack);
                results.add(recipe.assemble(input, registries));
            }
        }
        IRecipeSlotBuilder armorSlot = slot(builder, RecipeIngredientRole.INPUT, 17, 47).addItemStacks(armors);
        slot(builder, RecipeIngredientRole.INPUT, 53, 47).addIngredients(recipe.upgrade());
        IRecipeSlotBuilder resultSlot = slot(builder, RecipeIngredientRole.OUTPUT, 116, 47).addItemStacks(results);
        builder.createFocusLink(armorSlot, resultSlot);
    }
}
