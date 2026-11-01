package techguns.data;

import net.minecraft.resources.ResourceLocation;
import techguns.recipe.UpgradeBenchRecipe;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import techguns.recipe.ReactionChamberRecipe;
import techguns.registry.TGFluids;
import techguns.recipe.CamoChangeRecipe;
import techguns.recipe.FabricatorRecipe;
import techguns.recipe.AmmoBenchRecipe;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import techguns.TGIds;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.MeleeToolItem;
import techguns.gun.ammo.AmmoType;
import techguns.recipe.AmmoChangeRecipe;
import techguns.recipe.AmmoPressRecipe;
import techguns.recipe.BlastFurnaceRecipe;
import techguns.recipe.ChargingRecipe;
import techguns.recipe.ChemLabRecipe;
import techguns.recipe.OreClusterRecipe;
import techguns.recipe.GrinderRecipe;
import techguns.recipe.MetalPressRecipe;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import techguns.recipe.MiningHeadUpgradeRecipe;
import techguns.registry.TGAmmo;
import techguns.registry.TGArmors;
import techguns.registry.TGBlocks;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;
import techguns.registry.TGSlotItems;
import techguns.registry.TGTags;

/**
 * Crafting and cooking recipes. Recipes that need Techguns machines are added in Phase 5.
 */
public class TGRecipeProvider extends RecipeProvider {
    public TGRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        smeltingAndBlasting(output, List.of(TGItems.RAW_TIN.get(), TGBlocks.TIN_ORE.get(), TGBlocks.DEEPSLATE_TIN_ORE.get()),
                TGItems.TIN_INGOT.get(), 0.7f, "tin_ingot");
        smeltingAndBlasting(output, List.of(TGItems.RAW_LEAD.get(), TGBlocks.LEAD_ORE.get(), TGBlocks.DEEPSLATE_LEAD_ORE.get()),
                TGItems.LEAD_INGOT.get(), 0.8f, "lead_ingot");
        blastingOnly(output, List.of(TGItems.RAW_TITANIUM.get(), TGBlocks.TITANIUM_ORE.get(), TGBlocks.DEEPSLATE_TITANIUM_ORE.get()),
                TGItems.TITANIUM_INGOT.get(), 1.5f, 200, "titanium_ingot");
        smeltingAndBlasting(output, List.of(TGItems.RAW_RUBBER.get()), TGItems.RUBBER_BAR.get(), 0.5f, "rubber_bar");

