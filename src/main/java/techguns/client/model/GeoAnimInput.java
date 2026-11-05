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
 * @param ammo         loaded rounds as a fraction of the magazine (0..1), Molang {@code query.tg_ammo}
 * @param ammoCount    loaded rounds, Molang {@code query.tg_ammo_count}
 * @param empty        no rounds left
 * @param mirror       left hand: the whole-gun motion on the {@code root} bone is mirrored (x offset, y and z turn)
 * @param holdPitch    extra turn of the {@code root} bone about x in degrees (lowered offhand gun)
 * @param holdYaw      extra turn of the {@code root} bone about y in degrees, for the right hand (crossbow hold)
 * @param recoil       whole-gun motion on the fire timer, null if the gun renderer still does it in code
 * @param reloadMotion whole-gun motion on the reload timer, null if the gun renderer still does it in code
 * @param chargeMotion whole-gun motion on the charge progress, null if none or the gun renderer still does it in code
 * @param events       who holds the gun and which timer runs, for the sound, particle and instruction keyframes; null
 *                     when no keyframe events fire (GUI, ground, frames)
 * @param gui          drawn as a GUI icon, Molang {@code query.tg_gui} (e.g. a laser beam bone scaled to 0 in {@code hold})
 * @param firstPerson  drawn in first person, Molang {@code query.tg_first_person} (e.g. a belt that feeds only there)
 * @param inHand       held (first or third person), Molang {@code query.tg_in_hand}; false for GUI, ground and frames
 * @param grapple      grappling hook of the holder (0 in the gun, 1 launched, -1 attached), Molang {@code query.tg_grapple}
 */
public record GeoAnimInput(float fire, float reload, float aim, float charge, float ammo, int ammoCount, boolean empty, boolean mirror,
                           float holdPitch, float holdYaw, @Nullable Layer recoil, @Nullable Layer reloadMotion,
                           @Nullable Layer chargeMotion, @Nullable Events events, boolean gui, boolean firstPerson,
                           boolean inHand, int grapple) {
    /** Not animated and nothing loaded. */
    public static final GeoAnimInput NONE = still(false, 0);

    /**
     * Not animated (GUI, ground, item frames): {@code ammoCount} loaded rounds (the magazine size is unknown, so
     * {@code query.tg_ammo} is full or empty).
     */
    public static GeoAnimInput still(boolean gui, int ammoCount) {
        return new GeoAnimInput(0f, 0f, 0f, 0f, ammoCount > 0 ? 1f : 0f, ammoCount, false, false, 0f, 0f, null, null, null, null, gui,
                false, false, 0);
    }

    /** An animation played with a strength: {@code move} scales its offsets, {@code turn} its rotations. */
    public record Layer(String animation, float move, float turn) {
    }

    /**
     * Source of keyframe events: an event fires once when the sampled time of its animation passes it, per holder, hand
     * and run of the timer.
     *
     * @param holder      entity id of the holder
     * @param hand        hand ordinal (main 0, off 1)
     * @param firstPerson drawn in first person (view space), else in camera-relative world space
     * @param fireCycle   identifies the running recoil (0 = none): {@code fire} / {@code fire_last} and recoil events
     * @param reloadCycle identifies the running reload (0 = none): {@code reload} and reload motion events
     */
    public record Events(int holder, int hand, boolean firstPerson, long fireCycle, long reloadCycle) {
    }
}
