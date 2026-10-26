package techguns.recipe;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import techguns.registry.TGRecipes;

/**
 * Chem Lab recipe (Techguns 2 {@code ChemLabRecipes}): up to two counted items, a counted flask item and an input fluid
 * make an item and/or a fluid. An absent input requires its slot to be empty; with {@code allow_swap} the two item
 * inputs may be in either slot.
 */
public record ChemLabRecipe(Optional<SizedIngredient> first, Optional<SizedIngredient> second, Optional<SizedIngredient> flask,
                            Optional<SizedFluidIngredient> fluid, ItemStack result, FluidStack fluidResult, boolean allowSwap,
                            int powerPerTick) implements Recipe<ChemLabRecipe.Input> {

    /** The Chem Lab contents a recipe is matched against. */
    public record Input(ItemStack first, ItemStack second, ItemStack flask, FluidStack fluid) implements net.minecraft.world.item.crafting.RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return switch (index) {
                case 0 -> first;
                case 1 -> second;
                case 2 -> flask;
                default -> throw new IllegalArgumentException("No item for index " + index);
            };
        }

        @Override
        public int size() {
            return 3;
        }
    }

    /** How the item inputs matched: {@code SWAPPED} means the first ingredient is in the second slot. */
    public enum Match {
        NONE, NORMAL, SWAPPED
    }

    public Match match(Input input) {
        if (!fits(flask, input.flask()) || !fits(fluid, input.fluid())) {
            return Match.NONE;
        }
        if (fits(first, input.first()) && fits(second, input.second())) {
            return Match.NORMAL;
        }
        if (allowSwap && fits(first, input.second()) && fits(second, input.first())) {
            return Match.SWAPPED;
        }
        return Match.NONE;
    }

    private static boolean fits(Optional<SizedIngredient> ingredient, ItemStack stack) {
        return ingredient.map(i -> i.test(stack)).orElse(stack.isEmpty());
    }

    private static boolean fits(Optional<SizedFluidIngredient> ingredient, FluidStack stack) {
        return ingredient.map(i -> i.test(stack)).orElse(true);
    }

    /** Count of an item input, 0 if absent. */
    public static int count(Optional<SizedIngredient> ingredient) {
        return ingredient.map(SizedIngredient::count).orElse(0);
    }

    /** Whether {@code stack} is one of the two item inputs (Techguns 2 {@code isItemPartOfRecipe} for the item slots). */
    public boolean usesItem(ItemStack stack) {
        return first.map(i -> i.ingredient().test(stack)).orElse(false) || second.map(i -> i.ingredient().test(stack)).orElse(false);
    }

    /** Whether the two items may go into the item slots together (Techguns 2 {@code isValidInputSlot1_2}). */
    public boolean acceptsPair(ItemStack a, ItemStack b) {
        return (accepts(first, a) && accepts(second, b)) || (allowSwap && accepts(first, b) && accepts(second, a));
    }

    private static boolean accepts(Optional<SizedIngredient> ingredient, ItemStack stack) {
        return ingredient.map(i -> i.ingredient().test(stack)).orElse(stack.isEmpty());
    }

    @Override
    public boolean matches(Input input, Level level) {
        return match(input) != Match.NONE;
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.CHEM_LAB_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.CHEM_LAB.get();
    }

    public static class Serializer implements RecipeSerializer<ChemLabRecipe> {
        private static final MapCodec<ChemLabRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SizedIngredient.FLAT_CODEC.optionalFieldOf("first").forGetter(ChemLabRecipe::first),
                SizedIngredient.FLAT_CODEC.optionalFieldOf("second").forGetter(ChemLabRecipe::second),
                SizedIngredient.FLAT_CODEC.optionalFieldOf("flask").forGetter(ChemLabRecipe::flask),
                SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("fluid").forGetter(ChemLabRecipe::fluid),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(ChemLabRecipe::result),
                FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid_result", FluidStack.EMPTY).forGetter(ChemLabRecipe::fluidResult),
                Codec.BOOL.optionalFieldOf("allow_swap", false).forGetter(ChemLabRecipe::allowSwap),
                Codec.INT.fieldOf("power_per_tick").forGetter(ChemLabRecipe::powerPerTick)
        ).apply(instance, ChemLabRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, Optional<SizedIngredient>> OPTIONAL_INGREDIENT =
                ByteBufCodecs.optional(SizedIngredient.STREAM_CODEC);

        private static final StreamCodec<RegistryFriendlyByteBuf, ChemLabRecipe> STREAM_CODEC = StreamCodec.of((buf, recipe) -> {
            OPTIONAL_INGREDIENT.encode(buf, recipe.first());
            OPTIONAL_INGREDIENT.encode(buf, recipe.second());
            OPTIONAL_INGREDIENT.encode(buf, recipe.flask());
            ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC).encode(buf, recipe.fluid());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.result());
            FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.fluidResult());
            buf.writeBoolean(recipe.allowSwap());
            buf.writeVarInt(recipe.powerPerTick());
        }, buf -> new ChemLabRecipe(OPTIONAL_INGREDIENT.decode(buf), OPTIONAL_INGREDIENT.decode(buf), OPTIONAL_INGREDIENT.decode(buf),
                ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC).decode(buf), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                FluidStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readBoolean(), buf.readVarInt()));

        @Override
        public MapCodec<ChemLabRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChemLabRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
