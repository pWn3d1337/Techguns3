package techguns.recipe;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import techguns.registry.TGRecipes;

/**
 * What an ore cluster block gives the Ore Drill (Techguns 2 {@code TGOreClusters.OreCluster}): the mining level the
 * drill needs, the ore and power multipliers, and weighted outputs, either the first item of an ingredient (tags let
 * other mods' ores join) or a bucket of the first fluid in a fluid tag. Entries with nothing to give are skipped.
 */
public record OreClusterRecipe(Block cluster, int miningLevel, float oreMultiplier, float powerMultiplier, List<Entry> entries)
        implements MachineRecipe {

    /** One weighted output: an item ingredient or a fluid tag. */
    public record Entry(Optional<Ingredient> item, Optional<TagKey<Fluid>> fluid, int weight) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Ingredient.CODEC_NONEMPTY.optionalFieldOf("item").forGetter(Entry::item),
                TagKey.codec(Registries.FLUID).optionalFieldOf("fluid").forGetter(Entry::fluid),
                Codec.intRange(1, 10000).fieldOf("weight").forGetter(Entry::weight)
        ).apply(i, Entry::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), Entry::item,
                ByteBufCodecs.optional(StreamCodec.of((buf, tag) -> buf.writeResourceLocation(tag.location()),
                        buf -> TagKey.create(Registries.FLUID, buf.readResourceLocation()))), Entry::fluid,
                ByteBufCodecs.VAR_INT, Entry::weight, Entry::new);

        public static Entry item(Ingredient item, int weight) {
            return new Entry(Optional.of(item), Optional.empty(), weight);
        }

        public static Entry fluid(TagKey<Fluid> fluid, int weight) {
            return new Entry(Optional.empty(), Optional.of(fluid), weight);
        }

        public ItemStack itemOutput() {
            // an empty tag (other mod's ore missing) shows a barrier placeholder: it gives nothing
            if (item.isEmpty() || item.get().hasNoItems()) {
                return ItemStack.EMPTY;
            }
            ItemStack[] items = item.get().getItems();
            return items.length == 0 ? ItemStack.EMPTY : items[0].copyWithCount(1);
        }

        /** A bucket of the first fluid in the tag, or empty. */
        public FluidStack fluidOutput() {
            return fluid.flatMap(tag -> BuiltInRegistries.FLUID.getTag(tag)).flatMap(set -> set.stream().findFirst())
                    .map(holder -> new FluidStack(holder.value(), 1000)).orElse(FluidStack.EMPTY);
        }

        public boolean isEmpty() {
            return itemOutput().isEmpty() && fluidOutput().isEmpty();
        }
    }

    /** Techguns 2 {@code WeightedRandom.getRandomItem} over the entries that give something; null if none. */
    public Entry pick(RandomSource random) {
        List<Entry> valid = entries.stream().filter(e -> !e.isEmpty()).toList();
        int total = valid.stream().mapToInt(Entry::weight).sum();
        if (total <= 0) {
            return null;
        }
        int roll = random.nextInt(total);
        for (Entry entry : valid) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry;
            }
        }
        return valid.getLast();
    }

    @Override
    public boolean matches(MachineInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.ORE_CLUSTER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.ORE_CLUSTER.get();
    }

    public static class Serializer implements RecipeSerializer<OreClusterRecipe> {
        private static final MapCodec<OreClusterRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("cluster").forGetter(OreClusterRecipe::cluster),
                Codec.intRange(0, 10).fieldOf("mining_level").forGetter(OreClusterRecipe::miningLevel),
                Codec.floatRange(0.0f, 1000.0f).fieldOf("ore_multiplier").forGetter(OreClusterRecipe::oreMultiplier),
                Codec.floatRange(0.0f, 1000.0f).fieldOf("power_multiplier").forGetter(OreClusterRecipe::powerMultiplier),
                Entry.CODEC.listOf().fieldOf("entries").forGetter(OreClusterRecipe::entries)
        ).apply(i, OreClusterRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, OreClusterRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.BLOCK), OreClusterRecipe::cluster,
                ByteBufCodecs.VAR_INT, OreClusterRecipe::miningLevel,
                ByteBufCodecs.FLOAT, OreClusterRecipe::oreMultiplier,
                ByteBufCodecs.FLOAT, OreClusterRecipe::powerMultiplier,
                Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), OreClusterRecipe::entries,
                OreClusterRecipe::new);

        @Override
        public MapCodec<OreClusterRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OreClusterRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
