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

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> Supplier<RecipeType<T>> type(String name) {
        return TYPES.register(name, () -> RecipeType.simple(TGIds.id(name)));
    }

    private TGRecipes() {
    }
}
