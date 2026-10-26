package techguns.recipe;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.GunItem;
import techguns.gun.ammo.AmmoType;
import techguns.registry.TGRecipes;

/**
 * Changes a gun's ammo variant (Fabric {@code AmmoChangeRecipe}): a gun plus an ammo item of another variant gives the
 * gun loaded with that variant; the rounds left in the gun come back as a full magazine or loose rounds and the empty
 * item.
 */
public class AmmoChangeRecipe extends CustomRecipe {
    public AmmoChangeRecipe(CraftingBookCategory category) {
        super(category);
    }

    /** The gun, its target variant and the input slot of the gun. */
    private record Target(ItemStack gun, GunItem item, String variant, int gunSlot) {
    }

    @Nullable
    private static Target target(CraftingInput input) {
        ItemStack gun = ItemStack.EMPTY;
        ItemStack ammo = ItemStack.EMPTY;
        int gunSlot = -1;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof GunItem) {
                if (!gun.isEmpty()) {
                    return null;
                }
                gun = stack;
                gunSlot = i;
            } else {
                if (!ammo.isEmpty()) {
                    return null;
                }
                ammo = stack;
            }
        }
        if (gun.isEmpty() || ammo.isEmpty()) {
            return null;
        }
        GunItem item = (GunItem) gun.getItem();
        String variant = variantOf(item.ammoType(), ammo);
        if (variant == null || variant.equals(GunData.variant(gun))) {
            return null;
        }
        return new Target(gun, item, variant, gunSlot);
    }

    /** The variant whose single reload item is {@code ammo}, or null. */
    @Nullable
    private static String variantOf(AmmoType type, ItemStack ammo) {
        for (AmmoType.Variant variant : type.variants()) {
            List<ItemLike> items = variant.ammo();
            if (items.size() == 1 && ammo.is(items.getFirst().asItem())) {
                return variant.key();
            }
        }
        return null;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return target(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Target target = target(input);
        if (target == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = target.gun().copyWithCount(1);
        var firing = target.item().stats().firing();
        // one reload item's worth (Fabric loaded 1 round for multi-item reloads)
        int loaded = firing.ammoPerReload() > 1 ? firing.clipSize() / firing.ammoPerReload() : firing.clipSize();
        GunData.setAmmo(result, new GunAmmo(loaded, target.variant()));
        return result;
    }

    /** The old rounds go to the gun's slot, further items to the ammo slot (or the player if it is still occupied). */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(input);
        Target target = target(input);
        if (target == null) {
            return remaining;
        }
        List<ItemStack> unloaded = target.item().unloadedItems(GunData.ammo(target.gun()));
        int next = 0;
        for (int i = 0; i < remaining.size() && next < unloaded.size(); i++) {
            if (i == target.gunSlot() || (!input.getItem(i).isEmpty() && remaining.get(i).isEmpty())) {
                remaining.set(i, unloaded.get(next++));
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TGRecipes.AMMO_CHANGE.get();
    }
}
