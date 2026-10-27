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
 * Charging Station recipe (Techguns 2 {@code ChargingStationRecipe}): an empty item becomes the charged one after
 * {@code charge} FE.
 */
public record ChargingRecipe(Ingredient input, ItemStack result, int charge) implements MachineRecipe {

    @Override
    public boolean matches(MachineInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.CHARGING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.CHARGING.get();
    }

    public static class Serializer implements RecipeSerializer<ChargingRecipe> {
        private static final MapCodec<ChargingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(ChargingRecipe::input),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ChargingRecipe::result),
                Codec.INT.fieldOf("charge").forGetter(ChargingRecipe::charge)
        ).apply(instance, ChargingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ChargingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, ChargingRecipe::input,
                ItemStack.STREAM_CODEC, ChargingRecipe::result,
                ByteBufCodecs.VAR_INT, ChargingRecipe::charge,
                ChargingRecipe::new);

        @Override
        public MapCodec<ChargingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChargingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
