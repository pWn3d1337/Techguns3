package techguns.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.TGIds;
import techguns.recipe.AmmoChangeRecipe;
import techguns.recipe.AmmoPressRecipe;
import techguns.recipe.BlastFurnaceRecipe;
import techguns.recipe.GrinderRecipe;
import techguns.recipe.MetalPressRecipe;
import techguns.recipe.MiningHeadUpgradeRecipe;
import techguns.recipe.TransferAmmoRecipe;

/**
 * Recipe serializers and the machine recipe types.
 */
public final class TGRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Techguns.MODID);

    public static final Supplier<RecipeSerializer<AmmoChangeRecipe>> AMMO_CHANGE = SERIALIZERS.register("ammo_change",
            () -> new SimpleCraftingRecipeSerializer<>(AmmoChangeRecipe::new));
    public static final Supplier<RecipeSerializer<MiningHeadUpgradeRecipe>> MINING_HEAD_UPGRADE = SERIALIZERS.register("mining_head_upgrade",
            () -> new SimpleCraftingRecipeSerializer<>(MiningHeadUpgradeRecipe::new));
    public static final Supplier<RecipeSerializer<TransferAmmoRecipe>> TRANSFER_AMMO = SERIALIZERS.register("transfer_ammo",
            TransferAmmoRecipe.Serializer::new);

    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Techguns.MODID);

    // --- Machines ---
    public static final Supplier<RecipeType<MetalPressRecipe>> METAL_PRESS = type("metal_press");
    public static final Supplier<RecipeSerializer<MetalPressRecipe>> METAL_PRESS_SERIALIZER = SERIALIZERS.register("metal_press",
            MetalPressRecipe.Serializer::new);
    public static final Supplier<RecipeType<AmmoPressRecipe>> AMMO_PRESS = type("ammo_press");
    public static final Supplier<RecipeSerializer<AmmoPressRecipe>> AMMO_PRESS_SERIALIZER = SERIALIZERS.register("ammo_press",
            AmmoPressRecipe.Serializer::new);
    public static final Supplier<RecipeType<BlastFurnaceRecipe>> BLAST_FURNACE = type("blast_furnace");
    public static final Supplier<RecipeSerializer<BlastFurnaceRecipe>> BLAST_FURNACE_SERIALIZER = SERIALIZERS.register("blast_furnace",
            BlastFurnaceRecipe.Serializer::new);
    public static final Supplier<RecipeType<GrinderRecipe>> GRINDER = type("grinder");
    public static final Supplier<RecipeSerializer<GrinderRecipe>> GRINDER_SERIALIZER = SERIALIZERS.register("grinder",
            GrinderRecipe.Serializer::new);
    public static final Supplier<RecipeType<techguns.recipe.ChemLabRecipe>> CHEM_LAB = type("chem_lab");
    public static final Supplier<RecipeSerializer<techguns.recipe.ChemLabRecipe>> CHEM_LAB_SERIALIZER = SERIALIZERS.register("chem_lab",
            techguns.recipe.ChemLabRecipe.Serializer::new);
    public static final Supplier<RecipeType<techguns.recipe.ChargingRecipe>> CHARGING = type("charging");
    public static final Supplier<RecipeSerializer<techguns.recipe.ChargingRecipe>> CHARGING_SERIALIZER = SERIALIZERS.register("charging",
            techguns.recipe.ChargingRecipe.Serializer::new);

    // --- Multiblocks ---
    public static final Supplier<RecipeType<techguns.recipe.FabricatorRecipe>> FABRICATOR = type("fabricator");
    public static final Supplier<RecipeSerializer<techguns.recipe.FabricatorRecipe>> FABRICATOR_SERIALIZER = SERIALIZERS.register("fabricator",
            techguns.recipe.FabricatorRecipe.Serializer::new);

    public static final Supplier<RecipeType<techguns.recipe.ReactionChamberRecipe>> REACTION_CHAMBER = type("reaction_chamber");
    public static final Supplier<RecipeSerializer<techguns.recipe.ReactionChamberRecipe>> REACTION_CHAMBER_SERIALIZER = SERIALIZERS.register(
            "reaction_chamber", techguns.recipe.ReactionChamberRecipe.Serializer::new);

    // --- Benches ---
    public static final Supplier<RecipeType<techguns.recipe.UpgradeBenchRecipe>> UPGRADE_BENCH = type("upgrade_bench");
    public static final Supplier<RecipeSerializer<techguns.recipe.UpgradeBenchRecipe>> UPGRADE_BENCH_SERIALIZER = SERIALIZERS.register("upgrade_bench",
            techguns.recipe.UpgradeBenchRecipe.Serializer::new);
    public static final Supplier<RecipeType<techguns.recipe.CamoChangeRecipe>> CAMO_CHANGE = type("camo_change");
    public static final Supplier<RecipeSerializer<techguns.recipe.CamoChangeRecipe>> CAMO_CHANGE_SERIALIZER = SERIALIZERS.register("camo_change",
            techguns.recipe.CamoChangeRecipe.Serializer::new);
    public static final Supplier<RecipeType<techguns.recipe.AmmoBenchRecipe>> AMMO_BENCH = type("ammo_bench");
    public static final Supplier<RecipeSerializer<techguns.recipe.AmmoBenchRecipe>> AMMO_BENCH_SERIALIZER = SERIALIZERS.register("ammo_bench",
            techguns.recipe.AmmoBenchRecipe.Serializer::new);

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> Supplier<RecipeType<T>> type(String name) {
        return TYPES.register(name, () -> RecipeType.simple(TGIds.id(name)));
    }

    private TGRecipes() {
    }
}
