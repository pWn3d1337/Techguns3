package techguns.recipe;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import techguns.registry.TGRecipes;

/**
 * Camo Bench group (Fabric {@code CamoChangeRecipe}, Techguns 2 {@code CamoBenchRecipe}): blocks of one kind in
 * different looks; the Camo Bench turns any of them into another of the group. Items with named camos
 * ({@code techguns:camo}) need no recipe.
 */
public record CamoChangeRecipe(List<Item> entries) implements MachineRecipe {
    @Override
    public boolean matches(MachineInput input, Level level) {
        return entries.contains(input.getItem(0).getItem());
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(entries.getFirst());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.CAMO_CHANGE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.CAMO_CHANGE.get();
    }

    public static class Serializer implements RecipeSerializer<CamoChangeRecipe> {
        private static final MapCodec<CamoChangeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().listOf(2, 64).fieldOf("entries").forGetter(CamoChangeRecipe::entries)
        ).apply(instance, CamoChangeRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CamoChangeRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list()), CamoChangeRecipe::entries,
                CamoChangeRecipe::new);

        @Override
        public MapCodec<CamoChangeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CamoChangeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
