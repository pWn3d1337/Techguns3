package techguns.gametest;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.MeleeToolItem;
import techguns.gun.ammo.AmmoType;
import techguns.recipe.AmmoChangeRecipe;
import techguns.recipe.MiningHeadUpgradeRecipe;
import techguns.recipe.TransferAmmoRecipe;
import techguns.registry.TGAmmo;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;

/**
 * Crafting recipe serializers (Phase 3 batch 3g): ammo variant change, mining head swap, guns made from guns keeping
 * their rounds.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGRecipeTests {
    private static final String ARENA = "empty_8x4x8";

    private static ItemStack gun(net.minecraft.world.item.Item item, int loaded, String variant) {
        ItemStack stack = new ItemStack(item);
        GunData.setAmmo(stack, new GunAmmo(loaded, variant));
        return stack;
    }

    @GameTest(template = ARENA)
    public static void ammoChangeLoadsNewVariant(GameTestHelper helper) {
        AmmoChangeRecipe recipe = new AmmoChangeRecipe(CraftingBookCategory.EQUIPMENT);
        CraftingInput input = CraftingInput.of(2, 1, List.of(gun(TGGuns.M4.get(), 15, AmmoType.DEFAULT),
                new ItemStack(TGAmmo.ASSAULT_RIFLE_MAGAZINE_INCENDIARY.get())));

        helper.assertTrue(recipe.matches(input, helper.getLevel()), "M4 + incendiary magazine should match");
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(input);

        helper.assertTrue(GunData.loaded(result) == 30 && AmmoType.INCENDIARY.equals(GunData.variant(result)),
                "The M4 should be loaded with 30 incendiary rounds, was " + GunData.ammo(result));
        // 15 of 30 shots = 1 of the magazine's 3 rifle rounds, plus the empty magazine
        helper.assertTrue(remaining.get(0).is(TGAmmo.RIFLE_ROUNDS.get()) && remaining.get(0).getCount() == 1,
                "The gun slot should give back 1 rifle round, was " + remaining.get(0));
        helper.assertTrue(remaining.get(1).is(TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get()), "The ammo slot should give back the empty magazine");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void ammoChangeReturnsFullMagazine(GameTestHelper helper) {
        AmmoChangeRecipe recipe = new AmmoChangeRecipe(CraftingBookCategory.EQUIPMENT);
        CraftingInput full = CraftingInput.of(2, 1, List.of(gun(TGGuns.M4.get(), 30, AmmoType.DEFAULT),
                new ItemStack(TGAmmo.ASSAULT_RIFLE_MAGAZINE_INCENDIARY.get())));
        CraftingInput same = CraftingInput.of(2, 1, List.of(gun(TGGuns.M4.get(), 30, AmmoType.DEFAULT),
                new ItemStack(TGAmmo.ASSAULT_RIFLE_MAGAZINE.get())));

        NonNullList<ItemStack> remaining = recipe.getRemainingItems(full);

        helper.assertTrue(remaining.get(0).is(TGAmmo.ASSAULT_RIFLE_MAGAZINE.get()), "A full M4 should give back a full magazine, was " + remaining.get(0));
        helper.assertFalse(recipe.matches(same, helper.getLevel()), "The variant the gun already has should not match");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void miningHeadSwapReturnsOldHead(GameTestHelper helper) {
        MiningHeadUpgradeRecipe recipe = new MiningHeadUpgradeRecipe(CraftingBookCategory.EQUIPMENT);
        MeleeToolItem chainsaw = (MeleeToolItem) TGGuns.CHAINSAW.get();
        CraftingInput input = CraftingInput.of(2, 1, List.of(gun(chainsaw, 100, AmmoType.DEFAULT),
                new ItemStack(TGItems.CHAINSAW_BLADES_CARBON.get())));

        helper.assertTrue(recipe.matches(input, helper.getLevel()), "Chainsaw + carbon blades should match");
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(input);

        helper.assertTrue(chainsaw.headLevel(result) == 2, "The chainsaw should get head level 2, was " + chainsaw.headLevel(result));
        helper.assertTrue(GunData.loaded(result) == 100, "The swap should keep the fuel");
        helper.assertTrue(remaining.get(0).is(TGItems.CHAINSAW_BLADES_STEEL.get()), "The steel blades should come back, was " + remaining.get(0));
        helper.assertFalse(recipe.matches(CraftingInput.of(2, 1, List.of(result, new ItemStack(TGItems.CHAINSAW_BLADES_CARBON.get()))),
                helper.getLevel()), "The installed head should not match");
        helper.assertFalse(recipe.matches(CraftingInput.of(2, 1, List.of(gun(chainsaw, 0, AmmoType.DEFAULT),
                new ItemStack(TGItems.POWER_HAMMER_HEAD_CARBON.get()))), helper.getLevel()), "Another tool's head should not match");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void goldenRevolverKeepsRounds(GameTestHelper helper) {
        RecipeHolder<CraftingRecipe> holder = helper.getLevel().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(r -> r.value() instanceof TransferAmmoRecipe && r.value().getResultItem(null).is(TGGuns.GOLDEN_REVOLVER.get()))
                .findFirst().orElseThrow();
        ItemStack gold = new ItemStack(Items.GOLD_INGOT);
        CraftingInput input = CraftingInput.of(3, 3, List.of(gold, gold, gold, gold, gun(TGGuns.REVOLVER.get(), 4, AmmoType.DEFAULT),
                gold, gold, gold, gold));

        helper.assertTrue(holder.value().matches(input, helper.getLevel()), "Revolver in gold should match");
        ItemStack result = holder.value().assemble(input, helper.getLevel().registryAccess());

        helper.assertTrue(result.is(TGGuns.GOLDEN_REVOLVER.get()) && GunData.loaded(result) == 4,
                "The golden revolver should keep the 4 rounds, was " + GunData.ammo(result));
        helper.succeed();
    }
}