        buildNuggetRecipes(output);
        buildMaterialRecipes(output);
        buildComponentRecipes(output);
        buildAmmoRecipes(output);
        buildGunRecipes(output);
        buildArmorRecipes(output);
        buildShieldRecipes(output);
        buildBlockRecipes(output);
        buildMachineRecipes(output);
        buildSlotItemRecipes(output);
        buildOreDrillRecipes(output);
        buildExplosiveChargeRecipes(output);
        buildTurretRecipes(output);
    }

    /** Techguns 2 turret base and armor plates. */
    private void buildTurretRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGBlocks.TURRET.get()).pattern("pip").pattern("cec").pattern("prp")
                .define('p', TGTags.Items.plate("iron")).define('i', TGItems.CIRCUIT_BOARD.get()).define('c', Tags.Items.CONCRETES)
                .define('e', TGItems.ELECTRIC_ENGINE.get()).define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(TGItems.ELECTRIC_ENGINE.get()), has(TGItems.ELECTRIC_ENGINE.get())).save(output);
        turretArmor(output, TGItems.TURRET_ARMOR_IRON.get(), "iron");
        turretArmor(output, TGItems.TURRET_ARMOR_STEEL.get(), "steel");
        turretArmor(output, TGItems.TURRET_ARMOR_OBSIDIAN_STEEL.get(), "obsidian_steel");
        turretArmor(output, TGItems.TURRET_ARMOR_CARBON.get(), "carbon");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(TGItems.TURRET_ARMOR_IRON.get()), RecipeCategory.MISC, new ItemStack(Items.IRON_INGOT, 5), 0.0f, 200)
                .unlockedBy(getHasName(TGItems.TURRET_ARMOR_IRON.get()), has(TGItems.TURRET_ARMOR_IRON.get()))
                .save(output, TGIds.id("iron_ingot_from_turret_armor"));
    }

    private void turretArmor(RecipeOutput output, Item plate, String material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, plate).pattern("ppp").pattern(" p ").pattern(" p ").define('p', TGTags.Items.plate(material))
                .unlockedBy("has_plate", has(TGTags.Items.plate(material))).save(output);
    }

    /** Techguns 2 mining charges. */
    private void buildExplosiveChargeRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.EXPLOSIVE_CHARGE.get(), 8).pattern("rtr").pattern("tct").pattern("rtr")
                .define('r', TGItems.RUBBER_BAR.get()).define('t', Items.TNT).define('c', TGItems.CIRCUIT_BOARD.get())
                .unlockedBy(getHasName(Items.TNT), has(Items.TNT)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.EXPLOSIVE_CHARGE_ADVANCED.get(), 4).pattern("rtr").pattern("tct").pattern("rtr")
                .define('r', TGItems.PLASTIC_SHEET.get()).define('t', TGItems.TGX.get()).define('c', TGItems.CIRCUIT_BOARD.get())
                .unlockedBy(getHasName(TGItems.TGX.get()), has(TGItems.TGX.get())).save(output);
    }

    /** Techguns 2 Ore Drill blocks and heads, and the ore clusters (Techguns 2 {@code TGOreClusters} and its config). */
    private void buildOreDrillRecipes(RecipeOutput output) {
        Item engine = TGItems.ELECTRIC_ENGINE.get();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.ORE_DRILL_FRAME.get(), 8).pattern("pbp").pattern("bpb").pattern("pbp")
                .define('p', TGTags.Items.plate("iron")).define('b', Items.IRON_BARS)
                .unlockedBy(getHasName(Items.IRON_BARS), has(Items.IRON_BARS)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.ORE_DRILL_SCAFFOLD.get(), 8).pattern("bbb").pattern("bpb").pattern("bbb")
                .define('p', TGTags.Items.plate("iron")).define('b', Items.IRON_BARS)
                .unlockedBy(getHasName(Items.IRON_BARS), has(Items.IRON_BARS)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.ORE_DRILL_ROD.get(), 4).pattern("ipi").pattern("ipi").pattern("ipi")
                .define('p', TGTags.Items.plate("iron")).define('i', Tags.Items.INGOTS_IRON)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Tags.Items.INGOTS_IRON)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.ORE_DRILL_ENGINE.get(), 4).pattern("bbb").pattern("beb").pattern("bbb")
                .define('b', Items.IRON_BARS).define('e', engine).unlockedBy(getHasName(engine), has(engine)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.ORE_DRILL_CONTROLLER.get()).pattern("pcp").pattern("cec").pattern("pcp")
                .define('p', TGTags.Items.plate("iron")).define('c', TGItems.CIRCUIT_BOARD.get()).define('e', engine)
                .unlockedBy(getHasName(engine), has(engine)).save(output);

        drillHeads(output, TGItems.ORE_DRILL_HEAD_SMALL_STEEL.get(), TGItems.ORE_DRILL_HEAD_MEDIUM_STEEL.get(), TGItems.ORE_DRILL_HEAD_LARGE_STEEL.get(),
                b -> b.pattern("sps").pattern("sps").pattern(" s ").define('s', TGTags.Items.ingot("steel")).define('p', TGTags.Items.plate("steel")),
                TGItems.STEEL_PLATE.get());
        drillHeads(output, TGItems.ORE_DRILL_HEAD_SMALL_OBSIDIAN_STEEL.get(), TGItems.ORE_DRILL_HEAD_MEDIUM_OBSIDIAN_STEEL.get(),
                TGItems.ORE_DRILL_HEAD_LARGE_OBSIDIAN_STEEL.get(), b -> b.pattern("sps").pattern("sps").pattern(" s ")
                        .define('s', TGTags.Items.ingot("obsidian_steel")).define('p', TGTags.Items.plate("obsidian_steel")),
                TGItems.OBSIDIAN_STEEL_PLATE.get());
        drillHeads(output, TGItems.ORE_DRILL_HEAD_SMALL_CARBON.get(), TGItems.ORE_DRILL_HEAD_MEDIUM_CARBON.get(), TGItems.ORE_DRILL_HEAD_LARGE_CARBON.get(),
                b -> b.pattern("sss").pattern("sss").pattern(" s ").define('s', TGTags.Items.plate("carbon")), TGItems.CARBON_PLATE.get());

        oreCluster(output, TGBlocks.ORE_CLUSTER_COAL, 0, 10.0f, 0.1f, item(Items.COAL_ORE, 99), item(Items.DIAMOND, 1));
        oreCluster(output, TGBlocks.ORE_CLUSTER_COMMON_METAL, 0, 5.0f, 0.2f, item(Items.IRON_ORE, 45), item(Items.COPPER_ORE, 30),
                item(TGBlocks.TIN_ORE.get(), 25));
        oreCluster(output, TGBlocks.ORE_CLUSTER_RARE_METAL, 1, 2.5f, 0.4f, item(TGBlocks.LEAD_ORE.get(), 50), ore("osmium", 50), ore("aluminum", 50));
        oreCluster(output, TGBlocks.ORE_CLUSTER_SHINY_METAL, 2, 1.0f, 1.0f, item(TGBlocks.TITANIUM_ORE.get(), 20), item(Items.GOLD_ORE, 40),
                ore("silver", 40));
        oreCluster(output, TGBlocks.ORE_CLUSTER_URANIUM, 3, 0.5f, 1.0f, item(TGBlocks.URANIUM_ORE.get(), 50));
        oreCluster(output, TGBlocks.ORE_CLUSTER_COMMON_GEM, 1, 5.0f, 0.2f, item(Items.REDSTONE_ORE, 40), item(Items.LAPIS_ORE, 20),
                ore("certus_quartz", 40));
        oreCluster(output, TGBlocks.ORE_CLUSTER_SHINY_GEM, 3, 0.2f, 1.0f, item(Items.DIAMOND_ORE, 40), item(Items.EMERALD_ORE, 20));
        oreCluster(output, TGBlocks.ORE_CLUSTER_NETHER_CRYSTAL, 2, 4.0f, 0.5f, item(Items.NETHER_QUARTZ_ORE, 50), item(Items.GLOWSTONE, 40),
                item(Items.BLAZE_ROD, 10));
        oreCluster(output, TGBlocks.ORE_CLUSTER_OIL, 2, 4.0f, 1.0f, OreClusterRecipe.Entry.fluid(TGTags.Fluids.DRILL_OIL, 10));
    }

    /** Small head from {@code small}, medium from three small, large from three medium (Techguns 2). */
    private void drillHeads(RecipeOutput output, Item small, Item medium, Item large,
                            java.util.function.UnaryOperator<ShapedRecipeBuilder> smallPattern, Item unlock) {
        smallPattern.apply(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, small)).unlockedBy(getHasName(unlock), has(unlock)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, medium).pattern("d d").pattern(" d ").define('d', small)
                .unlockedBy(getHasName(small), has(small)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, large).pattern("d d").pattern(" d ").define('d', medium)
                .unlockedBy(getHasName(medium), has(medium)).save(output);
    }

    private static OreClusterRecipe.Entry item(net.minecraft.world.level.ItemLike item, int weight) {
        return OreClusterRecipe.Entry.item(Ingredient.of(item), weight);
    }

    /** Another mod's ore by its common tag; skipped while no mod fills the tag. */
    private static OreClusterRecipe.Entry ore(String material, int weight) {
        return OreClusterRecipe.Entry.item(Ingredient.of(TGTags.Items.ore(material)), weight);
    }

    private void oreCluster(RecipeOutput output, net.neoforged.neoforge.registries.DeferredBlock<? extends net.minecraft.world.level.block.Block> cluster,
                            int miningLevel, float oreMultiplier, float powerMultiplier, OreClusterRecipe.Entry... entries) {
        output.accept(TGIds.id("ore_cluster/" + cluster.getId().getPath().replace("ore_cluster_", "")),
                new OreClusterRecipe(cluster.get(), miningLevel, oreMultiplier, powerMultiplier, List.of(entries)), null);
    }

    /** Techguns 2 armor recipes. */
    private void buildArmorRecipes(RecipeOutput output) {
        Item cloth = TGItems.HEAVY_CLOTH.get();
        armor(output, TGArmors.T1_COMBAT_HELMET.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth), "iii", "c c");
        armor(output, TGArmors.T1_COMBAT_CHESTPLATE.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth),
                "c c", "iii", "ccc");
        armor(output, TGArmors.T1_COMBAT_LEGGINGS.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth),
                "iii", "c c", "c c");
        armor(output, TGArmors.T1_COMBAT_BOOTS.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth), "c c", "i i");

        // T1 miner (dyeYellow helmet, nuggetIron boots)
        Consumer<ShapedRecipeBuilder> minerKeys = b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth);
        armor(output, TGArmors.T1_MINER_HELMET.get(), cloth, b -> b.define('i', Tags.Items.INGOTS_IRON).define('c', cloth)
                .define('y', Tags.Items.DYES_YELLOW), "iyi", "c c");
        armor(output, TGArmors.T1_MINER_CHESTPLATE.get(), cloth, minerKeys, "c c", "ccc", "ici");
        armor(output, TGArmors.T1_MINER_LEGGINGS.get(), cloth, minerKeys, "ici", "c c", "c c");
        armor(output, TGArmors.T1_MINER_BOOTS.get(), cloth, b -> b.define('i', Tags.Items.NUGGETS_IRON).define('c', cloth), "c c", "i i");

        // T1 scout
        Consumer<ShapedRecipeBuilder> clothOnly = b -> b.define('c', cloth);
        armor(output, TGArmors.T1_SCOUT_HELMET.get(), cloth, clothOnly, "ccc", "c c");
        armor(output, TGArmors.T1_SCOUT_CHESTPLATE.get(), cloth, clothOnly, "c c", "ccc", "ccc");
        armor(output, TGArmors.T1_SCOUT_LEGGINGS.get(), cloth, clothOnly, "ccc", "c c", "c c");
        armor(output, TGArmors.T1_SCOUT_BOOTS.get(), cloth, clothOnly, "c c", "c c");

        // T2 combat
        TagKey<Item> obsidianSteel = TGTags.Items.ingot("obsidian_steel");
        Consumer<ShapedRecipeBuilder> combatKeys = b -> b.define('i', obsidianSteel).define('h', cloth);
        armor(output, TGArmors.T2_COMBAT_HELMET.get(), cloth, combatKeys, "iii", "h h");
        armor(output, TGArmors.T2_COMBAT_CHESTPLATE.get(), cloth, combatKeys, "h h", "iii", "iii");
        armor(output, TGArmors.T2_COMBAT_LEGGINGS.get(), cloth, combatKeys, "ihi", "i i", "h h");
        armor(output, TGArmors.T2_COMBAT_BOOTS.get(), cloth, combatKeys, "h h", "i i");

        // T2 commando
        Item rubber = TGItems.RUBBER_BAR.get();
        Consumer<ShapedRecipeBuilder> commandoKeys = b -> b.define('i', obsidianSteel).define('h', rubber);
        armor(output, TGArmors.T2_COMMANDO_HELMET.get(), rubber, b -> b.define('i', obsidianSteel).define('h', rubber)
                .define('g', Tags.Items.GLASS_PANES), "ihi", "hgh");
        armor(output, TGArmors.T2_COMMANDO_CHESTPLATE.get(), rubber, commandoKeys, "h h", "hih", "iii");
        armor(output, TGArmors.T2_COMMANDO_LEGGINGS.get(), rubber, commandoKeys, "hih", "i i", "h h");
        armor(output, TGArmors.T2_COMMANDO_BOOTS.get(), rubber, commandoKeys, "i i", "h h");

        // T3 combat
        Item fibers = TGItems.CARBON_FIBERS.get();
        Consumer<ShapedRecipeBuilder> t3Keys = b -> b.define('p', TGTags.Items.plate("carbon")).define('f', fibers);
        armor(output, TGArmors.T3_COMBAT_HELMET.get(), fibers, t3Keys, "ppp", "f f");
        armor(output, TGArmors.T3_COMBAT_CHESTPLATE.get(), fibers, t3Keys, "p p", "ppp", "fpf");
        armor(output, TGArmors.T3_COMBAT_LEGGINGS.get(), fibers, t3Keys, "pfp", "p p", "f f");
        armor(output, TGArmors.T3_COMBAT_BOOTS.get(), fibers, t3Keys, "f f", "p p");

        // T2 riot: upgrade of the matching T2 combat piece
        Item treated = TGItems.TREATED_LEATHER.get();
        Consumer<ShapedRecipeBuilder> riotKeys = b -> b.define('p', TGTags.Items.plate("lead")).define('l', treated);
        armor(output, TGArmors.T2_RIOT_HELMET.get(), treated,
                b -> riotKeys.accept(b.define('h', TGArmors.T2_COMBAT_HELMET.get())), "php", "l l");
        armor(output, TGArmors.T2_RIOT_CHESTPLATE.get(), treated,
                b -> riotKeys.accept(b.define('c', TGArmors.T2_COMBAT_CHESTPLATE.get())), "p p", "lcl", "lpl");
        armor(output, TGArmors.T2_RIOT_LEGGINGS.get(), treated,
                b -> riotKeys.accept(b.define('h', TGArmors.T2_COMBAT_LEGGINGS.get())), "lhl", "p p", "l l");
        armor(output, TGArmors.T2_RIOT_BOOTS.get(), treated,
                b -> riotKeys.accept(b.define('h', TGArmors.T2_COMBAT_BOOTS.get())), "lhl", "p p");

        // Beret
        armor(output, TGArmors.T2_BERET_ITEM.get(), cloth, clothOnly, " cc", "c c");

        // Hazmat suit
        Item fiber = TGItems.PROTECTIVE_FIBER.get();
        Consumer<ShapedRecipeBuilder> hazmatKeys = b -> b.define('f', fiber);
        armor(output, TGArmors.HAZMAT_HELMET.get(), fiber, hazmatKeys, "fff", "f f");
        armor(output, TGArmors.HAZMAT_CHESTPLATE.get(), fiber, hazmatKeys, "f f", "fff", "fff");
        armor(output, TGArmors.HAZMAT_LEGGINGS.get(), fiber, hazmatKeys, "fff", "f f", "f f");
        armor(output, TGArmors.HAZMAT_BOOTS.get(), fiber, hazmatKeys, "f f", "f f");

        // Steam armor
        Item steamPlate = TGItems.STEAM_ARMOR_PLATE.get();
        Consumer<ShapedRecipeBuilder> steamKeys = b -> b.define('s', steamPlate);
        armor(output, TGArmors.STEAM_HELMET.get(), steamPlate, steamKeys, "sss", "s s");
        armor(output, TGArmors.STEAM_CHESTPLATE.get(), steamPlate, steamKeys, "s s", "sss", "sss");
        armor(output, TGArmors.STEAM_LEGGINGS.get(), steamPlate, steamKeys, "sss", "s s", "s s");
        armor(output, TGArmors.STEAM_BOOTS.get(), steamPlate, steamKeys, "s s", "s s");

        // T3 power armor
        Item powerPlating = TGItems.POWER_PLATING.get();
        Item elite = TGItems.CIRCUIT_BOARD_ELITE.get();
        Item engine = TGItems.ELECTRIC_ENGINE.get();
        Map<Character, Object> powerKeys = Map.of('p', powerPlating, 'c', elite, 'e', engine, 'g', Tags.Items.GLASS_PANES);
        usedKeys(output, TGArmors.T3_POWER_HELMET.get(), powerPlating, powerKeys, "cpc", "pgp");
        usedKeys(output, TGArmors.T3_POWER_CHESTPLATE.get(), powerPlating, powerKeys, "p p", "ece", "ppp");
        usedKeys(output, TGArmors.T3_POWER_LEGGINGS.get(), powerPlating, powerKeys, "pcp", "e e", "p p");
        usedKeys(output, TGArmors.T3_POWER_BOOTS.get(), powerPlating, powerKeys, "e e", "p p");

        // T3 miner (HEV suit)
        Map<Character, Object> hevKeys = Map.of('p', TGTags.Items.plate("carbon"), 'c', elite,
                'l', TGTags.Items.plate("titanium"), 'g', Tags.Items.GLASS_PANES);
        usedKeys(output, TGArmors.T3_MINER_HELMET.get(), elite, hevKeys, "pcp", "lgl");
        usedKeys(output, TGArmors.T3_MINER_CHESTPLATE.get(), elite, hevKeys, "p p", "lcl", "lpl");
        usedKeys(output, TGArmors.T3_MINER_LEGGINGS.get(), elite, hevKeys, "ppp", "c c", "l l");
        usedKeys(output, TGArmors.T3_MINER_BOOTS.get(), elite, hevKeys, "l l", "p p");

        // T3 exo suit: upgrade of the matching T3 combat piece
        exoSuit(output, TGArmors.T3_EXO_HELMET.get(), TGArmors.T3_COMBAT_HELMET.get(), 1);
        exoSuit(output, TGArmors.T3_EXO_CHESTPLATE.get(), TGArmors.T3_COMBAT_CHESTPLATE.get(), 2);
        exoSuit(output, TGArmors.T3_EXO_LEGGINGS.get(), TGArmors.T3_COMBAT_LEGGINGS.get(), 2);
        exoSuit(output, TGArmors.T3_EXO_BOOTS.get(), TGArmors.T3_COMBAT_BOOTS.get(), 1);

        // T4 (Techguns 2 has no crafting recipes for them): power armor Mk2 is the T3 power armor with Mk2 plating;
        // the nether combat armor and the nanosuit are the HEV suit with nether armor plating / nano fibers for both plates
        Item powerPlatingMk2 = TGItems.POWER_PLATING_MK2.get();
        Map<Character, Object> powerMk2Keys = Map.of('p', powerPlatingMk2, 'c', elite, 'e', engine, 'g', Tags.Items.GLASS_PANES);
        usedKeys(output, TGArmors.T4_POWER_HELMET.get(), powerPlatingMk2, powerMk2Keys, "cpc", "pgp");
        usedKeys(output, TGArmors.T4_POWER_CHESTPLATE.get(), powerPlatingMk2, powerMk2Keys, "p p", "ece", "ppp");
        usedKeys(output, TGArmors.T4_POWER_LEGGINGS.get(), powerPlatingMk2, powerMk2Keys, "pcp", "e e", "p p");
        usedKeys(output, TGArmors.T4_POWER_BOOTS.get(), powerPlatingMk2, powerMk2Keys, "e e", "p p");
        hevLike(output, TGItems.NETHER_ARMOR_PLATING.get(), elite, TGArmors.T4_PRAETOR_HELMET.get(), TGArmors.T4_PRAETOR_CHESTPLATE.get(),
                TGArmors.T4_PRAETOR_LEGGINGS.get(), TGArmors.T4_PRAETOR_BOOTS.get());
        hevLike(output, TGItems.NANO_FIBERS.get(), elite, TGArmors.T4_NANO_HELMET.get(), TGArmors.T4_NANO_CHESTPLATE.get(),
                TGArmors.T4_NANO_LEGGINGS.get(), TGArmors.T4_NANO_BOOTS.get());
    }

    /** Techguns 2 recipes of the extra slot gear (face, back and hand items). */
    private void buildSlotItemRecipes(RecipeOutput output) {
        Item cloth = TGItems.HEAVY_CLOTH.get();
        Item rubber = TGItems.RUBBER_BAR.get();
        Item plastic = TGItems.PLASTIC_SHEET.get();
        Item basicCircuit = TGItems.CIRCUIT_BOARD.get();
        Item filter = TGSlotItems.GAS_MASK_FILTER.get();
        Item backpack = TGSlotItems.GLIDER_BACKPACK.get();
        Item wing = TGSlotItems.GLIDER_WING.get();
        TagKey<Item> ironPlate = TGTags.Items.plate("iron");

        // glider
        slotShaped(output, backpack, cloth, "glider_backpack", Map.of('h', cloth, 's', Tags.Items.INGOTS_IRON), "hhh", "shs", "hhh");
        slotShaped(output, wing, cloth, "glider_wing", Map.of('h', cloth, 's', Tags.Items.INGOTS_IRON), "sss", "hhh", "hhh");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGSlotItems.GLIDER.get())
                .requires(backpack).requires(wing, 2).unlockedBy(getHasName(wing), has(wing)).save(output, TGIds.id("glider"));

        // gas mask; a filter refills a used mask
        slotShaped(output, filter, Items.COAL, "gas_mask_filter", Map.of('i', Tags.Items.NUGGETS_IRON, 'c', Items.COAL), "iii", "ici", "iii");
        slotShaped(output, TGSlotItems.GAS_MASK.get(), filter, "gas_mask", Map.of('r', rubber, 'f', filter, 'g', Tags.Items.GLASS_PANES),
                "grg", "rrr", "rfr");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGSlotItems.GAS_MASK.get())
                .requires(TGSlotItems.GAS_MASK.get()).requires(filter).unlockedBy(getHasName(filter), has(filter))
                .save(output, TGIds.id("gas_mask_refilter"));

        // night vision goggles; with an empty battery they come out empty
        Map<Character, Object> goggleKeys = Map.of('r', rubber, 'h', cloth, 'c', basicCircuit, 'e', TGAmmo.REDSTONE_BATTERY,
                'g', Tags.Items.GLASS_BLOCKS, 's', Tags.Items.INGOTS_IRON);
        slotShaped(output, TGSlotItems.NIGHT_VISION_GOGGLES.get(), basicCircuit, "night_vision_goggles", goggleKeys, "rhr", "cec", "gsg");
        ItemStack emptyGoggles = new ItemStack(TGSlotItems.NIGHT_VISION_GOGGLES.get());
        emptyGoggles.setDamageValue(emptyGoggles.getMaxDamage());
        StackShapedRecipeBuilder emptyBuilder = StackShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, emptyGoggles, "rhr", "cec", "gsg")
                .define('r', rubber).define('h', cloth).define('c', basicCircuit).define('e', TGAmmo.REDSTONE_BATTERY_EMPTY)
                .define('g', Tags.Items.GLASS_BLOCKS).define('s', Tags.Items.INGOTS_IRON);
        emptyBuilder.unlockedBy(getHasName(basicCircuit), has(basicCircuit)).save(output, TGIds.id("night_vision_goggles_empty"));

        // jetpack from a glider or a jump pack
        Map<Character, Object> jetpackKeys = Map.of('f', TGAmmo.FUEL_TANK, 'p', TGTags.Items.plate("obsidian_steel"),
                'r', TGAmmo.ROCKET, 'm', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL);
        slotShaped(output, TGSlotItems.JETPACK.get(), TGAmmo.FUEL_TANK.get(), "jetpack_from_glider",
                with(jetpackKeys, 'g', TGSlotItems.GLIDER), "f f", "pgp", "rmr");
        slotShaped(output, TGSlotItems.JETPACK.get(), TGAmmo.FUEL_TANK.get(), "jetpack_from_jump_pack",
                with(jetpackKeys, 'g', TGSlotItems.JUMP_PACK), "f f", "pgp", "rmr");

        slotShaped(output, TGSlotItems.ANTIGRAV_PACK.get(), TGItems.ANTIGRAV_CORE.get(), "antigrav_pack",
                Map.of('t', TGTags.Items.plate("titanium"), 'e', TGAmmo.NUCLEAR_POWER_CELL, 'b', backpack,
                        'c', TGItems.CIRCUIT_BOARD_ELITE, 'm', TGItems.MECHANICAL_PARTS_CARBON, 'a', TGItems.ANTIGRAV_CORE),
                "tet", "cac", "mbm");
        slotShaped(output, TGSlotItems.SCUBA_TANKS.get(), TGAmmo.COMPRESSED_AIR_TANK.get(), "scuba_tanks",
                Map.of('p', plastic, 'a', TGAmmo.COMPRESSED_AIR_TANK, 'i', ironPlate), "p p", "aia", "p p");
        slotShaped(output, TGSlotItems.JUMP_PACK.get(), backpack, "jump_pack",
                Map.of('p', ironPlate, 'c', TGAmmo.COMPRESSED_AIR_TANK, 'n', Tags.Items.NUGGETS_IRON, 'b', backpack), "n n", "pbp", "c c");
        slotShaped(output, TGSlotItems.OXYGEN_MASK.get(), plastic, "oxygen_mask", Map.of('p', plastic, 'r', rubber), " p ", "rpr");

        // tactical mask, both arrangements
        Map<Character, Object> tacticalKeys = Map.of('p', plastic, 'n', TGSlotItems.NIGHT_VISION_GOGGLES, 'c', basicCircuit,
                'g', TGSlotItems.GAS_MASK, 'o', TGSlotItems.OXYGEN_MASK, 'e', TGAmmo.ENERGY_CELL);
        slotShaped(output, TGSlotItems.TACTICAL_MASK.get(), TGSlotItems.OXYGEN_MASK.get(), "tactical_mask", tacticalKeys, "ngo", "cpc", "pep");
        slotShaped(output, TGSlotItems.TACTICAL_MASK.get(), TGSlotItems.OXYGEN_MASK.get(), "tactical_mask_mirrored", tacticalKeys,
                "ogn", "cpc", "pep");

        slotShaped(output, TGSlotItems.WORKING_GLOVES.get(), cloth, "working_gloves", Map.of('h', cloth), " h", "hh");
        // new in Techguns 3: replaces the auto-feeder slots of the Techguns 2 inventory tab (a plastic food box, a rubber tube)
        slotShaped(output, TGSlotItems.AUTO_FEEDER.get(), plastic, "auto_feeder", Map.of('p', plastic, 'r', rubber), " pp", " pp", "rr ");
    }

    private static Map<Character, Object> with(Map<Character, Object> keys, char symbol, Object ingredient) {
        Map<Character, Object> copy = new java.util.HashMap<>(keys);
        copy.put(symbol, ingredient);
        return copy;
    }

    /** Shaped recipe with an explicit id; keys are items, item holders or item tags. */
    private static void slotShaped(RecipeOutput output, Item result, ItemLike unlock, String id, Map<Character, ?> keys, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result);
        for (String row : pattern) {
            builder.pattern(row);
        }
        keys.forEach((symbol, ingredient) -> {
            if (ingredient instanceof TagKey<?> tag) {
                @SuppressWarnings("unchecked")
                TagKey<Item> itemTag = (TagKey<Item>) tag;
                builder.define(symbol, itemTag);
            } else {
                builder.define(symbol, (ItemLike) ingredient);
            }
        });
        builder.unlockedBy(getHasName(unlock), has(unlock)).save(output, TGIds.id(id));
    }

    /** Shaped armor recipe that only defines the keys the pattern uses. */
    private static void usedKeys(RecipeOutput output, Item armor, ItemLike unlock, Map<Character, Object> keys, String... pattern) {
        String joined = String.join("", pattern);
        armor(output, armor, unlock, b -> keys.forEach((symbol, ingredient) -> {
            if (joined.indexOf(symbol) >= 0) {
                if (ingredient instanceof TagKey<?> tag) {
                    @SuppressWarnings("unchecked")
                    TagKey<Item> itemTag = (TagKey<Item>) tag;
                    b.define(symbol, itemTag);
                } else {
                    b.define(symbol, (ItemLike) ingredient);
                }
            }
        }), pattern);
    }

    private static void exoSuit(RecipeOutput output, Item exo, Item combat, int titaniumIngots) {
        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, exo)
                .requires(combat).requires(TGItems.MECHANICAL_PARTS_CARBON.get()).requires(TGItems.CIRCUIT_BOARD_ELITE.get())
                .requires(TGItems.ELECTRIC_ENGINE.get());
        for (int i = 0; i < titaniumIngots; i++) {
            builder.requires(TGTags.Items.ingot("titanium"));
        }
        builder.unlockedBy(getHasName(combat), has(combat)).save(output, TGIds.id(getItemName(exo)));
    }

    /** The T3 HEV suit patterns with {@code plating} in place of both its carbon and titanium plates. */
    private static void hevLike(RecipeOutput output, Item plating, Item elite, Item helmet, Item chestplate, Item leggings, Item boots) {
        Map<Character, Object> keys = Map.of('p', plating, 'c', elite, 'g', Tags.Items.GLASS_PANES);
        usedKeys(output, helmet, plating, keys, "pcp", "pgp");
        usedKeys(output, chestplate, plating, keys, "p p", "pcp", "ppp");
        usedKeys(output, leggings, plating, keys, "ppp", "c c", "p p");
        usedKeys(output, boots, plating, keys, "p p", "p p");
    }

    /** Techguns 2 machines, the machine upgrade and the machine recipes. */
    private void buildMachineRecipes(RecipeOutput output) {
        // Metal Press: Techguns 2 has a block-of-iron and an iron-plate version
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.METAL_PRESS.get())
                .pattern("ibi").pattern("iei").pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON).define('b', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('e', TGItems.ELECTRIC_ENGINE.get()).define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(TGItems.ELECTRIC_ENGINE.get()), has(TGItems.ELECTRIC_ENGINE.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.METAL_PRESS.get())
                .pattern("ibi").pattern("iei").pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON).define('b', TGTags.Items.plate("iron"))
                .define('e', TGItems.ELECTRIC_ENGINE.get()).define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(TGItems.ELECTRIC_ENGINE.get()), has(TGItems.ELECTRIC_ENGINE.get()))
                .save(output, TGIds.id("metal_press_alt"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.MACHINE_UPGRADE_STACK.get())
                .pattern("pgp").pattern("ncn").pattern("pnp")
                .define('p', TGTags.Items.plate("iron")).define('n', Tags.Items.INGOTS_GOLD).define('g', Tags.Items.DYES_GREEN)
                .define('c', Items.CHEST)
                .unlockedBy(getHasName(TGBlocks.METAL_PRESS.get()), has(TGBlocks.METAL_PRESS.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.AMMO_PRESS.get())
                .pattern("ili").pattern("cec").pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON).define('l', TGTags.Items.ingot("lead")).define('c', Tags.Items.INGOTS_COPPER)
                .define('e', TGItems.ELECTRIC_ENGINE.get()).define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(TGItems.ELECTRIC_ENGINE.get()), has(TGItems.ELECTRIC_ENGINE.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.BLAST_FURNACE.get())
                .pattern("iri").pattern("ifi").pattern("sbs")
                .define('i', TGTags.Items.plate("iron")).define('r', Tags.Items.DUSTS_REDSTONE).define('f', Items.FURNACE)
                .define('s', Items.STONE_BRICKS).define('b', Tags.Items.STORAGE_BLOCKS_IRON)
                .unlockedBy(getHasName(Items.FURNACE), has(Items.FURNACE)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.GRINDER.get())
                .pattern("imi").pattern("mem").pattern("iri")
                .define('i', TGTags.Items.plate("iron")).define('m', TGItems.MECHANICAL_PARTS_IRON.get())
                .define('e', TGItems.ELECTRIC_ENGINE.get()).define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(TGItems.ELECTRIC_ENGINE.get()), has(TGItems.ELECTRIC_ENGINE.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.CHEM_LAB.get())
                .pattern("igi").pattern("geg").pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON).define('g', Items.GLASS_BOTTLE).define('e', TGItems.ELECTRIC_ENGINE.get())
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(TGItems.ELECTRIC_ENGINE.get()), has(TGItems.ELECTRIC_ENGINE.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.CHARGING_STATION.get())
                .pattern("sgs").pattern("cbc").pattern("sgs")
                .define('s', TGTags.Items.plate("steel")).define('g', TGItems.GOLD_WIRE.get()).define('c', TGItems.COIL.get())
                .define('b', TGItems.CIRCUIT_BOARD.get())
                .unlockedBy(getHasName(TGItems.COIL.get()), has(TGItems.COIL.get())).save(output);

        benchRecipes(output);
        chemLabRecipes(output);
        chargingRecipes(output);
        ammoPressRecipes(output);
        blastFurnaceRecipes(output);
        grinderRecipes(output);

        // Techguns 2 TGMachineRecipes, in order; all allow swapped inputs
        metalPress(output, "tin_plate", ingot("tin"), ingot("tin"), TGItems.TIN_PLATE.get(), 2);
        metalPress(output, "copper_plate", Ingredient.of(Tags.Items.INGOTS_COPPER), Ingredient.of(Tags.Items.INGOTS_COPPER), TGItems.COPPER_PLATE.get(), 2);
        metalPress(output, "bronze_plate", ingot("bronze"), ingot("bronze"), TGItems.BRONZE_PLATE.get(), 2);
        metalPress(output, "iron_plate", Ingredient.of(Tags.Items.INGOTS_IRON), Ingredient.of(Tags.Items.INGOTS_IRON), TGItems.IRON_PLATE.get(), 2);
        metalPress(output, "steel_plate", ingot("steel"), ingot("steel"), TGItems.STEEL_PLATE.get(), 2);
        metalPress(output, "lead_plate", ingot("lead"), ingot("lead"), TGItems.LEAD_PLATE.get(), 2);
        metalPress(output, "mechanical_parts_iron", plate("iron"), Ingredient.of(Items.FLINT), TGItems.MECHANICAL_PARTS_IRON.get(), 1);
        metalPress(output, "mechanical_parts_obsidian_steel", plate("obsidian_steel"), Ingredient.of(Tags.Items.GEMS_QUARTZ),
                TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get(), 1);
        metalPress(output, "mechanical_parts_carbon", plate("carbon"), Ingredient.of(Items.BLAZE_ROD), TGItems.MECHANICAL_PARTS_CARBON.get(), 2);
        metalPress(output, "mechanical_parts_netherite", plate("netherite"), Ingredient.of(TGItems.GHAST_POWDER.get()),
                TGItems.MECHANICAL_PARTS_NETHERITE.get(), 2);
        metalPress(output, "copper_wire", plate("copper"), plate("copper"), TGItems.COPPER_WIRE.get(), 8);
        metalPress(output, "carbon_plate", Ingredient.of(TGItems.CARBON_FIBERS.get()), Ingredient.of(TGItems.CARBON_FIBERS.get()), TGItems.CARBON_PLATE.get(), 2);
        metalPress(output, "advanced_rounds", plate("obsidian_steel"), Ingredient.of(TGItems.TGX.get()), TGAmmo.ADVANCED_ROUNDS.get(), 16);
        metalPress(output, "obsidian_steel_plate", ingot("obsidian_steel"), ingot("obsidian_steel"), TGItems.OBSIDIAN_STEEL_PLATE.get(), 2);
        metalPress(output, "steam_armor_plate", plate("steel"), plate("bronze"), TGItems.STEAM_ARMOR_PLATE.get(), 1);
        metalPress(output, "gold_wire", Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(Tags.Items.INGOTS_GOLD), TGItems.GOLD_WIRE.get(), 2);
        metalPress(output, "grenade_40mm", plate("iron"), Ingredient.of(Items.TNT), TGAmmo.GRENADE_40MM.get(), 16);
        metalPress(output, "titanium_plate", ingot("titanium"), ingot("titanium"), TGItems.TITANIUM_PLATE.get(), 2);
        metalPress(output, "netherite_plate", Ingredient.of(Tags.Items.INGOTS_NETHERITE), Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                TGItems.NETHERITE_PLATE.get(), 2);
        metalPress(output, "gauss_rifle_slugs", plate("obsidian_steel"), plate("titanium"), TGAmmo.GAUSS_RIFLE_SLUGS.get(), 4);
        metalPress(output, "sniper_rounds_explosive", Ingredient.of(TGAmmo.SNIPER_ROUNDS_INCENDIARY.get()), Ingredient.of(TGItems.TGX.get()),
                TGAmmo.SNIPER_ROUNDS_EXPLOSIVE.get(), 1);
    }

    /**
     * Techguns 2 Chem Lab recipes without fluid mods (lava where Techguns 2 preferred fuel or oil). Uranium ore as in
     * Techguns 2, and raw uranium, which ores drop since 1.17.
     */
    private static void chemLabRecipes(RecipeOutput output) {
        var water = net.minecraft.tags.FluidTags.WATER;
        var lava = net.minecraft.tags.FluidTags.LAVA;
        var acid = techguns.registry.TGFluids.ACID.get();
        chemLab(output, "gunpowder", sized(Items.REDSTONE, 1), sized(Items.COAL, 1), null, fluid(water, 250),
                new ItemStack(Items.GUNPOWDER), true, 5);
        chemLab(output, "raw_plastic", sized(TGItems.RAW_RUBBER.get(), 1), sized(Items.COAL, 1), null, fluid(water, 1000),
                new ItemStack(TGItems.RAW_PLASTIC.get()), true, 25);
        chemLabFluid(output, "creeper_acid", sized(TGItems.BIOMASS.get(), 1), sized(Items.GUNPOWDER, 1), fluid(water, 1000),
                new net.neoforged.neoforge.fluids.FluidStack(acid, 1000), 20);
        // Techguns 2 rad medicine
        chemLab(output, "rad_pills", sized(Items.SUGAR, 4), sized(Items.GLISTERING_MELON_SLICE, 1), sized(Items.GLASS_BOTTLE, 1),
                net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient.of(Tags.Fluids.MILK, 1000), new ItemStack(TGItems.RAD_PILLS.get(), 4), true, 20);
        chemLab(output, "rad_away", sized(Items.NETHER_WART, 1), sized(Items.GLISTERING_MELON_SLICE, 1), sized(TGItems.INFUSION_BAG.get(), 1),
                net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient.of(acid, 250), new ItemStack(TGItems.RAD_AWAY.get()), true, 25);
        chemLab(output, "tgx", sized(Items.GUNPOWDER, 1), new SizedIngredient(Ingredient.of(Tags.Items.GEMS_LAPIS), 1), null, fluid(lava, 500),
                new ItemStack(TGItems.TGX.get()), true, 20);
        // incendiary rounds (Techguns 2: only the Chem Lab makes them; rifle rounds also split from incendiary stacks)
        chemLab(output, "rifle_rounds_incendiary", sized(TGAmmo.RIFLE_ROUNDS.get(), 1), sized(Items.BLAZE_POWDER, 1), null, fluid(lava, 250),
                new ItemStack(TGAmmo.RIFLE_ROUNDS_INCENDIARY.get()), false, 25);
        chemLab(output, "pistol_rounds_incendiary", sized(TGAmmo.PISTOL_ROUNDS.get(), 2), sized(Items.BLAZE_POWDER, 1), null, fluid(lava, 250),
                new ItemStack(TGAmmo.PISTOL_ROUNDS_INCENDIARY.get(), 2), false, 25);
        chemLab(output, "sniper_rounds_incendiary", sized(TGAmmo.SNIPER_ROUNDS.get(), 1), sized(Items.BLAZE_POWDER, 1), null, fluid(lava, 250),
                new ItemStack(TGAmmo.SNIPER_ROUNDS_INCENDIARY.get()), false, 25);
        chemLab(output, "shotgun_rounds_incendiary", sized(TGAmmo.SHOTGUN_ROUNDS.get(), 8), sized(Items.BLAZE_POWDER, 1), null, fluid(lava, 250),
                new ItemStack(TGAmmo.SHOTGUN_ROUNDS_INCENDIARY.get(), 8), false, 25);
        chemLab(output, "fuel_tank", null, null, sized(TGAmmo.FUEL_TANK_EMPTY.get(), 1), fluid(lava, 500),
                new ItemStack(TGAmmo.FUEL_TANK.get()), false, 1);
        // Techguns 2: 250 mB of any fuel of another mod (lava stays, Techguns 2 dropped it once a fuel existed)
        chemLab(output, "fuel_tank_from_fuel", null, null, sized(TGAmmo.FUEL_TANK_EMPTY.get(), 1), fluid(TGTags.Fluids.FUEL, 250),
                new ItemStack(TGAmmo.FUEL_TANK.get()), false, 1);
        chemLab(output, "rocket_high_velocity", sized(TGAmmo.ROCKET.get(), 1), null, null, fluid(lava, 250),
                new ItemStack(TGAmmo.ROCKET_HIGH_VELOCITY.get()), false, 5);
        chemLab(output, "carbon_fibers", new SizedIngredient(Ingredient.of(Tags.Items.GEMS_DIAMOND), 1), sized(Items.BLAZE_POWDER, 1), null,
                fluid(lava, 1000), new ItemStack(TGItems.CARBON_FIBERS.get(), 2), true, 25);
        chemLab(output, "raw_rubber", new SizedIngredient(Ingredient.of(ItemTags.LOGS), 1), null, null, fluid(water, 1000),
                new ItemStack(TGItems.RAW_RUBBER.get()), false, 20);
        chemLab(output, "bio_tank", sized(TGItems.BIOMASS.get(), 1), null, sized(TGAmmo.BIO_TANK_EMPTY.get(), 1), fluid(water, 500),
                new ItemStack(TGAmmo.BIO_TANK.get()), false, 1);
        chemLab(output, "compressed_air_tank", sized(Items.COAL, 1), null, sized(TGAmmo.COMPRESSED_AIR_TANK_EMPTY.get(), 1), fluid(water, 250),
                new ItemStack(TGAmmo.COMPRESSED_AIR_TANK.get()), true, 5);
        chemLab(output, "nether_charge", sized(Items.NETHERRACK, 1), sized(Items.SOUL_SAND, 1), null, fluid(lava, 1000),
                new ItemStack(TGAmmo.NETHER_CHARGE.get(), 4), true, 20);
        chemLab(output, "yellowcake", new SizedIngredient(Ingredient.fromValues(java.util.stream.Stream.of(
                        new Ingredient.TagValue(TGTags.Items.ore("uranium")), new Ingredient.TagValue(TGTags.Items.rawMaterial("uranium")))), 1),
                null, null, net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient.of(acid, 250), new ItemStack(TGItems.YELLOWCAKE.get(), 3), false, 20);
        chemLab(output, "nuclear_power_cell", sized(TGItems.ENRICHED_URANIUM.get(), 1), null, sized(TGAmmo.NUCLEAR_POWER_CELL_EMPTY.get(), 1),
                fluid(water, 1000), new ItemStack(TGAmmo.NUCLEAR_POWER_CELL.get()), true, 40);
        chemLab(output, "slime_ball", sized(Items.GREEN_DYE, 1), sized(TGItems.RAW_RUBBER.get(), 1), null,
                net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient.of(Tags.Fluids.MILK, 500), new ItemStack(Items.SLIME_BALL), true, 25);
        chemLab(output, "treated_leather", sized(Items.LEATHER, 2), new SizedIngredient(Ingredient.of(Tags.Items.SLIME_BALLS), 1), null,
                net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient.of(acid, 500), new ItemStack(TGItems.TREATED_LEATHER.get(), 2), true, 20);
        chemLab(output, "leather", sized(Items.ROTTEN_FLESH, 1), null, null, fluid(water, 500), new ItemStack(Items.LEATHER), false, 15);
        chemLab(output, "light_gray_concrete", sized(Items.GRAVEL, 1), sized(Items.SAND, 1), null, fluid(water, 250),
                new ItemStack(Items.LIGHT_GRAY_CONCRETE, 2), false, 5);
        chemLab(output, "clay", sized(Items.GRAVEL, 1), sized(Items.DIRT, 1), null, fluid(water, 250), new ItemStack(Items.CLAY, 2), false, 5);
    }

    private static SizedIngredient sized(ItemLike item, int count) {
        return new SizedIngredient(Ingredient.of(item), count);
    }

    private static net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient fluid(TagKey<net.minecraft.world.level.material.Fluid> tag, int amount) {
        return net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient.of(tag, amount);
    }

    private static void chemLab(RecipeOutput output, String name, SizedIngredient first, SizedIngredient second, SizedIngredient flask,
                                net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient fluid, ItemStack result, boolean swap, int power) {
        output.accept(TGIds.id("chem_lab/" + name), new ChemLabRecipe(java.util.Optional.ofNullable(first), java.util.Optional.ofNullable(second),
                java.util.Optional.ofNullable(flask), java.util.Optional.ofNullable(fluid), result, net.neoforged.neoforge.fluids.FluidStack.EMPTY,
                swap, power), null);
    }

    private static void chemLabFluid(RecipeOutput output, String name, SizedIngredient first, SizedIngredient second,
                                     net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient fluid,
                                     net.neoforged.neoforge.fluids.FluidStack result, int power) {
        output.accept(TGIds.id("chem_lab/" + name), new ChemLabRecipe(java.util.Optional.ofNullable(first), java.util.Optional.ofNullable(second),
                java.util.Optional.empty(), java.util.Optional.ofNullable(fluid), ItemStack.EMPTY, result, true, power), null);
    }

    /** Techguns 2 {@code ChargingStationRecipe}: energy cell 50000 FE, redstone battery 20000 FE. */
    private static void chargingRecipes(RecipeOutput output) {
        output.accept(TGIds.id("charging/energy_cell"), new ChargingRecipe(Ingredient.of(TGAmmo.ENERGY_CELL_EMPTY.get()),
                new ItemStack(TGAmmo.ENERGY_CELL.get()), 50000), null);
        output.accept(TGIds.id("charging/redstone_battery"), new ChargingRecipe(Ingredient.of(TGAmmo.REDSTONE_BATTERY_EMPTY.get()),
                new ItemStack(TGAmmo.REDSTONE_BATTERY.get()), 20000), null);
    }

    /** Techguns 2 {@code AmmoPressBuildPlans}: bullet metal steel/lead, casing metal copper/tin/iron/bronze, gunpowder. */
    /** Techguns 2 Repair, Camo and Upgrade Bench, the Fabric Ammo Bench, the armor upgrades and the bench recipes. */
    private void benchRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.REPAIR_BENCH.get())
                .pattern("pmp").pattern("ici").pattern("iii")
                .define('p', TGTags.Items.plate("iron")).define('m', TGItems.MECHANICAL_PARTS_IRON.get()).define('c', Items.CRAFTING_TABLE)
                .define('i', Tags.Items.NUGGETS_IRON)
                .unlockedBy(getHasName(TGItems.MECHANICAL_PARTS_IRON.get()), has(TGItems.MECHANICAL_PARTS_IRON.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.CAMO_BENCH.get())
                .pattern("ddd").pattern("ici").pattern("iii")
                .define('d', Tags.Items.DYES).define('c', Items.CRAFTING_TABLE).define('i', Tags.Items.NUGGETS_IRON)
                .unlockedBy(getHasName(Items.CRAFTING_TABLE), has(Items.CRAFTING_TABLE)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.UPGRADE_BENCH.get())
                .pattern(" c ").pattern("oeo").pattern("sos")
                .define('c', Items.CRAFTING_TABLE).define('o', TGTags.Items.plate("obsidian_steel")).define('e', Tags.Items.GEMS_EMERALD)
                .define('s', Tags.Items.OBSIDIANS)
                .unlockedBy(getHasName(TGItems.OBSIDIAN_STEEL_PLATE.get()), has(TGItems.OBSIDIAN_STEEL_PLATE.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TGItems.CROWBAR.get())
                .pattern(" np").pattern(" p ").pattern("p  ")
                .define('p', TGTags.Items.plate("steel")).define('n', TGTags.Items.nugget("steel"))
                .unlockedBy(getHasName(TGItems.STEEL_PLATE.get()), has(TGItems.STEEL_PLATE.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.COMBAT_KNIFE.get())
                .pattern(" s").pattern("p ")
                .define('s', TGTags.Items.plate("steel")).define('p', TGItems.PLASTIC_SHEET.get())
                .unlockedBy(getHasName(TGItems.STEEL_PLATE.get()), has(TGItems.STEEL_PLATE.get())).save(output);
        // two warheads; one makes a rocket a nuke rocket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.TACTICAL_NUKE_WARHEAD.get(), 2)
                .pattern("pcp").pattern("tut").pattern("pcp")
                .define('p', TGTags.Items.plate("lead")).define('t', TGItems.TGX.get()).define('c', TGItems.CIRCUIT_BOARD.get())
                .define('u', TGItems.ENRICHED_URANIUM.get())
                .unlockedBy(getHasName(TGItems.ENRICHED_URANIUM.get()), has(TGItems.ENRICHED_URANIUM.get())).save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.ROCKET_NUKE.get())
                .requires(TGAmmo.ROCKET.get()).requires(TGItems.TACTICAL_NUKE_WARHEAD.get())
                .unlockedBy(getHasName(TGItems.TACTICAL_NUKE_WARHEAD.get()), has(TGItems.TACTICAL_NUKE_WARHEAD.get())).save(output);
        // Fabric: shapeless crafting table + gunpowder + iron ingot
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGBlocks.AMMO_BENCH.get())
                .requires(Items.CRAFTING_TABLE).requires(Tags.Items.GUNPOWDERS).requires(Tags.Items.INGOTS_IRON)
                .unlockedBy(getHasName(Items.GUNPOWDER), has(Items.GUNPOWDER)).save(output);

        // armor upgrades: plates around a gold ingot (diamond for tier 3) with iron plates, steam armor plates or wool
        Object[][] tiers = {{TGTags.Items.plate("steel"), Tags.Items.INGOTS_GOLD}, {TGTags.Items.plate("obsidian_steel"), Tags.Items.INGOTS_GOLD},
                {TGTags.Items.plate("carbon"), Tags.Items.GEMS_DIAMOND}};
        ItemLike[][] upgrades = {
                {TGItems.UPGRADE_PROTECTION_1.get(), TGItems.UPGRADE_PROJECTILE_PROTECTION_1.get(), TGItems.UPGRADE_BLAST_PROTECTION_1.get()},
                {TGItems.UPGRADE_PROTECTION_2.get(), TGItems.UPGRADE_PROJECTILE_PROTECTION_2.get(), TGItems.UPGRADE_BLAST_PROTECTION_2.get()},
                {TGItems.UPGRADE_PROTECTION_3.get(), TGItems.UPGRADE_PROJECTILE_PROTECTION_3.get(), TGItems.UPGRADE_BLAST_PROTECTION_3.get()}};
        List<ResourceKey<Enchantment>> enchantments = List.of(Enchantments.PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.BLAST_PROTECTION);
        for (int tier = 0; tier < 3; tier++) {
            @SuppressWarnings("unchecked")
            TagKey<Item> plate = (TagKey<Item>) tiers[tier][0];
            @SuppressWarnings("unchecked")
            TagKey<Item> center = (TagKey<Item>) tiers[tier][1];
            for (int kind = 0; kind < 3; kind++) {
                ItemLike upgrade = upgrades[tier][kind];
                ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, upgrade)
                        .pattern("pip").pattern("igi").pattern("pip").define('p', plate).define('g', center);
                switch (kind) {
                    case 0 -> builder.define('i', TGTags.Items.plate("iron"));
                    case 1 -> builder.define('i', TGItems.STEAM_ARMOR_PLATE.get());
                    default -> builder.define('i', ItemTags.WOOL);
                }
                builder.unlockedBy(getHasName(TGBlocks.UPGRADE_BENCH.get()), has(TGBlocks.UPGRADE_BENCH.get())).save(output);
                output.accept(TGIds.id("upgrade_bench/" + getItemName(upgrade)),
                        new UpgradeBenchRecipe(Ingredient.of(upgrade), enchantments.get(kind), tier + 1), null);
            }
        }

        camoChangeRecipes(output);
        ammoBenchRecipes(output);
        fabricatorRecipes(output);
        reactionChamberRecipes(output);
    }

    /** Techguns 2 Reaction Chamber blocks, heat ray focus and {@code TGMachineRecipes} reactions. */
    private void reactionChamberRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.REACTION_CHAMBER_HOUSING.get(), 9)
                .pattern("sms").pattern("pcp").pattern("ses")
                .define('s', TGTags.Items.plate("steel")).define('m', TGItems.MECHANICAL_PARTS_CARBON.get()).define('p', TGItems.CYBERNETIC_PARTS.get())
                .define('c', TGBlocks.CHEM_LAB.get()).define('e', TGItems.CIRCUIT_BOARD_ELITE.get())
                .unlockedBy(getHasName(TGBlocks.CHEM_LAB.get()), has(TGBlocks.CHEM_LAB.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.REACTION_CHAMBER_GLASS.get(), 6)
                .pattern("rgr").pattern("rgr").pattern("rgr")
                .define('r', TGBlocks.REACTION_CHAMBER_HOUSING.get()).define('g', Tags.Items.GLASS_BLOCKS)
                .unlockedBy(getHasName(TGBlocks.REACTION_CHAMBER_HOUSING.get()), has(TGBlocks.REACTION_CHAMBER_HOUSING.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.REACTION_CHAMBER_CONTROLLER.get())
                .pattern(" g ").pattern("crc").pattern(" g ")
                .define('r', TGBlocks.REACTION_CHAMBER_HOUSING.get()).define('c', TGItems.CIRCUIT_BOARD_ELITE.get()).define('g', Tags.Items.GLASS_BLOCKS)
                .unlockedBy(getHasName(TGBlocks.REACTION_CHAMBER_HOUSING.get()), has(TGBlocks.REACTION_CHAMBER_HOUSING.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.INFUSION_BAG.get(), 4)
                .pattern("pp").pattern("pp").define('p', TGItems.PLASTIC_SHEET.get())
                .unlockedBy(getHasName(TGItems.PLASTIC_SHEET.get()), has(TGItems.PLASTIC_SHEET.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.HEAT_RAY_FOCUS.get())
                .pattern("cwc").pattern("plp").pattern("plp")
                .define('c', TGItems.CIRCUIT_BOARD_ELITE.get()).define('w', TGItems.GOLD_WIRE.get()).define('p', TGTags.Items.plate("steel"))
                .define('l', Items.REDSTONE_LAMP)
                .unlockedBy(getHasName(TGItems.CIRCUIT_BOARD_ELITE.get()), has(TGItems.CIRCUIT_BOARD_ELITE.get())).save(output);

        Ingredient heatRay = Ingredient.of(TGItems.HEAT_RAY_FOCUS.get());
        FluidIngredient water = FluidIngredient.tag(FluidTags.WATER);
        FluidIngredient lava = FluidIngredient.tag(FluidTags.LAVA);
        reaction(output, "uv_wheat", Ingredient.of(Items.WHEAT_SEEDS), Ingredient.of(TGItems.UV_EMITTER.get()), water,
                List.of(new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT_SEEDS, 2)), 10, 5, 3, 1, 1, 1000, 0.5f, ReactionChamberRecipe.Risk.EXPLOSION_LOW, 50000);
        reaction(output, "laser_focus", Ingredient.of(Tags.Items.GEMS_DIAMOND), heatRay, FluidIngredient.tag(TGTags.Fluids.LIQUID_REDSTONE),
                List.of(new ItemStack(TGItems.LASER_FOCUS.get())), 10, 5, 3, 1, 4, 4000, 0.5f, ReactionChamberRecipe.Risk.BREAK_ITEM, 100000);
        reaction(output, "titanium", Ingredient.of(TGTags.Items.ore("titanium")), heatRay, FluidIngredient.single(TGFluids.ACID.get()),
                List.of(new ItemStack(TGItems.RAW_TITANIUM.get(), 2), new ItemStack(Items.IRON_ORE)), 2, 1, 5, 0, 3, 100, 0.0f,
                ReactionChamberRecipe.Risk.BREAK_ITEM, 25000);
        reaction(output, "blaze_rod", Ingredient.of(TGItems.QUARTZ_ROD.get()), heatRay, lava, List.of(new ItemStack(Items.BLAZE_ROD)), 5, 3, 7, 2, 4,
                1000, 0.5f, ReactionChamberRecipe.Risk.BREAK_ITEM, 250000);
        reaction(output, "glowstone", Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE), heatRay, lava, List.of(new ItemStack(Items.GLOWSTONE)), 5, 3, 7,
                2, 4, 1000, 0.5f, ReactionChamberRecipe.Risk.EXPLOSION_MEDIUM, 250000);
        reaction(output, "antigrav_core", Ingredient.of(Items.NETHER_STAR), heatRay, FluidIngredient.tag(TGTags.Fluids.LIQUID_ENDER),
                List.of(new ItemStack(TGItems.ANTIGRAV_CORE.get())), 10, 7, 8, 2, 4, 4000, 1.0f, ReactionChamberRecipe.Risk.EXPLOSION_LOW, 500000);
        reaction(output, "enriched_uranium", Ingredient.of(TGItems.YELLOWCAKE.get()), heatRay, water,
                List.of(new ItemStack(TGItems.ENRICHED_URANIUM.get())), 5, 4, 7, 0, 3, 1000, 0.0f, ReactionChamberRecipe.Risk.BREAK_ITEM, 250000);
        // Techguns 3
        reaction(output, "ghast_powder", Ingredient.of(Items.GHAST_TEAR), heatRay, FluidIngredient.single(TGFluids.ACID.get()),
                List.of(new ItemStack(TGItems.GHAST_POWDER.get(), 2)), 5, 3, 4, 1, 4, 1000, 0.25f,
                ReactionChamberRecipe.Risk.BREAK_ITEM, 100000);
    }

    private static void reaction(RecipeOutput output, String name, Ingredient input, Ingredient focus, FluidIngredient fluid, List<ItemStack> outputs,
                                 int ticks, int requiredCompletion, int intensity, int intensityMargin, int liquidLevel, int liquidConsumption,
                                 float instability, ReactionChamberRecipe.Risk risk, int powerPerTick) {
        output.accept(TGIds.id("reaction_chamber/" + name), new ReactionChamberRecipe(input, focus, fluid, outputs, ticks, requiredCompletion,
                intensity, intensityMargin, liquidLevel, liquidConsumption, instability, risk, powerPerTick), null);
    }

    /** Techguns 2 Fabricator blocks and {@code TGMachineRecipes} Fabricator recipes. */
    private void fabricatorRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.FABRICATOR_HOUSING.get(), 4)
                .pattern("sms").pattern("pep").pattern("scs")
                .define('s', TGTags.Items.plate("steel")).define('m', TGItems.MECHANICAL_PARTS_CARBON.get())
                .define('p', TGItems.CYBERNETIC_PARTS.get()).define('e', TGItems.ELECTRIC_ENGINE.get()).define('c', TGItems.CIRCUIT_BOARD_ELITE.get())
                .unlockedBy(getHasName(TGItems.CIRCUIT_BOARD_ELITE.get()), has(TGItems.CIRCUIT_BOARD_ELITE.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.FABRICATOR_GLASS.get(), 4)
                .pattern("fgf").pattern("g g").pattern("fgf")
                .define('f', TGBlocks.FABRICATOR_HOUSING.get()).define('g', Tags.Items.GLASS_BLOCKS)
                .unlockedBy(getHasName(TGBlocks.FABRICATOR_HOUSING.get()), has(TGBlocks.FABRICATOR_HOUSING.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGBlocks.FABRICATOR_CONTROLLER.get())
                .pattern(" g ").pattern("cfc").pattern(" g ")
                .define('c', TGItems.CIRCUIT_BOARD_ELITE.get()).define('f', TGBlocks.FABRICATOR_HOUSING.get()).define('g', Tags.Items.GLASS_BLOCKS)
                .unlockedBy(getHasName(TGBlocks.FABRICATOR_HOUSING.get()), has(TGBlocks.FABRICATOR_HOUSING.get())).save(output);

        Ingredient redstone = Ingredient.of(Tags.Items.DUSTS_REDSTONE);
        Ingredient plastic = Ingredient.of(TGItems.PLASTIC_SHEET.get());
        Ingredient mechanicalParts = Ingredient.of(TGItems.MECHANICAL_PARTS_CARBON.get());
        Ingredient mechanicalPartsNetherite = Ingredient.of(TGItems.MECHANICAL_PARTS_NETHERITE.get());
        Ingredient circuit = Ingredient.of(TGItems.CIRCUIT_BOARD.get());
        Ingredient eliteCircuit = Ingredient.of(TGItems.CIRCUIT_BOARD_ELITE.get());
        Ingredient leadPlate = Ingredient.of(TGTags.Items.plate("lead"));
        fabricator(output, Ingredient.of(Tags.Items.INGOTS_GOLD), 1, Ingredient.of(TGItems.COPPER_WIRE.get()), 1, redstone, 3, plastic, 1,
                new ItemStack(TGAmmo.ENERGY_CELL_EMPTY.get()));
        fabricator(output, Ingredient.of(Items.SOUL_SAND), 1, Ingredient.of(TGItems.GOLD_WIRE.get()), 1, redstone, 1, plastic, 1,
                new ItemStack(TGItems.CYBERNETIC_PARTS.get()));
        fabricator(output, ingot("titanium"), 2, circuit, 4, mechanicalParts, 1, Ingredient.of(TGTags.Items.plate("carbon")), 4,
                new ItemStack(TGItems.POWER_PLATING.get(), 2));
        fabricator(output, Ingredient.of(TGItems.COIL.get()), 1, eliteCircuit, 2, mechanicalParts, 1, Ingredient.of(TGTags.Items.plate("titanium")), 1,
                new ItemStack(TGItems.SONIC_EMITTER.get()));
        fabricator(output, Ingredient.of(TGItems.ENRICHED_URANIUM.get()), 1, eliteCircuit, 2, mechanicalParts, 2, leadPlate, 2,
                new ItemStack(TGItems.RAD_EMITTER.get()));
        fabricator(output, ingot("steel"), 1, circuit, 1, redstone, 4, leadPlate, 2, new ItemStack(TGAmmo.NUCLEAR_POWER_CELL_EMPTY.get()));

        fabricator(output, ingot("titanium"), 2, eliteCircuit, 2, mechanicalPartsNetherite, 1, Ingredient.of(TGTags.Items.plate("netherite")), 2,
                new ItemStack(TGItems.POWER_PLATING_MK2.get(), 1));
        fabricator(output, ingot("netherite"), 2, Ingredient.of(TGItems.GOLD_WIRE.get()), 2, mechanicalPartsNetherite, 1, Ingredient.of(TGTags.Items.plate("titanium")), 2,
                new ItemStack(TGItems.NETHER_ARMOR_PLATING.get(), 2));
        fabricator(output, Ingredient.of(TGItems.CARBON_FIBERS), 2, Ingredient.of(TGItems.CYBERNETIC_PARTS.get()), 2, Ingredient.of(TGItems.GHAST_POWDER.get()), 1, Ingredient.of(TGTags.Items.plate("netherite")), 1,
                new ItemStack(TGItems.NANO_FIBERS.get(), 2));
    }

    private static void fabricator(RecipeOutput output, Ingredient input, int inputCount, Ingredient wires, int wireCount, Ingredient powder,
                                   int powderCount, Ingredient plate, int plateCount, ItemStack result) {
        output.accept(TGIds.id("fabricator/" + getItemName(result.getItem())), new FabricatorRecipe(new FabricatorRecipe.Input(input, inputCount),
                new FabricatorRecipe.Input(wires, wireCount), new FabricatorRecipe.Input(powder, powderCount),
                new FabricatorRecipe.Input(plate, plateCount), result), null);
    }

    /** Techguns 2 {@code CamoBenchRecipes}: vanilla colour blocks and the camo-changeable Techguns block groups. */
    private static void camoChangeRecipes(RecipeOutput output) {
        for (String kind : List.of("wool", "concrete", "concrete_powder", "terracotta", "stained_glass", "stained_glass_pane", "banner", "carpet")) {
            List<Item> entries = java.util.Arrays.stream(DyeColor.values())
                    .map(color -> BuiltInRegistries.ITEM.get(TGIds.vanilla(color.getName() + "_" + kind))).toList();
            output.accept(TGIds.id("camo_change/minecraft_" + kind), new CamoChangeRecipe(entries), null);
        }
        camoChange(output, "concrete", TGBlocks.CONCRETE);
        camoChange(output, "metal_panel", TGBlocks.METAL_PANEL);
        camoChange(output, "nether_metal", TGBlocks.NETHER_METAL);
        camoChange(output, "neon_lights", TGBlocks.NEON_LIGHTS);
        camoChange(output, "camo_net", TGBlocks.CAMO_NETS);
        camoChange(output, "camo_net_top", TGBlocks.CAMO_NETS_TOP);
        camoChange(output, "ladder", TGBlocks.LADDERS);
        camoChange(output, "lamp", TGBlocks.LAMPS);
        camoChange(output, "lantern", TGBlocks.LANTERNS);
    }

    private static void camoChange(RecipeOutput output, String name, List<? extends DeferredBlock<? extends Block>> blocks) {
        output.accept(TGIds.id("camo_change/" + name), new CamoChangeRecipe(blocks.stream().map(block -> block.get().asItem()).toList()), null);
    }

    /** Fabric {@code ammobench_crafting} recipes: one core, casing and powder each. */
    private static void ammoBenchRecipes(RecipeOutput output) {
        ammoBench(output, TGAmmo.PISTOL_ROUNDS.get(), 12);
        ammoBench(output, TGAmmo.SHOTGUN_ROUNDS.get(), 16);
        ammoBench(output, TGAmmo.RIFLE_ROUNDS.get(), 8);
        ammoBench(output, TGAmmo.RIFLE_ROUNDS_STACK.get(), 2);
        ammoBench(output, TGAmmo.SNIPER_ROUNDS.get(), 4);
    }

    private static void ammoBench(RecipeOutput output, ItemLike result, int count) {
        output.accept(TGIds.id("ammo_bench/" + getItemName(result)), new AmmoBenchRecipe(Ingredient.of(TGTags.Items.BULLET_CORE),
                Ingredient.of(TGTags.Items.BULLET_CASING), Ingredient.of(TGTags.Items.BULLET_POWDER), new ItemStack(result, count)), null);
    }

    private static void ammoPressRecipes(RecipeOutput output) {
        Ingredient bullet = Ingredient.fromValues(java.util.stream.Stream.of(
                new Ingredient.TagValue(TGTags.Items.ingot("steel")), new Ingredient.TagValue(TGTags.Items.ingot("lead"))));
        Ingredient casing = Ingredient.fromValues(java.util.stream.Stream.of(
                new Ingredient.TagValue(Tags.Items.INGOTS_COPPER), new Ingredient.TagValue(TGTags.Items.ingot("tin")),
                new Ingredient.TagValue(Tags.Items.INGOTS_IRON), new Ingredient.TagValue(TGTags.Items.ingot("bronze"))));
        Ingredient powder = Ingredient.of(Tags.Items.GUNPOWDERS);
        ammoPress(output, 0, "pistol_rounds", bullet, casing, powder, TGAmmo.PISTOL_ROUNDS.get(), 12);
        ammoPress(output, 1, "shotgun_rounds", bullet, casing, powder, TGAmmo.SHOTGUN_ROUNDS.get(), 16);
        ammoPress(output, 2, "rifle_rounds", bullet, casing, powder, TGAmmo.RIFLE_ROUNDS.get(), 8);
        // new in Techguns 3, like the Ammo Bench: the rifle rounds of one operation as stacks
        ammoPress(output, 3, "rifle_rounds_stack", bullet, casing, powder, TGAmmo.RIFLE_ROUNDS_STACK.get(), 2);
        ammoPress(output, 4, "sniper_rounds", bullet, casing, powder, TGAmmo.SNIPER_ROUNDS.get(), 4);
    }

    private static void ammoPress(RecipeOutput output, int order, String name, Ingredient bullet, Ingredient casing, Ingredient powder,
                                  ItemLike result, int count) {
        output.accept(TGIds.id("ammo_press/" + name), new AmmoPressRecipe(order, bullet, casing, powder, new ItemStack(result, count)), null);
    }

    /** Techguns 2 {@code BlastFurnaceRecipes}: power per tick and duration per recipe. */
    private static void blastFurnaceRecipes(RecipeOutput output) {
        blastFurnace(output, "steel_ingot", new SizedIngredient(Ingredient.of(Items.IRON_INGOT), 4),
                new SizedIngredient(Ingredient.of(ItemTags.COALS), 1), new ItemStack(TGItems.STEEL_INGOT.get(), 4), 10, 800);
        blastFurnace(output, "obsidian_steel_ingot", new SizedIngredient(ingot("steel"), 1),
                new SizedIngredient(Ingredient.of(Tags.Items.OBSIDIANS), 1), new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get()), 10, 200);
        blastFurnace(output, "bronze_ingot", new SizedIngredient(Ingredient.of(Tags.Items.INGOTS_COPPER), 3),
                new SizedIngredient(ingot("tin"), 1), new ItemStack(TGItems.BRONZE_INGOT.get(), 4), 10, 100);
    }

    private static void blastFurnace(RecipeOutput output, String name, SizedIngredient first, SizedIngredient second, ItemStack result,
                                     int powerPerTick, int time) {
        output.accept(TGIds.id("blast_furnace/" + name), new BlastFurnaceRecipe(first, second, result, powerPerTick, time), null);
    }

    /**
     * Techguns 2 grinder recipes: guns back into materials, carbon plates into fibers, rounds into nuggets and powder
     * (with chances). Techguns 2 gave steel ingots for the "nuggets" part of {@code steelAndPlastic}; steel nuggets here.
     */
    private static void grinderRecipes(RecipeOutput output) {
        ItemStack[] barrelStone = {new ItemStack(Items.COBBLESTONE, 3)};
        ItemStack[] barrelObsidian = {new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get(), 3)};
        ItemStack gold3 = new ItemStack(Items.GOLD_INGOT, 3);
        grinder(output, TGGuns.HAND_CANNON.get(), barrelStone, ironAndWood(0, 1, 4));
        grinder(output, TGGuns.SAWED_OFF.get(), ironAndWood(4, 1, 0), stacks(new ItemStack(Items.FLINT)));
        grinder(output, TGGuns.REVOLVER.get(), ironAndWood(3, 1, 0));
        grinder(output, TGGuns.GOLDEN_REVOLVER.get(), ironAndWood(3, 1, 0), stacks(new ItemStack(Items.GOLD_INGOT, 4)));
        grinder(output, TGGuns.THOMPSON.get(), ironAndWood(4, 1, 0));
        grinder(output, TGGuns.AK47.get(), barrelObsidian, ironAndWood(4, 1, 0));
        grinder(output, TGGuns.BOLT_ACTION.get(), barrelObsidian, ironAndWood(4, 1, 0));
        grinder(output, TGGuns.M4.get(), barrelObsidian, steelAndPlastic(2, 2, 0));
        grinder(output, TGGuns.M4_INFILTRATOR.get(), barrelObsidian, steelAndPlastic(3, 2, 0), stacks(new ItemStack(Items.REDSTONE)));
        grinder(output, TGGuns.PISTOL.get(), barrelObsidian, steelAndPlastic(0, 1, 0), ironAndWood(2, 0, 0));
        grinder(output, TGGuns.COMBAT_SHOTGUN.get(), barrelObsidian, steelAndPlastic(2, 2, 0));
        grinder(output, TGGuns.MAC10.get(), barrelObsidian, steelAndPlastic(1, 0, 5), ironAndWood(1, 0, 0));
        grinder(output, TGGuns.FLAMETHROWER.get(), steelAndPlastic(0, 2, 0), ironAndWood(2, 0, 0));
        grinder(output, TGGuns.ROCKET_LAUNCHER.get(), stacks(new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get(), 6)), steelAndPlastic(1, 0, 5));
        grinder(output, TGGuns.GRIM_REAPER.get(), stacks(new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get(), 32),
                new ItemStack(TGItems.CIRCUIT_BOARD_ELITE.get()), new ItemStack(TGItems.CARBON_FIBERS.get())));
        grinder(output, TGGuns.GRENADE_LAUNCHER.get(), barrelObsidian, steelAndPlastic(2, 2, 0));
        grinder(output, TGGuns.AUG.get(), barrelObsidian, steelAndPlastic(2, 2, 0));
        grinder(output, TGGuns.NETHER_BLASTER.get(), stacks(new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get(), 4),
                new ItemStack(TGItems.CYBERNETIC_PARTS.get(), 2)));
        grinder(output, TGGuns.BIOGUN.get(), steelAndPlastic(2, 2, 0), stacks(new ItemStack(Items.GLASS, 2)));
        grinder(output, TGGuns.TESLA_GUN.get(), carbonPlasticAndRedstone(3, 2, 2));
        grinder(output, TGGuns.LMG.get(), stacks(new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get(), 6), new ItemStack(TGItems.STEEL_INGOT.get(), 4),
                new ItemStack(TGItems.PLASTIC_SHEET.get(), 2)));
        grinder(output, TGGuns.MINIGUN.get(), stacks(new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get(), 20), new ItemStack(TGItems.STEEL_INGOT.get(), 2),
                new ItemStack(TGItems.ELECTRIC_ENGINE.get())));
        grinder(output, TGGuns.AS50.get(), stacks(new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get(), 9), new ItemStack(TGItems.PLASTIC_SHEET.get(), 2),
                new ItemStack(Items.DIAMOND)));
        grinder(output, TGGuns.VECTOR.get(), obsidianAndPlastic(5, 2));
        grinder(output, TGGuns.SCAR.get(), obsidianAndPlastic(5, 2));
        grinder(output, TGGuns.LASERGUN.get(), obsidianAndPlastic(2, 2), stacks(new ItemStack(Items.REDSTONE, 20), gold3));
        grinder(output, TGGuns.BLASTER_RIFLE.get(), carbonPlasticAndRedstone(3, 1, 20), stacks(gold3));
        grinder(output, TGGuns.SONIC_SHOTGUN.get(), carbonPlasticAndRedstone(3, 2, 2), stacks(new ItemStack(TGItems.TITANIUM_INGOT.get())));
        grinder(output, TGGuns.PDW.get(), carbonPlasticAndRedstone(6, 1, 0), obsidianAndPlastic(1, 0));
        grinder(output, TGGuns.PULSE_RIFLE.get(), carbonPlasticAndRedstone(6, 1, 0), obsidianAndPlastic(1, 0));
        grinder(output, TGGuns.MIB_GUN.get(), stacks(gold3, new ItemStack(TGItems.TITANIUM_INGOT.get()), new ItemStack(Items.REDSTONE, 20)));
        grinder(output, TGGuns.POWER_HAMMER.get(), ironAndWood(4, 0, 0), stacks(new ItemStack(Items.FLINT)));
        grinder(output, TGGuns.CHAINSAW.get(), ironAndWood(5, 0, 0), stacks(new ItemStack(TGItems.PLASTIC_SHEET.get())));
        grinder(output, TGGuns.NUCLEAR_DEATHRAY.get(), stacks(new ItemStack(TGItems.CARBON_FIBERS.get(), 6), new ItemStack(TGItems.LEAD_INGOT.get(), 3),
                new ItemStack(TGItems.MECHANICAL_PARTS_CARBON.get()), new ItemStack(TGItems.CIRCUIT_BOARD_ELITE.get())));
        grinder(output, TGGuns.GAUSS_RIFLE.get(), stacks(new ItemStack(TGItems.CARBON_FIBERS.get(), 9), new ItemStack(TGItems.TITANIUM_INGOT.get()),
                new ItemStack(TGItems.PLASTIC_SHEET.get()), new ItemStack(TGItems.CIRCUIT_BOARD_ELITE.get())));
        grinder(output, TGGuns.GUIDED_MISSILE_LAUNCHER.get(), stacks(new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get(), 8),
                new ItemStack(TGItems.COPPER_WIRE.get())));
        grinder(output, TGGuns.MINING_DRILL.get(), obsidianAndPlastic(6, 2));
        grinder(output, TGGuns.TFG.get(), stacks(new ItemStack(TGItems.TITANIUM_INGOT.get(), 5), new ItemStack(TGItems.LEAD_INGOT.get(), 4),
                new ItemStack(TGItems.CARBON_FIBERS.get(), 4)));
        grinder(output, TGItems.CARBON_PLATE.get(), stacks(new ItemStack(TGItems.CARBON_FIBERS.get())));

        // rounds: lead and copper nuggets, sometimes powder (blaze powder / TGX for special rounds)
        ItemStack lead = new ItemStack(TGItems.LEAD_NUGGET.get());
        ItemStack copper = new ItemStack(TGItems.COPPER_NUGGET.get());
        ItemStack powder = new ItemStack(Items.GUNPOWDER);
        ItemStack blaze = new ItemStack(Items.BLAZE_POWDER);
        roundsGrinder(output, TGAmmo.RIFLE_ROUNDS.get(), out(lead, 1), out(copper.copyWithCount(2), 1), out(powder, 0.125f));
        roundsGrinder(output, TGAmmo.PISTOL_ROUNDS.get(), out(lead, 0.75f), out(copper, 1.5f), out(powder, 1.0f / 12.0f));
        roundsGrinder(output, TGAmmo.SNIPER_ROUNDS.get(), out(lead.copyWithCount(2), 1), out(copper.copyWithCount(4), 1), out(powder, 0.25f));
        roundsGrinder(output, TGAmmo.SHOTGUN_ROUNDS.get(), out(lead, 0.5f), out(copper, 1), out(powder, 0.0625f));
        roundsGrinder(output, TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), out(lead, 1), out(copper.copyWithCount(2), 1), out(powder, 0.125f), out(blaze, 0.125f));
        roundsGrinder(output, TGAmmo.PISTOL_ROUNDS_INCENDIARY.get(), out(lead, 0.75f), out(copper, 1.5f), out(powder, 1.0f / 12.0f), out(blaze, 0.125f));
        roundsGrinder(output, TGAmmo.SNIPER_ROUNDS_INCENDIARY.get(), out(lead.copyWithCount(2), 1), out(copper.copyWithCount(4), 1), out(powder, 0.25f),
                out(blaze, 0.125f));
        roundsGrinder(output, TGAmmo.SHOTGUN_ROUNDS_INCENDIARY.get(), out(lead, 0.5f), out(copper, 1), out(powder, 0.0625f), out(blaze, 0.125f));
        roundsGrinder(output, TGAmmo.SNIPER_ROUNDS_EXPLOSIVE.get(), out(lead.copyWithCount(2), 1), out(copper.copyWithCount(4), 1), out(powder, 0.25f),
                out(blaze, 0.125f), out(new ItemStack(TGItems.TGX.get()), 0.5f));
    }

    private static ItemStack[] stacks(ItemStack... stacks) {
        return stacks;
    }

    private static ItemStack[] ironAndWood(int iron, int wood, int nuggets) {
        java.util.List<ItemStack> list = new java.util.ArrayList<>();
        if (iron > 0) list.add(new ItemStack(Items.IRON_INGOT, iron));
        if (wood > 0) list.add(new ItemStack(Items.OAK_LOG, wood));
        if (nuggets > 0) list.add(new ItemStack(Items.IRON_NUGGET, nuggets));
        return list.toArray(ItemStack[]::new);
    }

    private static ItemStack[] steelAndPlastic(int steel, int plastic, int nuggets) {
        java.util.List<ItemStack> list = new java.util.ArrayList<>();
        if (steel > 0) list.add(new ItemStack(TGItems.STEEL_INGOT.get(), steel));
        if (plastic > 0) list.add(new ItemStack(TGItems.PLASTIC_SHEET.get(), plastic));
        if (nuggets > 0) list.add(new ItemStack(TGItems.STEEL_NUGGET.get(), nuggets));
        return list.toArray(ItemStack[]::new);
    }

    private static ItemStack[] obsidianAndPlastic(int obsidianSteel, int plastic) {
        java.util.List<ItemStack> list = new java.util.ArrayList<>();
        if (obsidianSteel > 0) list.add(new ItemStack(TGItems.OBSIDIAN_STEEL_INGOT.get(), obsidianSteel));
        if (plastic > 0) list.add(new ItemStack(TGItems.PLASTIC_SHEET.get(), plastic));
        return list.toArray(ItemStack[]::new);
    }

    private static ItemStack[] carbonPlasticAndRedstone(int carbon, int plastic, int redstone) {
        java.util.List<ItemStack> list = new java.util.ArrayList<>();
        if (carbon > 0) list.add(new ItemStack(TGItems.CARBON_FIBERS.get(), carbon));
        if (plastic > 0) list.add(new ItemStack(TGItems.PLASTIC_SHEET.get(), plastic));
        if (redstone > 0) list.add(new ItemStack(Items.REDSTONE, redstone));
        return list.toArray(ItemStack[]::new);
    }

    private static void grinder(RecipeOutput output, ItemLike input, ItemStack[]... outputs) {
        java.util.List<GrinderRecipe.Output> list = java.util.Arrays.stream(outputs).flatMap(java.util.Arrays::stream)
                .map(GrinderRecipe.Output::of).toList();
        output.accept(TGIds.id("grinder/" + getItemName(input)), new GrinderRecipe(Ingredient.of(input), list), null);
    }

    private static GrinderRecipe.Output out(ItemStack stack, float chance) {
        return new GrinderRecipe.Output(stack, chance);
    }

    private static void roundsGrinder(RecipeOutput output, ItemLike input, GrinderRecipe.Output... outputs) {
        output.accept(TGIds.id("grinder/" + getItemName(input)), new GrinderRecipe(Ingredient.of(input), java.util.List.of(outputs)), null);
    }

    private static Ingredient ingot(String material) {
        return Ingredient.of(TGTags.Items.ingot(material));
    }

    private static Ingredient plate(String material) {
        return Ingredient.of(TGTags.Items.plate(material));
    }

    private static void metalPress(RecipeOutput output, String name, Ingredient first, Ingredient second, ItemLike result, int count) {
        output.accept(TGIds.id("metal_press/" + name), new MetalPressRecipe(first, second, true, new ItemStack(result, count)), null);
    }

    /** Techguns 2 decorative blocks; other variants of a group come from the Camo Bench. */
    private void buildBlockRecipes(RecipeOutput output) {
        Ingredient anyConcrete = Ingredient.of(Items.WHITE_CONCRETE, Items.ORANGE_CONCRETE, Items.MAGENTA_CONCRETE, Items.LIGHT_BLUE_CONCRETE,
                Items.YELLOW_CONCRETE, Items.LIME_CONCRETE, Items.PINK_CONCRETE, Items.GRAY_CONCRETE, Items.LIGHT_GRAY_CONCRETE, Items.CYAN_CONCRETE,
                Items.PURPLE_CONCRETE, Items.BLUE_CONCRETE, Items.BROWN_CONCRETE, Items.GREEN_CONCRETE, Items.RED_CONCRETE, Items.BLACK_CONCRETE);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TGBlocks.CONCRETE_BROWN.get(), 16)
                .pattern("ccc").pattern("cbc").pattern("ccc")
                .define('c', anyConcrete).define('b', Items.IRON_BARS)
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS)).save(output, TGIds.id("concrete_brown"));

        // neon lights: 16 double neon lights
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TGBlocks.NEONTUBES2.get(), 16)
                .pattern("nnn").pattern("gsg").pattern("nnn")
                .define('n', Tags.Items.NUGGETS_IRON).define('g', Tags.Items.GLASS_PANES).define('s', Tags.Items.DUSTS_GLOWSTONE)
                .unlockedBy("has_glowstone_dust", has(Tags.Items.DUSTS_GLOWSTONE)).save(output, TGIds.id("neontubes2"));
        // nether metal: 16 plates
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TGBlocks.NETHERMETAL_PANEL.get(), 16)
                .pattern("nsn").pattern("sis").pattern("nsn")
                .define('n', Items.NETHERRACK).define('s', Items.STONE).define('i', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_netherrack", has(Items.NETHERRACK)).save(output, TGIds.id("nethermetal_panel"));
        // metal panel: 32 red containers from iron or tin plates
        for (String plate : List.of("iron", "tin")) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TGBlocks.CONTAINER_RED.get(), 32)
                    .pattern("sss").pattern("psp").pattern("sss")
                    .define('s', Items.STONE).define('p', TGTags.Items.plate(plate))
                    .unlockedBy("has_" + plate + "_plate", has(TGTags.Items.plate(plate))).save(output, TGIds.id("container_red_from_" + plate + "_plate"));
        }
        buildShapedBlockRecipes(output);
        // stairs: 6 from a block, and back
        stairs(output, TGBlocks.PANEL_LARGE_BORDER_STAIRS.get(), TGBlocks.PANEL_LARGE_BORDER.get());
        stairs(output, TGBlocks.STEELFRAME_DARK_STAIRS.get(), TGBlocks.STEELFRAME_DARK.get());
        stairs(output, TGBlocks.CONCRETE_BROWN_LIGHT_STAIRS.get(), TGBlocks.CONCRETE_BROWN_LIGHT.get());
        stairs(output, TGBlocks.CONCRETE_GREY_DARK_STAIRS.get(), TGBlocks.CONCRETE_GREY_DARK.get());
    }

    /**
     * Phase 5a2 (Techguns 2 Recipewriter). Not crafted yet: the desert/snow camo nets and the shiny/rusty/carbon ladders
     * (Camo Bench), crates and the slimy ladder (no recipe in 1.12).
     */
    private void buildShapedBlockRecipes(RecipeOutput output) {
        // lamps: 16 from iron nuggets, glass panes and redstone; yellow and white convert into each other
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.LAMP_YELLOW.get(), 16)
                .pattern("iii").pattern("grg").pattern("ggg")
                .define('i', Tags.Items.NUGGETS_IRON).define('r', Tags.Items.DUSTS_REDSTONE).define('g', Tags.Items.GLASS_PANES)
                .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE)).save(output, TGIds.id("lamp_yellow"));
        swap(output, TGBlocks.LAMP_YELLOW.get(), TGBlocks.LAMP_WHITE.get());
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.LAMP_YELLOW_LANTERN.get(), 16)
                .pattern("iii").pattern("grg").pattern("iii")
                .define('i', Tags.Items.NUGGETS_IRON).define('r', Tags.Items.DUSTS_REDSTONE).define('g', Tags.Items.GLASS_PANES)
                .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE)).save(output, TGIds.id("lamp_yellow_lantern"));
        swap(output, TGBlocks.LAMP_YELLOW_LANTERN.get(), TGBlocks.LAMP_WHITE_LANTERN.get());

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, TGBlocks.SANDBAGS.get(), 8)
                .requires(TGItems.RUBBER_BAR.get()).requires(Ingredient.of(Tags.Items.SANDS), 8)
                .unlockedBy("has_rubber_bar", has(TGItems.RUBBER_BAR.get())).save(output, TGIds.id("sandbags"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.CAMO_NET_WOOD.get(), 8)
                .pattern("sds").pattern("sds")
                .define('s', Tags.Items.RODS_WOODEN).define('d', Items.DIRT)
                .unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN)).save(output, TGIds.id("camo_net_wood"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.CAMO_NET_TOP_WOOD.get(), 16)
                .pattern("wdw").pattern("dsd").pattern("wdw")
                .define('w', Tags.Items.RODS_WOODEN).define('d', Items.DIRT).define('s', Tags.Items.STRINGS)
                .unlockedBy("has_string", has(Tags.Items.STRINGS)).save(output, TGIds.id("camo_net_top_wood"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.BUNKER_DOOR.get(), 2)
                .pattern("pp").pattern("pp").pattern("pp")
                .define('p', TGTags.Items.plate("iron"))
                .unlockedBy("has_iron_plate", has(TGTags.Items.plate("iron"))).save(output, TGIds.id("bunker_door"));

        for (String plate : List.of("iron", "tin")) {
            TagKey<Item> plates = TGTags.Items.plate(plate);
            String suffix = "_from_" + plate + "_plate";
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.DOOR_METAL_3X3.get(), 2)
                    .pattern("ppp").pattern("sps").pattern("ppp").define('s', Items.PISTON).define('p', plates)
                    .unlockedBy("has_" + plate + "_plate", has(plates)).save(output, TGIds.id("door_metal_3x3" + suffix));
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.DOOR_HANGAR_UP_3X3.get(), 2)
                    .pattern("sps").pattern("ppp").pattern("ppp").define('s', Items.PISTON).define('p', plates)
                    .unlockedBy("has_" + plate + "_plate", has(plates)).save(output, TGIds.id("door_hangar_up_3x3" + suffix));
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TGBlocks.DOOR_HANGAR_DOWN_3X3.get(), 2)
                    .pattern("ppp").pattern("ppp").pattern("sps").define('s', Items.PISTON).define('p', plates)
                    .unlockedBy("has_" + plate + "_plate", has(plates)).save(output, TGIds.id("door_hangar_down_3x3" + suffix));
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.LADDER_METAL.get(), 16)
                    .pattern("iii").pattern(" i ").pattern("iii").define('i', plates)
                    .unlockedBy("has_" + plate + "_plate", has(plates)).save(output, TGIds.id("ladder_metal" + suffix));
        }
        swap(output, TGBlocks.DOOR_HANGAR_UP_3X3.get(), TGBlocks.DOOR_HANGAR_DOWN_3X3.get());

        // Techguns 3 ladders: 16 copper ladders from copper plates like the metal ladder; the oxidized copper, hazard and
        // nether ladders only come from the Camo Bench, which switches between all ladders
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TGBlocks.LADDER_COPPER.get(), 16)
                .pattern("iii").pattern(" i ").pattern("iii").define('i', TGTags.Items.plate("copper"))
                .unlockedBy("has_copper_plate", has(TGTags.Items.plate("copper"))).save(output, TGIds.id("ladder_copper"));
    }

    /** Two blocks that convert into each other with a shapeless recipe (Techguns 2 lamp/door variants). */
    private static void swap(RecipeOutput output, ItemLike first, ItemLike second) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, second).requires(first)
                .unlockedBy(getHasName(first), has(first)).save(output, TGIds.id(getItemName(second) + "_from_" + getItemName(first)));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, first).requires(second)
                .unlockedBy(getHasName(second), has(second)).save(output, TGIds.id(getItemName(first) + "_from_" + getItemName(second)));
    }

    /** Techguns 2 stairs recipes: 6 stairs from a block, and one stair back into a block. */
    private static void stairs(RecipeOutput output, ItemLike stairs, ItemLike block) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 6)
                .pattern("b  ").pattern("bb ").pattern("bbb")
                .define('b', block).unlockedBy(getHasName(block), has(block)).save(output, TGIds.id(getItemName(stairs)));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block)
                .requires(stairs).unlockedBy(getHasName(stairs), has(stairs)).save(output, TGIds.id(getItemName(block) + "_from_stairs"));
    }

    /** Techguns 2 shields: a vanilla shield framed by the material, with glass panes. */
    private void buildShieldRecipes(RecipeOutput output) {
        shield(output, TGArmors.RIOT_SHIELD.get(), TGTags.Items.ingot("steel"), "igi", "isi", "igi");
        shield(output, TGArmors.BALLISTIC_SHIELD.get(), TGTags.Items.plate("obsidian_steel"), "igi", "isi", "iii");
        shield(output, TGArmors.ADVANCED_SHIELD.get(), TGTags.Items.plate("carbon"), "igi", "isi", "iii");
        // not in Techguns 2: a ballistic shield plated with steel
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGArmors.HEAVY_SHIELD.get())
                .pattern("pgp").pattern("psp").pattern("ppp")
                .define('p', TGTags.Items.plate("steel")).define('g', Tags.Items.GLASS_PANES).define('s', TGArmors.BALLISTIC_SHIELD.get())
                .unlockedBy("has_ballistic_shield", has(TGArmors.BALLISTIC_SHIELD.get())).save(output, TGIds.id("heavy_shield"));
    }

    private static void shield(RecipeOutput output, Item shield, TagKey<Item> material, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, shield);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define('i', material).define('g', Tags.Items.GLASS_PANES).define('s', Items.SHIELD)
                .unlockedBy("has_shield", has(Items.SHIELD)).save(output, TGIds.id(getItemName(shield)));
    }

    private static void armor(RecipeOutput output, Item armor, ItemLike unlock, Consumer<ShapedRecipeBuilder> keys, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, armor);
        for (String row : pattern) {
            builder.pattern(row);
        }
        keys.accept(builder);
        builder.unlockedBy(getHasName(unlock), has(unlock)).save(output, TGIds.id(getItemName(armor)));
    }

    /** 9 nuggets and 1 ingot, both ways. */
    private void buildNuggetRecipes(RecipeOutput output) {
        nuggetsAndIngot(output, Items.COPPER_INGOT, TGItems.COPPER_NUGGET.get(), TGTags.Items.nugget("copper"), "copper");
        nuggetsAndIngot(output, TGItems.LEAD_INGOT.get(), TGItems.LEAD_NUGGET.get(), TGTags.Items.nugget("lead"), "lead");
        nuggetsAndIngot(output, TGItems.STEEL_INGOT.get(), TGItems.STEEL_NUGGET.get(), TGTags.Items.nugget("steel"), "steel");
    }

    private void buildMaterialRecipes(RecipeOutput output) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.BIOMASS.get(), 4)
                .pattern(" g ").pattern("gbg").pattern(" g ")
                .define('g', Tags.Items.DYES_GREEN).define('b', Tags.Items.SLIME_BALLS)
                .unlockedBy("has_slime_ball", has(Tags.Items.SLIME_BALLS)).save(output, TGIds.id("biomass"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.COPPER_WIRE.get())
                .pattern(" cc").pattern(" c ").pattern("cc ")
                .define('c', TGTags.Items.nugget("copper"))
                .unlockedBy("has_copper_nugget", has(TGTags.Items.nugget("copper"))).save(output, TGIds.id("copper_wire"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.HEAVY_CLOTH.get(), 3)
                .pattern(" w ").pattern("wlw").pattern(" w ")
                .define('w', ItemTags.WOOL).define('l', Items.LEATHER)
                .unlockedBy("has_leather", has(Items.LEATHER)).save(output, TGIds.id("heavy_cloth"));
    }

    private void buildComponentRecipes(RecipeOutput output) {
        TagKey<Item> carbonPlate = TGTags.Items.plate("carbon");
        TagKey<Item> obsidianSteel = TGTags.Items.ingot("obsidian_steel");
        TagKey<Item> iron = Tags.Items.INGOTS_IRON;

        // Mechanical parts
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.MECHANICAL_PARTS_IRON.get(), 2)
                .pattern(" i ").pattern("ifi").pattern(" i ")
                .define('f', Items.FLINT).define('i', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT)).save(output, TGIds.id("mechanical_parts_iron"));

        // Receivers
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.IRON_RECEIVER.get())
                .pattern("iii").pattern(" mn").pattern("  n")
                .define('i', iron).define('m', TGItems.MECHANICAL_PARTS_IRON.get()).define('n', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has_mechanical_parts_iron", has(TGItems.MECHANICAL_PARTS_IRON.get())).save(output, TGIds.id("iron_receiver"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.STEEL_RECEIVER.get())
                .pattern("iii").pattern(" mn").pattern("  n")
                .define('i', TGTags.Items.ingot("steel")).define('m', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get())
                .define('n', TGTags.Items.nugget("steel"))
                .unlockedBy("has_mechanical_parts_obsidian_steel", has(TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get()))
                .save(output, TGIds.id("steel_receiver"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.OBSIDIAN_STEEL_RECEIVER.get())
                .pattern("iii").pattern(" mi").pattern("  i")
                .define('i', obsidianSteel).define('m', TGItems.MECHANICAL_PARTS_CARBON.get())
                .unlockedBy("has_mechanical_parts_carbon", has(TGItems.MECHANICAL_PARTS_CARBON.get()))
                .save(output, TGIds.id("obsidian_steel_receiver"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.CARBON_RECEIVER.get())
                .pattern("ppp").pattern(" mi").pattern("  i")
                .define('p', carbonPlate).define('i', obsidianSteel).define('m', TGItems.MECHANICAL_PARTS_CARBON.get())
                .unlockedBy("has_mechanical_parts_carbon", has(TGItems.MECHANICAL_PARTS_CARBON.get()))
                .save(output, TGIds.id("carbon_receiver"));

        // Barrels
        barrel(output, TGItems.STONE_BARREL.get(), Ingredient.of(Items.STONE), has(Items.STONE));
        barrel(output, TGItems.IRON_BARREL.get(), Ingredient.of(Items.IRON_INGOT), has(Items.IRON_INGOT));
        barrel(output, TGItems.OBSIDIAN_STEEL_BARREL.get(), Ingredient.of(TGItems.OBSIDIAN_STEEL_INGOT.get()),
                has(TGItems.OBSIDIAN_STEEL_INGOT.get()));
        barrel(output, TGItems.CARBON_BARREL.get(), Ingredient.of(carbonPlate), has(carbonPlate));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.LASER_BARREL.get())
                .pattern("fff").pattern("ggl").pattern("fff")
                .define('f', Items.GOLD_INGOT).define('g', Tags.Items.GLASS_BLOCKS).define('l', TGItems.LASER_FOCUS.get())
                .unlockedBy("has_laser_focus", has(TGItems.LASER_FOCUS.get())).save(output, TGIds.id("laser_barrel"));

        // Stocks
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.WOOD_STOCK.get())
                .pattern("ww").pattern(" w")
                .define('w', ItemTags.LOGS)
                .unlockedBy("has_logs", has(ItemTags.LOGS)).save(output, TGIds.id("wood_stock"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.PLASTIC_STOCK.get())
                .pattern("ppp").pattern("  p")
                .define('p', TGItems.PLASTIC_SHEET.get())
                .unlockedBy("has_plastic_sheet", has(TGItems.PLASTIC_SHEET.get())).save(output, TGIds.id("plastic_stock"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGItems.CARBON_STOCK.get())
                .pattern("iii").pattern(" pp")
                .define('i', carbonPlate).define('p', TGItems.PLASTIC_SHEET.get())
                .unlockedBy("has_carbon_plate", has(carbonPlate)).save(output, TGIds.id("carbon_stock"));

        // Electronics
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.CIRCUIT_BOARD.get(), 4)
                .pattern("cgc").pattern("rir").pattern("cgc")
                .define('r', Items.REDSTONE).define('c', TGItems.COPPER_WIRE.get()).define('g', Tags.Items.DYES_GREEN).define('i', iron)
                .unlockedBy("has_copper_wire", has(TGItems.COPPER_WIRE.get())).save(output, TGIds.id("circuit_board"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TGItems.CIRCUIT_BOARD_ADVANCED.get())
                .requires(TGItems.CIRCUIT_BOARD.get()).requires(TGItems.GOLD_WIRE.get()).requires(Items.LAPIS_LAZULI)
                .unlockedBy("has_circuit_board", has(TGItems.CIRCUIT_BOARD.get())).save(output, TGIds.id("circuit_board_advanced"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.CIRCUIT_BOARD_ELITE.get(), 2)
                .pattern("cqc").pattern("ril").pattern("cgc")
                .define('q', Items.QUARTZ).define('r', Items.REDSTONE).define('c', TGItems.GOLD_WIRE.get())
                .define('g', Items.GHAST_TEAR).define('i', carbonPlate).define('l', Items.LAPIS_LAZULI)
                .unlockedBy("has_gold_wire", has(TGItems.GOLD_WIRE.get())).save(output, TGIds.id("circuit_board_elite"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.COIL.get())
                .pattern(" wi").pattern("wiw").pattern("iw ")
                .define('w', TGItems.COPPER_WIRE.get()).define('i', iron)
                .unlockedBy("has_copper_wire", has(TGItems.COPPER_WIRE.get())).save(output, TGIds.id("coil"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.ELECTRIC_ENGINE.get())
                .pattern("wrw").pattern("imi").pattern("wrw")
                .define('r', Items.REDSTONE).define('w', TGItems.COPPER_WIRE.get()).define('i', iron)
                .define('m', TGItems.MECHANICAL_PARTS_IRON.get())
                .unlockedBy("has_mechanical_parts_iron", has(TGItems.MECHANICAL_PARTS_IRON.get())).save(output, TGIds.id("electric_engine"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TGItems.PUMP_MECHANISM.get())
                .pattern("nnn").pattern("gpg").pattern("nnn")
                .define('p', Items.PISTON).define('g', Tags.Items.GLASS_BLOCKS).define('n', Items.IRON_INGOT)
                .unlockedBy("has_piston", has(Items.PISTON)).save(output, TGIds.id("pump_mechanism"));
    }

    private void buildAmmoRecipes(RecipeOutput output) {
        TagKey<Item> steelIngot = TGTags.Items.ingot("steel");
        TagKey<Item> steelNugget = TGTags.Items.nugget("steel");
        Item mechParts = TGItems.MECHANICAL_PARTS_IRON.get();

        // Empty magazines
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.PISTOL_MAGAZINE_EMPTY.get(), 4)
                .pattern("ii ").pattern("imi").pattern("igi")
                .define('g', Tags.Items.INGOTS_IRON).define('i', Tags.Items.NUGGETS_IRON).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("pistol_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.SMG_MAGAZINE_EMPTY.get(), 4)
                .pattern("ii ").pattern("ii ").pattern("imi")
                .define('i', Tags.Items.NUGGETS_IRON).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("smg_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get(), 4)
                .pattern("ss ").pattern("ss ").pattern("sms")
                .define('s', steelNugget).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("assault_rifle_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.LMG_MAGAZINE_EMPTY.get(), 4)
                .pattern("ii ").pattern("imi").pattern("iii")
                .define('i', steelIngot).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("lmg_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.MINIGUN_DRUM_EMPTY.get(), 4)
                .pattern("sss").pattern("pmp").pattern("sss")
                .define('p', TGItems.PLASTIC_SHEET.get()).define('s', steelIngot)
                .define('m', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get())
                .unlockedBy("has_mechanical_parts_obsidian_steel", has(TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get()))
                .save(output, TGIds.id("minigun_drum_empty"));

        // Refill: empty magazine + loose rounds
        refill(output, TGAmmo.PISTOL_MAGAZINE.get(), TGAmmo.PISTOL_MAGAZINE_EMPTY.get(), TGAmmo.PISTOL_ROUNDS.get(), 3, "pistol_magazine");
        refill(output, TGAmmo.PISTOL_MAGAZINE_INCENDIARY.get(), TGAmmo.PISTOL_MAGAZINE_EMPTY.get(),
                TGAmmo.PISTOL_ROUNDS_INCENDIARY.get(), 3, "pistol_magazine_incendiary");
        refill(output, TGAmmo.SMG_MAGAZINE.get(), TGAmmo.SMG_MAGAZINE_EMPTY.get(), TGAmmo.PISTOL_ROUNDS.get(), 2, "smg_magazine");
        refill(output, TGAmmo.SMG_MAGAZINE_INCENDIARY.get(), TGAmmo.SMG_MAGAZINE_EMPTY.get(),
                TGAmmo.PISTOL_ROUNDS_INCENDIARY.get(), 2, "smg_magazine_incendiary");
        refill(output, TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(), TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get(), TGAmmo.RIFLE_ROUNDS.get(), 3,
                "assault_rifle_magazine");
        refill(output, TGAmmo.ASSAULT_RIFLE_MAGAZINE_INCENDIARY.get(), TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get(),
                TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), 3, "assault_rifle_magazine_incendiary");
        // LMG: 8 rounds around the empty magazine (shaped), or 8 incendiary rounds shapeless
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.LMG_MAGAZINE.get())
                .pattern("bbb").pattern("bmb").pattern("bbb")
                .define('b', TGAmmo.RIFLE_ROUNDS.get()).define('m', TGAmmo.LMG_MAGAZINE_EMPTY.get())
                .unlockedBy("has_lmg_magazine_empty", has(TGAmmo.LMG_MAGAZINE_EMPTY.get())).save(output, TGIds.id("lmg_magazine"));
        refill(output, TGAmmo.LMG_MAGAZINE_INCENDIARY.get(), TGAmmo.LMG_MAGAZINE_EMPTY.get(), TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), 8,
                "lmg_magazine_incendiary");

        TagKey<Item> obsidianSteel = TGTags.Items.ingot("obsidian_steel");
        TagKey<Item> titanium = TGTags.Items.ingot("titanium");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.AS50_MAGAZINE_EMPTY.get(), 4)
                .pattern("s s").pattern("s s").pattern("sms")
                .define('s', steelIngot).define('m', mechParts)
                .unlockedBy("has_mechanical_parts_iron", has(mechParts)).save(output, TGIds.id("as50_magazine_empty"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.ADVANCED_MAGAZINE_EMPTY.get(), 4)
                .pattern("i i").pattern("i i").pattern("imi")
                .define('i', obsidianSteel).define('m', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get())
                .unlockedBy("has_mechanical_parts_obsidian_steel", has(TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get()))
                .save(output, TGIds.id("advanced_magazine_empty"));
        refill(output, TGAmmo.AS50_MAGAZINE.get(), TGAmmo.AS50_MAGAZINE_EMPTY.get(), TGAmmo.SNIPER_ROUNDS.get(), 2, "as50_magazine");
        refill(output, TGAmmo.AS50_MAGAZINE_INCENDIARY.get(), TGAmmo.AS50_MAGAZINE_EMPTY.get(), TGAmmo.SNIPER_ROUNDS_INCENDIARY.get(), 2,
                "as50_magazine_incendiary");
        refill(output, TGAmmo.AS50_MAGAZINE_EXPLOSIVE.get(), TGAmmo.AS50_MAGAZINE_EMPTY.get(), TGAmmo.SNIPER_ROUNDS_EXPLOSIVE.get(), 2,
                "as50_magazine_explosive");
        refill(output, TGAmmo.ADVANCED_MAGAZINE.get(), TGAmmo.ADVANCED_MAGAZINE_EMPTY.get(), TGAmmo.ADVANCED_ROUNDS.get(), 3,
                "advanced_magazine");
        // advanced rounds, gauss slugs and filled tanks come from the Metal Press and Chem Lab (Techguns 2)

        // Batch 3c power sources
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.REDSTONE_BATTERY.get(), 2)
                .pattern("nwn").pattern("nrn").pattern("nrn")
                .define('r', Items.REDSTONE).define('w', TGItems.COPPER_WIRE.get()).define('n', TGTags.Items.nugget("copper"))
                .unlockedBy("has_copper_wire", has(TGItems.COPPER_WIRE.get())).save(output, TGIds.id("redstone_battery"));

        // Rounds and rockets
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.ROCKET.get(), 4)
                .pattern(" s ").pattern("sts").pattern("sgs")
                .define('s', Tags.Items.NUGGETS_IRON).define('t', Items.TNT).define('g', Items.GUNPOWDER)
                .unlockedBy("has_tnt", has(Items.TNT)).save(output, TGIds.id("rocket"));
        // nuke rockets need the tactical nuke warhead, high velocity rockets the chemical laboratory (Phase 5)
        // Batch 3e ammo (the magic rifle's bullets have no recipe in Fabric)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.FUEL_TANK_EMPTY.get(), 4)
                .pattern("p").pattern("g").pattern("p")
                .define('p', TGItems.PLASTIC_SHEET.get()).define('g', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_plastic_sheet", has(TGItems.PLASTIC_SHEET.get())).save(output, TGIds.id("fuel_tank_empty"));
        // Batch 3g: rifle round stacks refill the minigun drum
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.RIFLE_ROUNDS_STACK.get())
                .requires(TGAmmo.RIFLE_ROUNDS.get(), 4)
                .unlockedBy("has_rifle_rounds", has(TGAmmo.RIFLE_ROUNDS.get())).save(output, TGIds.id("rifle_rounds_stack"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.RIFLE_ROUNDS_STACK_INCENDIARY.get())
                .requires(TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), 4)
                .unlockedBy("has_rifle_rounds_incendiary", has(TGAmmo.RIFLE_ROUNDS_INCENDIARY.get()))
                .save(output, TGIds.id("rifle_rounds_stack_incendiary"));
        // Techguns 2: stacks split back into four rounds
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.RIFLE_ROUNDS.get(), 4)
                .requires(TGAmmo.RIFLE_ROUNDS_STACK.get())
                .unlockedBy("has_rifle_rounds_stack", has(TGAmmo.RIFLE_ROUNDS_STACK.get())).save(output, TGIds.id("rifle_rounds_from_stack"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), 4)
                .requires(TGAmmo.RIFLE_ROUNDS_STACK_INCENDIARY.get())
                .unlockedBy("has_rifle_rounds_stack_incendiary", has(TGAmmo.RIFLE_ROUNDS_STACK_INCENDIARY.get()))
                .save(output, TGIds.id("rifle_rounds_incendiary_from_stack"));
        refill(output, TGAmmo.MINIGUN_DRUM.get(), TGAmmo.MINIGUN_DRUM_EMPTY.get(), TGAmmo.RIFLE_ROUNDS_STACK.get(), 4, "minigun_drum");
        refill(output, TGAmmo.MINIGUN_DRUM_INCENDIARY.get(), TGAmmo.MINIGUN_DRUM_EMPTY.get(), TGAmmo.RIFLE_ROUNDS_STACK_INCENDIARY.get(), 4,
                "minigun_drum_incendiary");

        // Batch 3f: compressed air tanks and tool mining heads
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.COMPRESSED_AIR_TANK_EMPTY.get(), 4)
                .pattern(" l ").pattern("i i").pattern("i i")
                .define('l', Items.LEVER).define('i', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON)).save(output, TGIds.id("compressed_air_tank_empty"));
        TagKey<Item> obsidianSteelIngot = TGTags.Items.ingot("obsidian_steel");
        TagKey<Item> carbonPlate = TGTags.Items.plate("carbon");
        miningHead(output, TGItems.CHAINSAW_BLADES_STEEL.get(), steelIngot, TGItems.MECHANICAL_PARTS_IRON.get(), " i", "ip", " i");
        miningHead(output, TGItems.CHAINSAW_BLADES_OBSIDIAN.get(), obsidianSteelIngot, TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get(),
                " i", "ip", " i");
        miningHead(output, TGItems.CHAINSAW_BLADES_CARBON.get(), carbonPlate, TGItems.MECHANICAL_PARTS_CARBON.get(), " i", "ip", " i");
        miningHead(output, TGItems.MINING_DRILL_HEAD_OBSIDIAN.get(), obsidianSteelIngot, null, " ii", "iii", " ii");
        miningHead(output, TGItems.MINING_DRILL_HEAD_CARBON.get(), carbonPlate, null, " ii", "iii", " ii");
        miningHead(output, TGItems.POWER_HAMMER_HEAD_STEEL.get(), steelIngot, null, "i  ", "iii", "i  ");
        miningHead(output, TGItems.POWER_HAMMER_HEAD_OBSIDIAN.get(), obsidianSteelIngot, null, "i  ", "iii", "i  ");
        miningHead(output, TGItems.POWER_HAMMER_HEAD_CARBON.get(), carbonPlate, null, "i  ", "iii", "i  ");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGAmmo.BIO_TANK_EMPTY.get())
                .pattern("sss").pattern("sgs").pattern("sgs")
                .define('s', steelNugget).define('g', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_steel_nugget", has(steelNugget)).save(output, TGIds.id("bio_tank_empty"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, TGAmmo.STONE_BULLETS.get(), 16)
                .requires(Items.COBBLESTONE, 3).requires(Items.GUNPOWDER)
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER)).save(output, TGIds.id("stone_bullets"));

        // hand grenades (Techguns 2)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGGuns.STIELGRANATE.get(), 16)
                .pattern(" it").pattern(" wi").pattern("i  ")
                .define('i', Tags.Items.INGOTS_IRON).define('w', ItemTags.PLANKS).define('t', Items.TNT)
                .unlockedBy("has_tnt", has(Items.TNT)).save(output, TGIds.id("stielgranate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, TGGuns.FRAG_GRENADE.get(), 16)
                .pattern(" if").pattern("iti").pattern(" i ")
                .define('i', steelIngot).define('f', Items.FLINT_AND_STEEL).define('t', Items.TNT)
                .unlockedBy("has_tnt", has(Items.TNT)).save(output, TGIds.id("frag_grenade"));
    }

    /** Shapeless: empty magazine plus {@code rounds} loose rounds gives the filled magazine. */
    private static void refill(RecipeOutput output, ItemLike result, ItemLike empty, ItemLike round, int rounds, String id) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, result)
                .requires(empty).requires(round, rounds)
                .unlockedBy(getHasName(empty), has(empty)).save(output, TGIds.id(id));
    }

    private void buildGunRecipes(RecipeOutput output) {
        TagKey<Item> steelIngot = TGTags.Items.ingot("steel");
        Item obsBarrel = TGItems.OBSIDIAN_STEEL_BARREL.get();
        Item steelReceiver = TGItems.STEEL_RECEIVER.get();
        Item obsReceiver = TGItems.OBSIDIAN_STEEL_RECEIVER.get();
        Item ironReceiver = TGItems.IRON_RECEIVER.get();
        Item plasticStock = TGItems.PLASTIC_STOCK.get();
        Item woodStock = TGItems.WOOD_STOCK.get();
        Item plastic = TGItems.PLASTIC_SHEET.get();
        Item glassPane = Items.GLASS_PANE;

        // M4: loaded / empty / incendiary differ only in the magazine and the result's ammo
        Item m4 = TGGuns.M4.get();
        Consumer<StackShapedRecipeBuilder> m4Keys = b -> b.define('r', Items.REDSTONE).define('b', obsBarrel).define('s', plasticStock)
                .define('f', steelReceiver).define('g', glassPane).define('i', Tags.Items.INGOTS_IRON).define('n', Tags.Items.NUGGETS_IRON);
        gun(output, m4, 30, AmmoType.DEFAULT, "m4", TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(), m4Keys, "gr", "bfs", "nmi");
        gun(output, m4, 0, AmmoType.DEFAULT, "m4_empty", TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get(), m4Keys, "gr", "bfs", "nmi");
        gun(output, m4, 30, AmmoType.INCENDIARY, "m4_incendiary", TGAmmo.ASSAULT_RIFLE_MAGAZINE_INCENDIARY.get(), m4Keys,
                "gr", "bfs", "nmi");

        // Pistol
        Item pistol = TGGuns.PISTOL.get();
        Consumer<StackShapedRecipeBuilder> pistolKeys = b -> b.define('p', plastic).define('b', obsBarrel).define('s', steelIngot)
                .define('f', TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get());
        gun(output, pistol, 18, AmmoType.DEFAULT, "pistol", TGAmmo.PISTOL_MAGAZINE.get(), pistolKeys, "bsp", "fp", "m");
        gun(output, pistol, 0, AmmoType.DEFAULT, "pistol_empty", TGAmmo.PISTOL_MAGAZINE_EMPTY.get(), pistolKeys, "bsp", "fp", "m");
        gun(output, pistol, 18, AmmoType.INCENDIARY, "pistol_incendiary", TGAmmo.PISTOL_MAGAZINE_INCENDIARY.get(), pistolKeys,
                "bsp", "fp", "m");

        // Lasergun
        Item laser = TGGuns.LASERGUN.get();
        Consumer<StackShapedRecipeBuilder> laserKeys = b -> b.define('p', TGItems.MECHANICAL_PARTS_CARBON.get())
                .define('b', TGItems.LASER_BARREL.get()).define('r', obsReceiver).define('s', plasticStock)
                .define('d', Items.REDSTONE).define('g', glassPane);
        gun(output, laser, 45, AmmoType.DEFAULT, "lasergun", TGAmmo.ENERGY_CELL.get(), laserKeys, "gd", "brs", "mp");
        gun(output, laser, 0, AmmoType.DEFAULT, "lasergun_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), laserKeys, "gd", "brs", "mp");

        // Rocket launcher: the loaded one has a rocket left of the barrels
        Item rocketLauncher = TGGuns.ROCKET_LAUNCHER.get();
        Consumer<StackShapedRecipeBuilder> rocketKeys = b -> b.define('b', obsBarrel).define('f', steelReceiver);
        gun(output, rocketLauncher, 1, AmmoType.DEFAULT, "rocket_launcher", TGAmmo.ROCKET.get(), rocketKeys, "mbb", "f");
        gun(output, rocketLauncher, 0, AmmoType.DEFAULT, "rocket_launcher_empty", null, rocketKeys, "bb", "f");

        // Revolver and bolt action: empty only (Fabric has no loaded recipe)
        gun(output, TGGuns.REVOLVER.get(), 0, AmmoType.DEFAULT, "revolver_empty", null,
                b -> b.define('b', TGItems.IRON_BARREL.get()).define('s', woodStock).define('f', Items.FLINT_AND_STEEL)
                        .define('m', TGItems.MECHANICAL_PARTS_IRON.get()),
                "bmf", "s");
        gun(output, TGGuns.BOLT_ACTION.get(), 0, AmmoType.DEFAULT, "bolt_action_empty", null,
                b -> b.define('b', obsBarrel).define('s', woodStock).define('f', ironReceiver)
                        .define('g', Tags.Items.GLASS_BLOCKS).define('i', Tags.Items.INGOTS_IRON),
                "gi", "bfs");

        gun(output, TGGuns.AK47.get(), 30, AmmoType.DEFAULT, "ak47", TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(),
                b -> b.define('b', obsBarrel).define('s', woodStock).define('f', ironReceiver).define('w', ItemTags.LOGS),
                "bfs", "wm");
        gun(output, TGGuns.AUG.get(), 30, AmmoType.DEFAULT, "aug", TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(),
                b -> b.define('p', steelIngot).define('b', obsBarrel).define('s', plasticStock).define('f', steelReceiver)
                        .define('g', glassPane).define('i', plastic),
                "gpg", "bfs", "im");
        gun(output, TGGuns.SCAR.get(), 20, AmmoType.DEFAULT, "scar", TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(),
                b -> b.define('p', plastic).define('r', obsReceiver).define('b', obsBarrel).define('s', plasticStock)
                        .define('d', Items.DIAMOND).define('g', Tags.Items.GLASS_BLOCKS),
                "gpd", "brs", "m");
        gun(output, TGGuns.THOMPSON.get(), 20, AmmoType.DEFAULT, "thompson", TGAmmo.SMG_MAGAZINE.get(),
                b -> b.define('b', TGItems.IRON_BARREL.get()).define('s', woodStock).define('f', ironReceiver),
                "bfs", "m");
        gun(output, TGGuns.MAC10.get(), 32, AmmoType.DEFAULT, "mac10", TGAmmo.SMG_MAGAZINE.get(),
                b -> b.define('p', plastic).define('r', steelReceiver).define('b', obsBarrel).define('i', Tags.Items.NUGGETS_IRON),
                "pi", "bri", "m");
        gun(output, TGGuns.VECTOR.get(), 25, AmmoType.DEFAULT, "vector", TGAmmo.SMG_MAGAZINE.get(),
                b -> b.define('r', obsReceiver).define('b', obsBarrel).define('s', plasticStock).define('d', Items.REDSTONE)
                        .define('g', glassPane),
                "gd", "brs", "m");
        gun(output, TGGuns.LMG.get(), 100, AmmoType.DEFAULT, "lmg", TGAmmo.LMG_MAGAZINE.get(),
                b -> b.define('r', Items.REDSTONE).define('b', obsBarrel).define('s', plasticStock).define('f', obsReceiver)
                        .define('g', glassPane),
                "gr", "bfs", "m");
        gun(output, TGGuns.MINIGUN.get(), 200, AmmoType.DEFAULT, "minigun", TGAmmo.MINIGUN_DRUM.get(),
                b -> b.define('b', obsBarrel).define('r', obsReceiver).define('e', TGItems.ELECTRIC_ENGINE.get()),
                "bbe", "bbr", "bbm");

        // --- Batch 3b ---
        TagKey<Item> obsidianSteel = TGTags.Items.ingot("obsidian_steel");
        TagKey<Item> titanium = TGTags.Items.ingot("titanium");
        Item carbonBarrel = TGItems.CARBON_BARREL.get();
        Item carbonReceiver = TGItems.CARBON_RECEIVER.get();
        Item carbonStock = TGItems.CARBON_STOCK.get();
        Item advancedCircuit = TGItems.CIRCUIT_BOARD_ADVANCED.get();

        // Shotguns: empty only (Fabric has no loaded recipe)
        gun(output, TGGuns.SAWED_OFF.get(), 0, AmmoType.DEFAULT, "sawed_off_empty", null,
                b -> b.define('b', TGItems.IRON_BARREL.get()).define('f', Items.FLINT_AND_STEEL).define('w', woodStock),
                "bfw", "bf ");
        gun(output, TGGuns.COMBAT_SHOTGUN.get(), 0, AmmoType.DEFAULT, "combat_shotgun_empty", null,
                b -> b.define('b', obsBarrel).define('s', plasticStock).define('f', steelReceiver).define('i', steelIngot),
                "bfs", "i  ");

        Item as50 = TGGuns.AS50.get();
        Consumer<StackShapedRecipeBuilder> as50Keys = b -> b.define('p', obsidianSteel).define('b', obsBarrel).define('t', plasticStock)
                .define('d', Items.DIAMOND).define('f', obsReceiver);
        gun(output, as50, 10, AmmoType.DEFAULT, "as50", TGAmmo.AS50_MAGAZINE.get(), as50Keys, "dpd", "bbf", " mt");
        gun(output, as50, 0, AmmoType.DEFAULT, "as50_empty", TGAmmo.AS50_MAGAZINE_EMPTY.get(), as50Keys, "dpd", "bbf", " mt");
        gun(output, as50, 10, AmmoType.INCENDIARY, "as50_incendiary", TGAmmo.AS50_MAGAZINE_INCENDIARY.get(), as50Keys,
                "dpd", "bbf", " mt");
        gun(output, as50, 10, AmmoType.EXPLOSIVE, "as50_explosive", TGAmmo.AS50_MAGAZINE_EXPLOSIVE.get(), as50Keys,
                "dpd", "bbf", " mt");

        // Gauss rifle: the loaded one takes slugs ('a') and a full cell ('e'), the empty one an empty cell
        Item gauss = TGGuns.GAUSS_RIFLE.get();
        Consumer<StackShapedRecipeBuilder> gaussKeys = b -> b.define('p', titanium).define('b', TGItems.GAUSS_BARREL.get())
                .define('r', carbonReceiver).define('s', carbonStock).define('c', advancedCircuit).define('d', Items.DIAMOND);
        gun(output, gauss, 8, AmmoType.DEFAULT, "gauss_rifle",  null,
                gaussKeys.andThen(b -> b.define('a', TGAmmo.GAUSS_RIFLE_SLUGS.get()).define('e', TGAmmo.ENERGY_CELL.get())),
                "dpc", "brs", " ae");
        gun(output, gauss, 0, AmmoType.DEFAULT, "gauss_rifle_empty", null,
                gaussKeys.andThen(b -> b.define('e', TGAmmo.ENERGY_CELL_EMPTY.get())), "dpc", "brs", "  e");

        Item blaster = TGGuns.BLASTER_RIFLE.get();
        Consumer<StackShapedRecipeBuilder> blasterKeys = b -> b.define('p', TGTags.Items.plate("carbon"))
                .define('b', TGItems.LASER_BARREL.get()).define('r', carbonReceiver).define('c', advancedCircuit)
                .define('s', carbonStock).define('g', Tags.Items.GLASS_BLOCKS);
        gun(output, blaster, 50, AmmoType.DEFAULT, "blaster_rifle", TGAmmo.ENERGY_CELL.get(), blasterKeys, "pcg", "brs", " m ");
        gun(output, blaster, 0, AmmoType.DEFAULT, "blaster_rifle_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), blasterKeys,
                "pcg", "brs", " m ");
        // blaster shotgun: no Fabric recipe

        Item pulse = TGGuns.PULSE_RIFLE.get();
        Consumer<StackShapedRecipeBuilder> pulseKeys = b -> b.define('p', titanium).define('b', carbonBarrel).define('r', carbonReceiver)
                .define('s', carbonStock).define('c', advancedCircuit).define('d', Items.DIAMOND);
        gun(output, pulse, 12, AmmoType.DEFAULT, "pulse_rifle", TGAmmo.ADVANCED_MAGAZINE.get(), pulseKeys, "dpc", "brs", " m ");
        gun(output, pulse, 0, AmmoType.DEFAULT, "pulse_rifle_empty", TGAmmo.ADVANCED_MAGAZINE_EMPTY.get(), pulseKeys,
                "dpc", "brs", " m ");

        Item pdw = TGGuns.PDW.get();
        Consumer<StackShapedRecipeBuilder> pdwKeys = b -> b.define('b', carbonBarrel).define('r', carbonReceiver).define('s', carbonStock);
        gun(output, pdw, 40, AmmoType.DEFAULT, "pdw", TGAmmo.ADVANCED_MAGAZINE.get(), pdwKeys, "brs", " m ");
        gun(output, pdw, 0, AmmoType.DEFAULT, "pdw_empty", TGAmmo.ADVANCED_MAGAZINE_EMPTY.get(), pdwKeys, "brs", " m ");

        // --- Batch 3e (the magic rifle has no recipe in Fabric) ---
        Item flamethrower = TGGuns.FLAMETHROWER.get();
        Consumer<StackShapedRecipeBuilder> flamethrowerKeys = b -> b.define('p', TGItems.PUMP_MECHANISM.get()).define('r', ironReceiver)
                .define('s', plasticStock).define('f', Items.FLINT_AND_STEEL);
        gun(output, flamethrower, 100, AmmoType.DEFAULT, "flamethrower", TGAmmo.FUEL_TANK.get(), flamethrowerKeys, "prs", "fm ");
        gun(output, flamethrower, 0, AmmoType.DEFAULT, "flamethrower_empty", TGAmmo.FUEL_TANK_EMPTY.get(), flamethrowerKeys, "prs", "fm ");
        Item biogun = TGGuns.BIOGUN.get();
        Consumer<StackShapedRecipeBuilder> biogunKeys = b -> b.define('r', steelReceiver).define('s', plasticStock)
                .define('f', TGItems.PUMP_MECHANISM.get());
        gun(output, biogun, 30, AmmoType.DEFAULT, "biogun", TGAmmo.BIO_TANK.get(), biogunKeys, "frs", " m ");
        gun(output, biogun, 0, AmmoType.DEFAULT, "biogun_empty", TGAmmo.BIO_TANK_EMPTY.get(), biogunKeys, "frs", " m ");
        Item tfg = TGGuns.TFG.get();
        Consumer<StackShapedRecipeBuilder> tfgKeys = b -> b.define('p', TGItems.PLASMA_GENERATOR.get())
                .define('b', TGItems.SHIELDED_TITANIUM_BARREL.get()).define('r', TGItems.TITANIUM_RECEIVER.get())
                .define('t', TGTags.Items.ingot("titanium"));
        gun(output, tfg, 20, AmmoType.DEFAULT, "tfg", TGAmmo.NUCLEAR_POWER_CELL.get(), tfgKeys, "t  ", "bpr", "tm ");
        gun(output, tfg, 0, AmmoType.DEFAULT, "tfg_empty", TGAmmo.NUCLEAR_POWER_CELL_EMPTY.get(), tfgKeys, "t  ", "bpr", "tm ");
        gun(output, TGGuns.HAND_CANNON.get(), 0, AmmoType.DEFAULT, "hand_cannon_empty", null,
                b -> b.define('b', TGItems.STONE_BARREL.get()).define('s', woodStock).define('f', Items.FLINT_AND_STEEL), "bfs");

        // --- Batch 3d ---
        gun(output, rocketLauncher, 1, AmmoType.HIGH_VELOCITY, "rocket_launcher_high_velocity", TGAmmo.ROCKET_HIGH_VELOCITY.get(), rocketKeys,
                "mbb", "f");
        gun(output, rocketLauncher, 1, AmmoType.NUKE, "rocket_launcher_nuke", TGAmmo.ROCKET_NUKE.get(), rocketKeys, "mbb", "f");
        gun(output, TGGuns.GRENADE_LAUNCHER.get(), 0, AmmoType.DEFAULT, "grenade_launcher_empty", null,
                b -> b.define('p', steelIngot).define('b', obsBarrel).define('r', steelReceiver).define('s', plasticStock),
                "brs", " p ");
        Item guided = TGGuns.GUIDED_MISSILE_LAUNCHER.get();
        Consumer<StackShapedRecipeBuilder> guidedKeys = b -> b.define('b', obsBarrel).define('c', TGItems.CIRCUIT_BOARD_ADVANCED.get())
                .define('f', obsReceiver).define('g', Tags.Items.GLASS_BLOCKS);
        gun(output, guided, 1, AmmoType.DEFAULT, "guided_missile_launcher", TGAmmo.ROCKET.get(), guidedKeys, " gc", "mbb", " f ");
        gun(output, guided, 1, AmmoType.HIGH_VELOCITY, "guided_missile_launcher_high_velocity", TGAmmo.ROCKET_HIGH_VELOCITY.get(), guidedKeys,
                " gc", "mbb", " f ");
        gun(output, guided, 0, AmmoType.DEFAULT, "guided_missile_launcher_empty", null, guidedKeys, "gc", "bb", "f ");
        // the Grim Reaper: four guided missile launchers, their rockets carry over
        transfer(output, TGGuns.GRIM_REAPER.get(), "grim_reaper",
                b -> b.define('r', guided).define('c', TGItems.CARBON_RECEIVER.get()), "rr ", "rrc");

        // --- Batch 3c ---
        Item laserPistol = TGGuns.LASER_PISTOL.get();
        Consumer<StackShapedRecipeBuilder> laserPistolKeys = b -> b.define('p', plastic).define('b', TGItems.LASER_BARREL.get())
                .define('s', TGTags.Items.nugget("steel")).define('c', advancedCircuit).define('o', obsidianSteel);
        gun(output, laserPistol, 20, AmmoType.DEFAULT, "laser_pistol", TGAmmo.REDSTONE_BATTERY.get(), laserPistolKeys, "obo", "scm", "ssp");
        gun(output, laserPistol, 0, AmmoType.DEFAULT, "laser_pistol_empty", TGAmmo.REDSTONE_BATTERY_EMPTY.get(), laserPistolKeys,
                "obo", "scm", "ssp");

        Item ndr = TGGuns.NUCLEAR_DEATHRAY.get();
        Consumer<StackShapedRecipeBuilder> ndrKeys = b -> b.define('r', carbonReceiver).define('s', carbonStock).define('t', titanium)
                .define('e', TGItems.RAD_EMITTER.get()).define('l', TGTags.Items.ingot("lead"));
        gun(output, ndr, 40, AmmoType.DEFAULT, "nuclear_deathray", TGAmmo.NUCLEAR_POWER_CELL.get(), ndrKeys, "ltl", "ers", " m ");
        gun(output, ndr, 0, AmmoType.DEFAULT, "nuclear_deathray_empty", TGAmmo.NUCLEAR_POWER_CELL_EMPTY.get(), ndrKeys,
                "ltl", "ers", " m ");

        Item tesla = TGGuns.TESLA_GUN.get();
        Consumer<StackShapedRecipeBuilder> teslaKeys = b -> b.define('r', carbonReceiver).define('c', TGItems.COIL.get()).define('s', carbonStock)
                .define('d', Items.REDSTONE).define('g', glassPane);
        gun(output, tesla, 25, AmmoType.DEFAULT, "tesla_gun", TGAmmo.ENERGY_CELL.get(), teslaKeys, " gd", "crs", " m ");
        gun(output, tesla, 0, AmmoType.DEFAULT, "tesla_gun_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), teslaKeys, " gd", "crs", " m ");

        Item mib = TGGuns.MIB_GUN.get();
        Consumer<StackShapedRecipeBuilder> mibKeys = b -> b.define('p', titanium).define('b', TGItems.LASER_BARREL.get())
                .define('s', TGItems.SONIC_EMITTER.get()).define('f', TGItems.MECHANICAL_PARTS_CARBON.get());
        gun(output, mib, 20, AmmoType.DEFAULT, "mib_gun", TGAmmo.ENERGY_CELL.get(), mibKeys, "bsp", " fp", "  m");
        gun(output, mib, 0, AmmoType.DEFAULT, "mib_gun_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), mibKeys, "bsp", " fp", "  m");

        // nether blaster: loaded only (the charge is used up); the alien blaster has no Fabric recipe
        gun(output, TGGuns.NETHER_BLASTER.get(), 10, AmmoType.DEFAULT, "nether_blaster", TGAmmo.NETHER_CHARGE.get(),
                b -> b.define('p', obsidianSteel).define('b', obsBarrel).define('c', TGItems.CYBERNETIC_PARTS.get())
                        .define('s', TGItems.PUMP_MECHANISM.get()),
                "cpc", "bms", "cpc");

        Item sonic = TGGuns.SONIC_SHOTGUN.get();
        Consumer<StackShapedRecipeBuilder> sonicKeys = b -> b.define('p', titanium).define('r', carbonReceiver).define('s', carbonStock)
                .define('e', TGItems.SONIC_EMITTER.get());
        gun(output, sonic, 8, AmmoType.DEFAULT, "sonic_shotgun", TGAmmo.ENERGY_CELL.get(), sonicKeys, " p ", "ers", " m ");
        gun(output, sonic, 0, AmmoType.DEFAULT, "sonic_shotgun_empty", TGAmmo.ENERGY_CELL_EMPTY.get(), sonicKeys, " p ", "ers", " m ");

        // --- Batch 3f: tools, crafted empty with any of their heads (head swaps: mining head upgrade recipe, batch 3g) ---
        Consumer<StackShapedRecipeBuilder> chainsawKeys = b -> b.define('p', plastic).define('r', ironReceiver).define('s', steelIngot);
        tool(output, TGGuns.CHAINSAW.get(), TGAmmo.FUEL_TANK_EMPTY.get(), chainsawKeys, "  p", "hsr", "  m");
        tool(output, TGGuns.MINING_DRILL.get(), TGAmmo.FUEL_TANK_EMPTY.get(), chainsawKeys, "  p", "hsr", "  m");
        tool(output, TGGuns.POWER_HAMMER.get(), TGAmmo.COMPRESSED_AIR_TANK_EMPTY.get(),
                b -> b.define('p', Items.PISTON).define('r', ironReceiver).define('i', TGItems.MECHANICAL_PARTS_IRON.get()),
                " i ", "hpr", " im");
        // shishkebap: Techguns 2 recipe (no Fabric port)
        gun(output, TGGuns.SHISHKEBAP.get(), 0, AmmoType.DEFAULT, "shishkebap_empty", TGAmmo.FUEL_TANK_EMPTY.get(),
                b -> b.define('p', TGTags.Items.plate("obsidian_steel")).define('f', TGItems.PUMP_MECHANISM.get()).define('s', plastic),
                "p ", "pf", "sm");
        // the grappling hook has no recipe in Fabric or Techguns 2

        // --- Batch 3g: guns made from guns keep their rounds; special recipes ---
        transfer(output, TGGuns.GOLDEN_REVOLVER.get(), "golden_revolver",
                b -> b.define('r', TGGuns.REVOLVER.get()).define('g', Tags.Items.INGOTS_GOLD), "ggg", "grg", "ggg");
        transfer(output, TGGuns.M4_INFILTRATOR.get(), "m4_infiltrator",
                b -> b.define('g', Tags.Items.GLASS_BLOCKS).define('p', steelIngot).define('w', ItemTags.WOOL)
                        .define('m', TGGuns.M4.get()).define('r', Items.REDSTONE).define('i', steelIngot),
                "gpg", "wwm", "rri");
        SpecialRecipeBuilder.special(AmmoChangeRecipe::new).save(output, TGIds.id("ammo_change"));
        SpecialRecipeBuilder.special(MiningHeadUpgradeRecipe::new).save(output, TGIds.id("mining_head_upgrade"));
    }

    /** A {@link techguns.recipe.TransferAmmoRecipe}: the result gun gets the rounds of the input guns. */
    private static void transfer(RecipeOutput output, Item gun, String id, Consumer<StackShapedRecipeBuilder> keys, String... pattern) {
        ItemStack result = new ItemStack(gun);
        GunData.setAmmo(result, new GunAmmo(0, AmmoType.DEFAULT));
        StackShapedRecipeBuilder builder = StackShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result, pattern).transferAmmo();
        keys.accept(builder);
        builder.unlockedBy(getHasName(gun), has(gun)).save(output, TGIds.id(id));
    }

    /** One empty tool recipe per mining head; the head ('h') sets the result's head level. */
    private static void tool(RecipeOutput output, Item item, ItemLike tank, Consumer<StackShapedRecipeBuilder> keys, String... pattern) {
        MeleeToolItem tool = (MeleeToolItem) item;
        for (int level = 0; level < tool.heads().size(); level++) {
            ItemStack result = new ItemStack(item);
            GunData.setAmmo(result, new GunAmmo(0, AmmoType.DEFAULT));
            tool.setHeadLevel(result, level);
            Item head = tool.heads().get(level).item().get();
            StackShapedRecipeBuilder builder = StackShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result, pattern);
            keys.accept(builder);
            builder.define('h', head).define('m', tank).unlockedBy(getHasName(head), has(head))
                    .save(output, TGIds.id(getItemName(item) + "_" + getItemName(head)));
        }
    }

    private static void miningHead(RecipeOutput output, ItemLike head, TagKey<Item> material, ItemLike parts, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, head);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define('i', material);
        if (parts != null) {
            builder.define('p', parts);
        }
        builder.unlockedBy("has_material", has(material)).save(output, TGIds.id(getItemName(head)));
    }

    /**
     * A gun recipe whose result carries the loaded-ammo component. {@code magazine} (may be null) is
     * defined as 'm' in the pattern.
     */
    private static void gun(RecipeOutput output, Item gun, int loaded, String variant, String id, ItemLike magazine,
                            Consumer<StackShapedRecipeBuilder> keys, String... pattern) {
        ItemStack result = new ItemStack(gun);
        GunData.setAmmo(result, new GunAmmo(loaded, variant));
        StackShapedRecipeBuilder builder = StackShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result, pattern);
        keys.accept(builder);
        if (magazine != null) {
            builder.define('m', magazine);
        }
        builder.unlockedBy(getHasName(gun), has(gun)).save(output, TGIds.id(id));
    }

    /** Two rows of three with an empty middle row. */
    private static void barrel(RecipeOutput output, ItemLike barrel, Ingredient material,
                               net.minecraft.advancements.Criterion<?> trigger) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, barrel)
                .pattern("sss").pattern("   ").pattern("sss")
                .define('s', material)
                .unlockedBy("has_material", trigger).save(output, TGIds.id(getItemName(barrel)));
    }

    /** 9 nuggets to an ingot and back, ids {@code techguns:<material>_ingot_from_nuggets} / {@code _nugget_from_ingot}. */
    private static void nuggetsAndIngot(RecipeOutput output, ItemLike ingot, ItemLike nugget, TagKey<Item> nuggetTag, String material) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot).requires(Ingredient.of(nuggetTag), 9)
                .unlockedBy(getHasName(nugget), has(nugget)).save(output, TGIds.id(material + "_ingot_from_nuggets"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9).requires(ingot)
                .unlockedBy(getHasName(ingot), has(ingot)).save(output, TGIds.id(material + "_nugget_from_ingot"));
    }

    /** Blast furnace only recipes, ids {@code techguns:<result>_from_blasting_<input>}. */
    private static void blastingOnly(RecipeOutput output, List<ItemLike> inputs, ItemLike result, float experience, int time, String group) {
        for (ItemLike input : inputs) {
            SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, result, experience, time)
                    .group(group).unlockedBy(getHasName(input), has(input))
                    .save(output, TGIds.id(getItemName(result) + "_from_blasting_" + getItemName(input)));
        }
    }

    /**
     * Furnace (200 ticks) and blast furnace (100 ticks) recipes for each input, with ids
     * {@code techguns:<result>_from_smelting_<input>} / {@code _from_blasting_}.
     */
    private static void smeltingAndBlasting(RecipeOutput output, List<ItemLike> inputs, ItemLike result, float experience, String group) {
        for (ItemLike input : inputs) {
            String suffix = getItemName(result) + "_from_%s_" + getItemName(input);
            SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, result, experience, 200)
                    .group(group).unlockedBy(getHasName(input), has(input))
                    .save(output, TGIds.id(suffix.formatted("smelting")));
            SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, result, experience, 100)
                    .group(group).unlockedBy(getHasName(input), has(input))
                    .save(output, TGIds.id(suffix.formatted("blasting")));
        }
    }
}
