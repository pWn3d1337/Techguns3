package techguns.gun;

import net.minecraft.world.item.ItemStack;
import techguns.registry.TGComponents;

/**
 * Typed access to a gun stack's data components. The only place that reads/writes gun components.
 */
// MIGRATION(26.x): newer vanilla adds components (use_cooldown, weapon components) that may replace parts of this.
public final class GunData {
    private GunData() {
    }

    public static GunAmmo ammo(ItemStack stack) {
        return stack.getOrDefault(TGComponents.GUN_AMMO.get(), GunAmmo.EMPTY);
    }

    public static int loaded(ItemStack stack) {
        return ammo(stack).loaded();
    }

    public static String variant(ItemStack stack) {
        return ammo(stack).variant();
    }

    public static void setAmmo(ItemStack stack, GunAmmo ammo) {
        stack.set(TGComponents.GUN_AMMO.get(), ammo);
    }

    public static void setLoaded(ItemStack stack, int loaded) {
        setAmmo(stack, ammo(stack).withLoaded(loaded));
    }

    /**
     * Removes up to {@code amount} rounds.
     *
     * @return rounds actually removed
     */
    public static int consume(ItemStack stack, int amount) {
        int loaded = loaded(stack);
        int consumed = Math.min(loaded, amount);
        setLoaded(stack, loaded - consumed);
        return consumed;
    }
}
