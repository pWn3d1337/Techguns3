package techguns.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.GunItem;
import techguns.gun.ammo.AmmoType;
import techguns.registry.TGRecipes;

/**
 * Shaped recipe that turns guns into another gun and keeps their rounds (Fabric {@code TransferAmmoRecipe}): golden
 * revolver, Infiltrator, Grim Reaper. The input guns must share one ammo variant; their rounds are summed up to the
 * result's clip size.
 */
public class TransferAmmoRecipe extends ShapedRecipe {
    /** Kept for the codec; vanilla's field is not accessible. */
    private final ShapedRecipePattern shapedPattern;

    public TransferAmmoRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result,
                              boolean showNotification) {
        super(group, category, pattern, result, showNotification);
        this.shapedPattern = pattern;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return super.matches(input, level) && inputVariant(input) != null;
    }

    /** The one ammo variant of all input guns ({@link AmmoType#DEFAULT} without guns), or null if they differ. */
    private static String inputVariant(CraftingInput input) {
        String variant = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof GunItem) {
                String gunVariant = GunData.variant(stack);
                if (variant != null && !variant.equals(gunVariant)) {
                    return null;
                }
                variant = gunVariant;
            }
        }
        return variant == null ? AmmoType.DEFAULT : variant;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        if (!(result.getItem() instanceof GunItem gun)) {
            return result;
        }
        int rounds = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof GunItem) {
                rounds += GunData.loaded(stack);
            }
        }
        String variant = inputVariant(input);
        if (variant == null || !gun.ammoType().hasVariant(variant)) {
            variant = AmmoType.DEFAULT;
        }
        GunData.setAmmo(result, new GunAmmo(Math.min(rounds, gun.stats().firing().clipSize()), variant));
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.TRANSFER_AMMO.get();
    }

    /** Same format as vanilla shaped recipes. */
    public static class Serializer implements RecipeSerializer<TransferAmmoRecipe> {
        private static final MapCodec<TransferAmmoRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.shapedPattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem(null)),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification)
        ).apply(instance, TransferAmmoRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, TransferAmmoRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ShapedRecipe::getGroup,
                CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
                ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.shapedPattern,
                ItemStack.STREAM_CODEC, recipe -> recipe.getResultItem(null),
                ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
                TransferAmmoRecipe::new);

        @Override
        public MapCodec<TransferAmmoRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TransferAmmoRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
