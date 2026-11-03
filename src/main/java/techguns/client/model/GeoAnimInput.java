package techguns.client.model;

/**
 * What a geo gun model animates in one draw: the progress (0..1, 0 = not playing) of the gun's timers. Each animation is
 * sampled at progress x its length in the animation file, so it always lasts exactly as long as the timer (the gun's
 * stats), whatever length it was authored with.
 *
 * @param fire   recoil timer: {@code fire}, or {@code fire_last} when the shot emptied the gun (its end pose then stays
 *               while the gun is empty)
 * @param reload reload timer: {@code reload}
 * @param aim    zoom: {@code aim} (runs backwards when zooming out)
 * @param charge charge: {@code charge}
 * @param empty  no rounds left
 */
public record GeoAnimInput(float fire, float reload, float aim, float charge, boolean empty) {
    /** Not animated (GUI, ground, item frames). */
    public static final GeoAnimInput NONE = new GeoAnimInput(0f, 0f, 0f, 0f, false);
}
