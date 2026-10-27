package techguns.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import techguns.registry.TGRecipes;

/**
 * Ammo Bench recipe (Fabric {@code AmmoBenchRecipe}, {@code ammobench_crafting}): one bullet core, one casing and one
 * powder (slots 0, 1, 2) make {@code result}; every recipe with matching inputs is a choice in the bench's list.
 */
public record AmmoBenchRecipe(Ingredient core, Ingredient casing, Ingredient powder, ItemStack result) implements MachineRecipe {
    public static final int SLOT_CORE = 0;
    public static final int SLOT_CASING = 1;
    public static final int SLOT_POWDER = 2;

    @Override
    public boolean matches(MachineInput input, Level level) {
        return core.test(input.getItem(SLOT_CORE)) && casing.test(input.getItem(SLOT_CASING)) && powder.test(input.getItem(SLOT_POWDER));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.AMMO_BENCH_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.AMMO_BENCH.get();
    }

    public static class Serializer implements RecipeSerializer<AmmoBenchRecipe> {
        private static final MapCodec<AmmoBenchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("core").forGetter(AmmoBenchRecipe::core),
                Ingredient.CODEC_NONEMPTY.fieldOf("casing").forGetter(AmmoBenchRecipe::casing),
                Ingredient.CODEC_NONEMPTY.fieldOf("powder").forGetter(AmmoBenchRecipe::powder),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(AmmoBenchRecipe::result)
        ).apply(instance, AmmoBenchRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, AmmoBenchRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, AmmoBenchRecipe::core,
                Ingredient.CONTENTS_STREAM_CODEC, AmmoBenchRecipe::casing,
                Ingredient.CONTENTS_STREAM_CODEC, AmmoBenchRecipe::powder,
                ItemStack.STREAM_CODEC, AmmoBenchRecipe::result,
                AmmoBenchRecipe::new);

        @Override
        public MapCodec<AmmoBenchRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AmmoBenchRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
