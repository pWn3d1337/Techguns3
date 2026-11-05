package techguns.client.render.geo;

import software.bernie.geckolib.loading.math.MathParser;
import techguns.client.model.GeoAnimInput;

/**
 * Molang queries a gun animation can read (docs/gun_animation_plan.md), set before each gun's animations are sampled:
 * <ul>
 *     <li>{@code query.tg_fire}, {@code query.tg_reload}, {@code query.tg_charge}, {@code query.tg_zoom}: 0..1 progress of the
 *     recoil, reload, charge and zoom timers (also outside the animation they drive, e.g. a part that moves with the
 *     reload in the {@code fire} animation);</li>
 *     <li>{@code query.tg_ammo}: loaded rounds as a fraction of the magazine; {@code query.tg_ammo_count}: loaded rounds
 *     (belts, magazines that empty visibly);</li>
 *     <li>{@code query.tg_time}: world time in seconds with the partial tick, for loops (spinning parts);</li>
 *     <li>{@code query.tg_gui}: 1 when drawn as a GUI icon, else 0;</li>
 *     <li>{@code query.tg_first_person}: 1 when drawn in first person, else 0;</li>
 *     <li>{@code query.tg_in_hand}: 1 when held (first or third person), 0 for GUI icons, on the ground and in frames;</li>
 *     <li>{@code query.tg_grapple}: grappling hook of the holder: 0 in the gun, 1 launched, -1 attached or pulling;</li>
 *     <li>{@code query.tg_locked}: 1 in first person once the lock-on of the held gun is complete, else 0.</li>
 * </ul>
 * The short prefix {@code q.} works as well. GeckoLib creates a variable the first time a file names it, so the order of
 * loading does not matter: the same instance is updated here.
 */
// MIGRATION(26.x): GeckoLib 5 replaces MathParser variables with its own query registration.
final class GunQueries {
    private static double fire, reload, charge, zoom, ammo, ammoCount, time, gui, firstPerson, inHand, grapple, locked;

    static {
        MathParser.setVariable("query.tg_fire", () -> fire);
        MathParser.setVariable("query.tg_reload", () -> reload);
        MathParser.setVariable("query.tg_charge", () -> charge);
        MathParser.setVariable("query.tg_zoom", () -> zoom);
        MathParser.setVariable("query.tg_ammo", () -> ammo);
        MathParser.setVariable("query.tg_ammo_count", () -> ammoCount);
        MathParser.setVariable("query.tg_time", () -> time);
        MathParser.setVariable("query.tg_gui", () -> gui);
        MathParser.setVariable("query.tg_first_person", () -> firstPerson);
        MathParser.setVariable("query.tg_in_hand", () -> inHand);
        MathParser.setVariable("query.tg_grapple", () -> grapple);
        MathParser.setVariable("query.tg_locked", () -> locked);
    }

    private GunQueries() {
    }

    /** The values of this draw; {@code seconds} is the world time (render thread only, like the sampling). */
    static void set(GeoAnimInput anim, double seconds) {
        fire = anim.fire();
        reload = anim.reload();
        charge = anim.charge();
        zoom = anim.aim();
        ammo = anim.ammo();
        ammoCount = anim.ammoCount();
        time = seconds;
        gui = anim.gui() ? 1 : 0;
        firstPerson = anim.firstPerson() ? 1 : 0;
        inHand = anim.inHand() ? 1 : 0;
        grapple = anim.grapple();
        locked = anim.locked() ? 1 : 0;
    }
}
