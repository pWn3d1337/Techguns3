package techguns.recipe;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import techguns.armor.TGArmorItem;
import techguns.registry.TGRecipes;

/**
 * Upgrade Bench recipe (Techguns 2 {@code UpgradeBenchRecipes}): a Techguns armor piece (slot 0) plus an upgrade item
 * (slot 1) gets {@code enchantment} at {@code level}, if the enchantment fits the piece and it has a lower level.
 */
public record UpgradeBenchRecipe(Ingredient upgrade, ResourceKey<Enchantment> enchantment, int level) implements MachineRecipe {
    public static final int SLOT_ARMOR = 0;
    public static final int SLOT_UPGRADE = 1;

    @Override
    public boolean matches(MachineInput input, Level level) {
        return upgrade.test(input.getItem(SLOT_UPGRADE)) && enchantment(level.registryAccess())
                .filter(holder -> canUpgrade(input.getItem(SLOT_ARMOR), holder)).isPresent();
    }

    public Optional<Holder.Reference<Enchantment>> enchantment(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.ENCHANTMENT).get(enchantment);
    }

    /** Techguns 2: only Techguns armor the enchantment can go on, and only to a higher level. */
    private boolean canUpgrade(ItemStack armor, Holder<Enchantment> holder) {
        return armor.getItem() instanceof TGArmorItem && holder.value().canEnchant(armor)
                && EnchantmentHelper.getTagEnchantmentLevel(holder, armor) < level;
    }

    /** Levels gained, for the XP cost (Techguns 2 {@code calculateXPCost}). */
    public int levelsGained(ItemStack armor, HolderLookup.Provider registries) {
        return enchantment(registries).map(holder -> Math.max(0, level - EnchantmentHelper.getTagEnchantmentLevel(holder, armor))).orElse(0);
    }

    /** The upgraded copy of the armor in {@code input}; other enchantments stay (Techguns 2 ignored exclusivity). */
    @Override
    public ItemStack assemble(MachineInput input, HolderLookup.Provider registries) {
        ItemStack result = input.getItem(SLOT_ARMOR).copyWithCount(1);
        enchantment(registries).ifPresent(holder -> EnchantmentHelper.updateEnchantments(result, enchantments -> enchantments.set(holder, level)));
        return result;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.UPGRADE_BENCH_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TGRecipes.UPGRADE_BENCH.get();
    }

    public static class Serializer implements RecipeSerializer<UpgradeBenchRecipe> {
        private static final MapCodec<UpgradeBenchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("upgrade").forGetter(UpgradeBenchRecipe::upgrade),
                ResourceKey.codec(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(UpgradeBenchRecipe::enchantment),
                Codec.intRange(1, 255).fieldOf("level").forGetter(UpgradeBenchRecipe::level)
        ).apply(instance, UpgradeBenchRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, UpgradeBenchRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, UpgradeBenchRecipe::upgrade,
                ResourceKey.streamCodec(Registries.ENCHANTMENT), UpgradeBenchRecipe::enchantment,
                ByteBufCodecs.VAR_INT, UpgradeBenchRecipe::level,
                UpgradeBenchRecipe::new);

        @Override
        public MapCodec<UpgradeBenchRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, UpgradeBenchRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
