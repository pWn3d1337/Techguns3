package techguns.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import techguns.registry.TGRecipes;

/**
 * Metal Press recipe (Techguns 2 {@code MetalPressRecipes}): one item from each input slot makes the result; with
 * {@code allow_swap} the inputs may be in either slot.
 */
public record MetalPressRecipe(Ingredient first, Ingredient second, boolean allowSwap, ItemStack result)
        implements Recipe<TwoSlotInput> {

    @Override
    public boolean matches(TwoSlotInput input, Level level) {
        return matches(input.first(), input.second());
    }

    public boolean matches(ItemStack a, ItemStack b) {
        return (first.test(a) && second.test(b)) || (allowSwap && first.test(b) && second.test(a));
    }

    /** Whether {@code stack} is one of the inputs (Techguns 2 {@code isItemPartOfRecipe}). */
    public boolean uses(ItemStack stack) {
        return first.test(stack) || second.test(stack);
    }

    @Override
    public ItemStack assemble(TwoSlotInput input, HolderLookup.Provider registries) {
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
        return TGRecipes.METAL_PRESS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.METAL_PRESS.get();
    }

    public static class Serializer implements RecipeSerializer<MetalPressRecipe> {
        private static final MapCodec<MetalPressRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("first").forGetter(MetalPressRecipe::first),
                Ingredient.CODEC_NONEMPTY.fieldOf("second").forGetter(MetalPressRecipe::second),
                Codec.BOOL.optionalFieldOf("allow_swap", true).forGetter(MetalPressRecipe::allowSwap),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(MetalPressRecipe::result)
        ).apply(instance, MetalPressRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MetalPressRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, MetalPressRecipe::first,
                Ingredient.CONTENTS_STREAM_CODEC, MetalPressRecipe::second,
                ByteBufCodecs.BOOL, MetalPressRecipe::allowSwap,
                ItemStack.STREAM_CODEC, MetalPressRecipe::result,
                MetalPressRecipe::new);

        @Override
        public MapCodec<MetalPressRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MetalPressRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
