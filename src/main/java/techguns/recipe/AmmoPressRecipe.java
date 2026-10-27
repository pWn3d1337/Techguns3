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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import techguns.registry.TGRecipes;

/**
 * Ammo Press build plan (Techguns 2 {@code AmmoPressBuildPlans}): one bullet metal, two casing metal and one powder make
 * the result of the selected plan. Plans are listed by {@code order}.
 */
public record AmmoPressRecipe(int order, Ingredient bulletMetal, Ingredient casingMetal, Ingredient powder, ItemStack result)
        implements MachineRecipe {
    public static final int CASING_METAL_COUNT = 2;

    @Override
    public boolean matches(MachineInput input, Level level) {
        return bulletMetal.test(input.getItem(0)) && casingMetal.test(input.getItem(1)) && input.getItem(1).getCount() >= CASING_METAL_COUNT
                && powder.test(input.getItem(2));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.AMMO_PRESS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.AMMO_PRESS.get();
    }

    public static class Serializer implements RecipeSerializer<AmmoPressRecipe> {
        private static final MapCodec<AmmoPressRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("order").forGetter(AmmoPressRecipe::order),
                Ingredient.CODEC_NONEMPTY.fieldOf("bullet_metal").forGetter(AmmoPressRecipe::bulletMetal),
                Ingredient.CODEC_NONEMPTY.fieldOf("casing_metal").forGetter(AmmoPressRecipe::casingMetal),
                Ingredient.CODEC_NONEMPTY.fieldOf("powder").forGetter(AmmoPressRecipe::powder),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(AmmoPressRecipe::result)
        ).apply(instance, AmmoPressRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, AmmoPressRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, AmmoPressRecipe::order,
                Ingredient.CONTENTS_STREAM_CODEC, AmmoPressRecipe::bulletMetal,
                Ingredient.CONTENTS_STREAM_CODEC, AmmoPressRecipe::casingMetal,
                Ingredient.CONTENTS_STREAM_CODEC, AmmoPressRecipe::powder,
                ItemStack.STREAM_CODEC, AmmoPressRecipe::result,
                AmmoPressRecipe::new);

        @Override
        public MapCodec<AmmoPressRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AmmoPressRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
