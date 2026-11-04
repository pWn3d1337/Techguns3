package techguns.client.model;

import javax.annotation.Nullable;

/**
 * What a geo gun model animates in one draw: the progress (0..1, 0 = not playing) of the gun's timers. Each animation is
 * sampled at progress x its length in the animation file, so it always lasts exactly as long as the timer (the gun's
 * stats), whatever length it was authored with.
 *
 * @param fire         recoil timer: {@code fire}, or {@code fire_last} when the shot emptied the gun (its end pose then
 *                     stays while the gun is empty)
 * @param reload       reload timer: {@code reload}
 * @param aim          zoom: {@code aim} (runs backwards when zooming out)
 * @param charge       charge: {@code charge}
 * @param empty        no rounds left
 * @param mirror       left hand: the whole-gun motion on the {@code root} bone is mirrored (x offset, y and z turn)
 * @param holdPitch    extra turn of the {@code root} bone about x in degrees (lowered offhand gun)
 * @param holdYaw      extra turn of the {@code root} bone about y in degrees, for the right hand (crossbow hold)
 * @param recoil       whole-gun motion on the fire timer, null if the gun renderer still does it in code
 * @param reloadMotion whole-gun motion on the reload timer, null if the gun renderer still does it in code
 * @param chargeMotion whole-gun motion on the charge progress, null if none or the gun renderer still does it in code
 */
public record GeoAnimInput(float fire, float reload, float aim, float charge, boolean empty, boolean mirror, float holdPitch,
                           float holdYaw, @Nullable Layer recoil, @Nullable Layer reloadMotion, @Nullable Layer chargeMotion) {
    /** Not animated (GUI, ground, item frames). */
    public static final GeoAnimInput NONE = new GeoAnimInput(0f, 0f, 0f, 0f, false, false, 0f, 0f, null, null, null);

    /** An animation played with a strength: {@code move} scales its offsets, {@code turn} its rotations. */
    public record Layer(String animation, float move, float turn) {
    }
}
