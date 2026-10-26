package techguns.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.recipe.AmmoChangeRecipe;
import techguns.recipe.MiningHeadUpgradeRecipe;
import techguns.recipe.TransferAmmoRecipe;

/**
 * Crafting recipe serializers. Machine recipe types (Ammo Bench, Camo Bench) come with their machines in Phase 5.
 */
public final class TGRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Techguns.MODID);

    public static final Supplier<RecipeSerializer<AmmoChangeRecipe>> AMMO_CHANGE = SERIALIZERS.register("ammo_change",
            () -> new SimpleCraftingRecipeSerializer<>(AmmoChangeRecipe::new));
    public static final Supplier<RecipeSerializer<MiningHeadUpgradeRecipe>> MINING_HEAD_UPGRADE = SERIALIZERS.register("mining_head_upgrade",
            () -> new SimpleCraftingRecipeSerializer<>(MiningHeadUpgradeRecipe::new));
    public static final Supplier<RecipeSerializer<TransferAmmoRecipe>> TRANSFER_AMMO = SERIALIZERS.register("transfer_ammo",
            TransferAmmoRecipe.Serializer::new);

    private TGRecipes() {
    }
}
