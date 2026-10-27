package techguns.recipe;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import techguns.registry.TGRecipes;

/**
 * Grinder recipe (Techguns 2 {@code GrinderRecipes}): grinds one input item into several outputs; an output with a
 * chance gives {@code count x chance} items on average (Techguns 2 {@code MachineOperationChance}).
 */
public record GrinderRecipe(Ingredient input, List<Output> outputs) implements MachineRecipe {

    /** One output; {@code chance} 1 is fixed, above 1 adds extra items. */
    public record Output(ItemStack stack, float chance) {
        public static final Codec<Output> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStack.STRICT_CODEC.fieldOf("item").forGetter(Output::stack),
                Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(Output::chance)
        ).apply(i, Output::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Output> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, Output::stack, ByteBufCodecs.FLOAT, Output::chance, Output::new);

        public static Output of(ItemStack stack) {
            return new Output(stack, 1.0f);
        }
    }

    @Override
    public boolean matches(MachineInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    /** Rolls the outputs for {@code multiplier} items at once: whole items are fixed, the remainder is a chance. */
    public List<ItemStack> roll(int multiplier, RandomSource random) {
        List<ItemStack> rolled = new ArrayList<>();
        for (Output output : outputs) {
            double amount = output.stack().getCount() * (double) output.chance() * multiplier;
            int count = (int) Math.floor(amount);
            if (random.nextDouble() < amount - count) {
                count++;
            }
            if (count > 0) {
                rolled.add(output.stack().copyWithCount(count));
            }
        }
        return rolled;
    }

    /** The most each output can give, for the space check. */
    public List<ItemStack> maxOutputs(int multiplier) {
        return outputs.stream()
                .map(o -> o.stack().copyWithCount((int) Math.ceil(o.stack().getCount() * (double) o.chance() * multiplier)))
                .filter(s -> !s.isEmpty()).toList();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.getFirst().stack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.GRINDER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.GRINDER.get();
    }

    public static class Serializer implements RecipeSerializer<GrinderRecipe> {
        private static final MapCodec<GrinderRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(GrinderRecipe::input),
                Output.CODEC.listOf(1, 9).fieldOf("outputs").forGetter(GrinderRecipe::outputs)
        ).apply(instance, GrinderRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, GrinderRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, GrinderRecipe::input,
                Output.STREAM_CODEC.apply(ByteBufCodecs.list()), GrinderRecipe::outputs,
                GrinderRecipe::new);

        @Override
        public MapCodec<GrinderRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GrinderRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
