package techguns.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import techguns.registry.TGRecipes;

/**
 * Blast Furnace recipe (Techguns 2 {@code BlastFurnaceRecipes}): counted inputs in fixed slots, with its own power per
 * tick and duration.
 */
public record BlastFurnaceRecipe(SizedIngredient first, SizedIngredient second, ItemStack result, int powerPerTick, int time)
        implements MachineRecipe {

    @Override
    public boolean matches(MachineInput input, Level level) {
        return first.test(input.getItem(0)) && second.test(input.getItem(1));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.BLAST_FURNACE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.BLAST_FURNACE.get();
    }

    public static class Serializer implements RecipeSerializer<BlastFurnaceRecipe> {
        private static final MapCodec<BlastFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SizedIngredient.FLAT_CODEC.fieldOf("first").forGetter(BlastFurnaceRecipe::first),
                SizedIngredient.FLAT_CODEC.fieldOf("second").forGetter(BlastFurnaceRecipe::second),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(BlastFurnaceRecipe::result),
                Codec.INT.fieldOf("power_per_tick").forGetter(BlastFurnaceRecipe::powerPerTick),
                Codec.INT.fieldOf("time").forGetter(BlastFurnaceRecipe::time)
        ).apply(instance, BlastFurnaceRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, BlastFurnaceRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedIngredient.STREAM_CODEC, BlastFurnaceRecipe::first,
                SizedIngredient.STREAM_CODEC, BlastFurnaceRecipe::second,
                ItemStack.STREAM_CODEC, BlastFurnaceRecipe::result,
                ByteBufCodecs.VAR_INT, BlastFurnaceRecipe::powerPerTick,
                ByteBufCodecs.VAR_INT, BlastFurnaceRecipe::time,
                BlastFurnaceRecipe::new);

        @Override
        public MapCodec<BlastFurnaceRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BlastFurnaceRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
