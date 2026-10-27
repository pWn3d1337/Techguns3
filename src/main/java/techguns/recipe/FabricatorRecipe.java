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
 * Fabricator recipe (Techguns 2 {@code FabricatorRecipe}): input, wires, powder and plate slots (0..3), each an
 * ingredient with a count, make {@code result}. Which items go into the wire, powder and plate slots is set by the
 * {@code techguns:fabricator_wires/powders/plates} item tags.
 */
public record FabricatorRecipe(Input input, Input wires, Input powder, Input plate, ItemStack result) implements MachineRecipe {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_WIRES = 1;
    public static final int SLOT_POWDER = 2;
    public static final int SLOT_PLATE = 3;

    /** One slot of the recipe: {@code count} items matching {@code ingredient}. */
    public record Input(Ingredient ingredient, int count) {
        public static final Codec<Input> CODEC = RecordCodecBuilder.create(i -> i.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(Input::ingredient),
                Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Input::count)
        ).apply(i, Input::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Input> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, Input::ingredient, ByteBufCodecs.VAR_INT, Input::count, Input::new);

        public boolean test(ItemStack stack) {
            return ingredient.test(stack) && stack.getCount() >= count;
        }

        /** How many times the slot's stack covers this input. */
        public int times(ItemStack stack) {
            return stack.getCount() / count;
        }
    }

    public Input slot(int slot) {
        return switch (slot) {
            case SLOT_WIRES -> wires;
            case SLOT_POWDER -> powder;
            case SLOT_PLATE -> plate;
            default -> input;
        };
    }

    @Override
    public boolean matches(MachineInput machineInput, Level level) {
        for (int slot = SLOT_INPUT; slot <= SLOT_PLATE; slot++) {
            if (!slot(slot).test(machineInput.getItem(slot))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.FABRICATOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.FABRICATOR.get();
    }

    public static class Serializer implements RecipeSerializer<FabricatorRecipe> {
        private static final MapCodec<FabricatorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Input.CODEC.fieldOf("input").forGetter(FabricatorRecipe::input),
                Input.CODEC.fieldOf("wires").forGetter(FabricatorRecipe::wires),
                Input.CODEC.fieldOf("powder").forGetter(FabricatorRecipe::powder),
                Input.CODEC.fieldOf("plate").forGetter(FabricatorRecipe::plate),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(FabricatorRecipe::result)
        ).apply(instance, FabricatorRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> STREAM_CODEC = StreamCodec.composite(
                Input.STREAM_CODEC, FabricatorRecipe::input,
                Input.STREAM_CODEC, FabricatorRecipe::wires,
                Input.STREAM_CODEC, FabricatorRecipe::powder,
                Input.STREAM_CODEC, FabricatorRecipe::plate,
                ItemStack.STREAM_CODEC, FabricatorRecipe::result,
                FabricatorRecipe::new);

        @Override
        public MapCodec<FabricatorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
