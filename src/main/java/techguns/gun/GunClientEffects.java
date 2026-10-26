package techguns.gun;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/**
 * Client-side gun effects triggered from common gun code. The client entrypoint installs the real
 * implementation; on a dedicated server the no-op default stays.
 */
public interface GunClientEffects {
    GunClientEffects NONE = new GunClientEffects() {
    };

    /** A gun was fired by {@code shooter} (recoil, muzzle flash). */
    default void onFired(LivingEntity shooter, InteractionHand hand, int recoilTicks, int muzzleFlashTicks) {
    }

    /** A charge gun was released with {@code charge} (0..1); the charge animation fades out during the recoil. */
    default void onChargeReleased(LivingEntity shooter, InteractionHand hand, float charge) {
    }

    /** A reload started. */
    default void onReloadStarted(LivingEntity shooter, InteractionHand hand, int reloadTicks) {
    }

    /** Toggle zoom for the local player. */
    default void toggleZoom(float fovMultiplier) {
    }

    /** Leave zoom for the local player. */
    default void resetZoom() {
    }

    /** Whether shift is held (expanded tooltips). */
    default boolean hasShiftDown() {
        return false;
    }

    static GunClientEffects get() {
        return Holder.instance;
    }

    static void install(GunClientEffects effects) {
        Holder.instance = effects;
    }

    final class Holder {
        private static volatile GunClientEffects instance = NONE;

        private Holder() {
        }
    }
}
